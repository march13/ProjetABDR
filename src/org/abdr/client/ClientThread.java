package org.abdr.client;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;

import org.abdr.node.Node;
import org.abdr.node.master.MasterNode;

public class ClientThread implements Runnable {
	public int numThread;
	public float myTime;

	public ClientThread() {
		super();
		myTime = 0;
		// TODO Auto-generated constructor stub
	}

	public ClientThread(int numThread) {
		this.numThread = numThread+1;
	}

	public void myRun() throws Exception {
		Interruptor inter = new Interruptor(Thread.currentThread());
		Timer tim = new Timer();
		int i = 0;
		float finalTime = 0;
		String name = "client" + numThread;
		Node node = new ClientNode(name);
		node.init(new Integer(numThread));
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind(name, node);
		
		System.out.println("Le noeud client " + numThread + " a démarré");
		MasterNode master = (MasterNode) registry.lookup("master");
		master.addClient("localhost", 1099, name);
		tim.schedule(inter, 10000);
		while (!Thread.currentThread().isInterrupted()) {
			i++;
			long start = System.currentTimeMillis();
			long time;
			KeyObject obj = new KeyObject(0, 1, 2, 3, 4, "blall"+numThread+" "+i, "jkhc",
					"jkhqskl", "khhkzejh", "lkjlmj");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(obj);
			byte[] myBytes = bos.toByteArray();
			master.put("P1", numThread+"l" + i, myBytes);
			//master.put("P2", numThread+"l" + i, myBytes);
			//master.put("P3", numThread+"l" + i, myBytes);

			/*byte[] myReturn = master.get("P1", numThread+"l" + i);

			ByteArrayInputStream bis = new ByteArrayInputStream(myReturn);
			ObjectInput in = null;
			try {
				in = new ObjectInputStream(bis);
				KeyObject o = (KeyObject) in.readObject();
				System.out.println(o.getTxt1());
			} finally {
					bis.close();
					if (in != null) {
						in.close();
				}
			}
*/			time = System.currentTimeMillis();
			myTime = (time - start);
			 finalTime += myTime;
		}

		finalTime = finalTime / i;
		tim.cancel();
		System.out.println("Tread n°" + numThread + " temps moyen: "
				+ finalTime);
		master.deleteClient(name);
		node.close();
		registry.unbind(name);
	}

	@Override
	public void run() {
		try {
			myRun();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
