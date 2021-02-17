package demo;

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
		AgentController ac;
		
		AgentController ac_agent;
		
		try {
			String [] arg= new String [] { "ab","ac"};
			ac = mc.createNewAgent("agent1", Test.class.getName(), arg);
			ac.start();
			String [] arg2= new String [] { "mal","au","crane"};
			ac_agent = mc.createNewAgent("agent2", Test.class.getName(), arg2);
			ac_agent.start();
		} catch (StaleProxyException e) {
		}
	}
}