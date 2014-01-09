package org.abdr.client;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.abdr.node.Node;
import org.abdr.node.master.MasterNode;

public class Client {

	public Client() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String name;
		if (!args[1].toString().equals(null))
			 name = "client"+args[1].toString();
		else
			return;
		Node node = new ClientNode(name);
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind(name, node);
		node.init(new Integer(args[1].toString()));
		System.out.println("Le noeud client a démarré");
		MasterNode master = (MasterNode) registry.lookup("master");
		master.addClient("localhost", 1099, name);
		KeyObject obj = new KeyObject(0, 1, 2, 3, 4, "blall", "jkhc",
				"jkhqskl", "khhkzejh", "lkjlmj");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(obj);
		byte[] myBytes = bos.toByteArray();
		for(int i = 0; i<30; i++)
			master.put("P1", "43"+i,myBytes);
		master.deleteClient(name);
		node.close();
		registry.unbind(name);
		System.exit(0);
	}

}
