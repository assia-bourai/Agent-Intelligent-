package jade.jadeProjet;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Launcher {
	public static void main(String[] args) {
		Runtime runtime = Runtime.instance();
		Profile config = new ProfileImpl("localhost", 8890, null);
		config.setParameter("gui", "true");
		AgentContainer mc = runtime.createMainContainer(config);
		AgentController acheteur;
		AgentController vendeur;
		
		
		try {
			String [] arg= new String [] { "fromage "};
			acheteur = mc.createNewAgent("acheteur1", ProductBuyerAgent.class.getName(), arg);
			acheteur.start();
			
			vendeur = mc.createNewAgent("vendeur1", ProductSellerAgent.class.getName(), null);
			vendeur.start();
			
		} catch (StaleProxyException e) {
		}
	}
}