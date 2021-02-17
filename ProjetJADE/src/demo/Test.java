package demo;

import jade.core.Agent;

public class Test extends Agent{
	protected void setup() {
		System.out.println("Hello World. ");
		System.out.println("My name is " + getLocalName());
/*
		Object args[] = getArguments();

		for (int i = 0; i < args.length; i++) {
			System.out.println("Argument " + i + " : " + args[i].toString());
		}*/
	}

}
