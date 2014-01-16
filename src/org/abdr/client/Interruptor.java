package org.abdr.client;
import java.util.TimerTask;


public class Interruptor extends TimerTask {
	private final Thread toInterrupt;
	
	
	public Interruptor(Thread toInterrupt) {
		super();
		this.toInterrupt = toInterrupt;
	}


	@Override
	public void run() {
		toInterrupt.interrupt();
		
	}

}
