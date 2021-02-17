package jade.jadeProjet;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class ProductBuyerAgent extends Agent {

	// le nom du produit � acheter
	private String targetProductName;
	//  agent vendeur
	private AID sellerAgent = new AID("vendeur1", AID.ISLOCALNAME);

	// initialisation de l'agent
	protected void setup() {

		// Printout a welcome message
		System.out.println("Hello! Buyer-agent " + getAID().getName() + " is ready.");

		// passer le produit � acheter comme argument

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			targetProductName = (String) args[0];
			System.out.println("Trying to buy " + targetProductName);
			// Add a TickerBehaviour that schedules a request to seller agents every minute

			addBehaviour(new TickerBehaviour(this, 60000) {
				protected void onTick() {
					myAgent.addBehaviour(new RequestPerformer());
				}
			});
		} else {
			// Make the agent terminate immediately
			System.out.println("le nom du produit n'a pas �t� sp�cifi�");
			doDelete();
		}
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Buyer-agent " + getAID().getName() + " terminating.");
	}

	/**
	 * Classe interne RequestPerformer. comportement utilis� par l'agent acheteur
	 * pour r�pondre � l'agent vendeur
	 */
	private class RequestPerformer extends Behaviour {
		private AID Seller; // The agent who provides the best offer
		private int Price; // The best offered price
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;

		public void action() {
			switch (step) {
			case 0:
				// envoyer le message � l'agent vendeur
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				cfp.addReceiver(sellerAgent);

				cfp.setContent(targetProductName);
				cfp.setConversationId("achat-produit");
				cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("achat-produit"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Reception des r�ponses des agents vendeurs
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer
						int price = Integer.parseInt(reply.getContent());
						Price = price;
							Seller = reply.getSender();
						
					}
				step = 2;

				} else {
					block();
				}
				break;
			case 2:
				// envoie du bon de commande  � l'agent vendeur 
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(Seller);
				order.setContent(targetProductName);
				order.setConversationId("book-trade");
				order.setReplyWith("order" + System.currentTimeMillis());
				myAgent.send(order);
				// Prepare the template to get the purchase order reply
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:
				// Receive the purchase order reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// Purchase order reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						System.out.println(targetProductName + " achet� avec succ�s "
								+ reply.getSender().getName());
						System.out.println("Price = " + Price);
						myAgent.doDelete();
					} else {
						System.out.println("Echec de l'achat du produit, r�essayez.");
					}

					step = 4;
				} else {
					block();
				}
				break;
			}
		}

		public boolean done() {
			if (step == 2 && Seller == null) {
				System.out.println("Echech de l'achat: " + targetProductName + " pas disponible pour la vente");
			}
			return ((step == 2 && Seller == null) || step == 4);
		}
	}

}
