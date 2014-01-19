package org.abdr.client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import oracle.kv.Version;

import org.abdr.node.AbstractNode;
import org.abdr.node.KeyValueVersion;
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
	public byte[] get(String key, String cat ) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Version putifVersion(String key, String cat, byte[] data, Version matchVersion)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void takeMyData(String srcHost, String srcPort, String key)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void multiPut(List<KeyValueVersion> ops) throws RemoteException,
			InterruptedException {
		// TODO Auto-generated method stub
		
	}

}
