package org.abdr.client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.SortedMap;

import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.ValueVersion;

import org.abdr.node.AbstractNode;
import org.abdr.node.Node;

public class ClientNode extends AbstractNode implements Node {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClientNode() throws RemoteException {
		super("default");
	}

	public ClientNode(String name) throws RemoteException {
		super(name);
	}

	@Override
	public void close() throws IOException {
		if (isStarted) {
			isStarted = false;
		}
	}

	@Override
	public void init(int numNode) throws Exception {
		if (isStarted) {
			return;
		}
		isStarted = true;
	}

	@Override
	public void put(String key, String cat, byte[] data) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] get(String key) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
