package org.abdr.node;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.abdr.node.master.MasterNode;
import org.abdr.node.master.MasterNodeImpl;

public class Main {

	public static void main(String[] args) throws Exception {
		if(isMaster(args)) {
			startMaster(LocateRegistry.createRegistry(1099), args);
		} else {
			startSlave(LocateRegistry.getRegistry(), args);
		}
	}

	static private void startMaster(Registry registry, String[] args)
			throws Exception {
		Node node = new MasterNodeImpl();
		registry.bind(node.getName(), node);
		node = (Node) registry.lookup(node.getName());
		node.init(0);
		System.out.println("Le noeud maître a démarré");
	}

	static private void startSlave(Registry registry, String[] args)
			throws Exception {
		String name;
		if (!args[1].toString().equals(null))
			 name = "slave"+args[1].toString();
		else
			return;
		Node node = new SlaveNode(name);
		//node.setNumNode(new Integer(args[1].toString()));
		registry.rebind(name, node);
		node = (Node) registry.lookup(name);
		node.init(new Integer(args[1].toString()));
		System.out.println("Le noeud esclave a démarré");
		MasterNode master = (MasterNode) registry.lookup("master");
		master.addSlave("localhost", 1099, name);
		
		//master.put(name, name);
		//System.out.println(new String(master.get(name)));
	}

	static private boolean isMaster(String[] args) {
		if (args == null || args.length < 1) {
			return false;
		}
		return Boolean.parseBoolean(args[0]);
	}

}
