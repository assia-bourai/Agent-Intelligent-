package agents;

import java.util.Hashtable;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ProductSellerAgent extends Agent {

	// Catalogue des produits (chaque produit est accocié à son prix
	private Hashtable catalogue;
	// Interface qui permet à l'utilisateur d'ajouter un produit à vendre
	private ProductSellerInt myInt;

	// Initialisation de l'agent
	protected void setup() {
		// Création du catalogue
		catalogue = new Hashtable();
		// Create and show the GUI
		myInt = new ProductSellerInt(this);
		myInt.showGui();
		// Add the behaviour serving requests for offer from buyer agents
		addBehaviour(new OfferRequestsServer());
		// Add the behaviour serving purchase orders from buyer agents
		addBehaviour(new PurchaseOrdersServer());
	}

	// Put agent clean-up operations here
	protected void takeDown() {

		// Close the GUI
		myInt.dispose();
		// Printout a dismissal message
		System.out.println("Seller-agent " + getAID().getName() + " terminating.");
	}

	/**
	 * This is invoked by the GUI when the user adds a new book for sale
	 */
	public void updateCatalogue(final String name, final int price) {
		addBehaviour(new OneShotBehaviour() {
			public void action() {
				catalogue.put(name, new Integer(price));
			}
		});
	}

	/**
	 * Classe interne OfferRequestsServer. Il s'agit du comportement utilisé par
	 * l'agent vendeur pour répondre aux demandes d'offres entrantes de l'agent
	 * acheteur. Si le produit demandé se trouve dans le catalogue , l'agent vendeur
	 * répond avec un message PROPOSE spécifiant le prix. Sinon, un message REFUSE
	 * est renvoyé.
	 */
	private class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {

				// réception du message , création de la réponse
				String name = msg.getContent();
				ACLMessage reply = msg.createReply();

				Integer price = (Integer) catalogue.get(name);
				if (price != null) {
					// produit disponible, retourner le prix
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(price.intValue()));
				} else {
					// le produit n'est pas disponible
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("le produit n'es pas disponible");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	}

	/**
	 * Classe interne PurchaseOrdersServer. Il s'agit du comportement utilisé par
	 * les agents vendeurs pour servir les acceptations d'offres entrantes
	 * (c'est-à-dire les bons de commande) des agents acheteurs. L'agent vendeur
	 * supprime le produit acheté du  catalogue et répond par un message INFORM
	 * pour informer l'acheteur que l'achat a été effectué avec succès.
	 */
	private class PurchaseOrdersServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL message reçu 
				String name = msg.getContent();
				ACLMessage reply = msg.createReply();

				Integer price = (Integer) catalogue.remove(name);
				if (price != null) {
					reply.setPerformative(ACLMessage.INFORM);
					System.out.println(name + " vendu à l'agent " + msg.getSender().getName());
				} else {
					// le produit n'est plus disponible.
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("produit non disponible");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	}
}
