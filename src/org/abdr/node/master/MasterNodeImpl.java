package org.abdr.node.master;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import org.abdr.node.AbstractNode;
import org.abdr.node.Node;

public class MasterNodeImpl extends AbstractNode implements MasterNode {

	private static final long serialVersionUID = 1L;
	private List<Node> slaves;

	public MasterNodeImpl() throws RemoteException {
		super("master");
	}

	@Override
	public void put(String key, String data) throws RemoteException {
		int selected = key.hashCode() % slaves.size();
		Node slave = slaves.get(selected);
		System.out.println("Redirection vers l'esclave " + slave);
		slave.put(key, data);
	}

	@Override
	public synchronized void init() throws Exception {
		if (isStarted) {
			return;
		}
		slaves = new ArrayList<>();
		isStarted = true;
	}

	@Override
	public synchronized void close() throws IOException {
		if (isStarted) {
			slaves.clear();
			isStarted = false;
		}
	}

	@Override
	public byte[] get(String key) throws RemoteException {
		return slaves.get(0).get(key);
	}

	@Override
	public void addSlave(String host, int port, String name) throws Exception {
		Registry registry = LocateRegistry.getRegistry(host, port);
		Node node = (Node) registry.lookup(name);
		slaves.add(node);
	}

	@Override
	public void deleteSlave(String name) throws RemoteException {
		for(Node node : slaves) {
			if(node.getName().equals(name)) {
				slaves.remove(node);
				break;
			}
		}
	}

}
