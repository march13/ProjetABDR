package org.abdr.node;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public abstract class AbstractNode extends UnicastRemoteObject implements Node {

	private static final long serialVersionUID = 1L;
	private String name;
	protected boolean isStarted = false;

	public AbstractNode(String name) throws RemoteException {
		super();
		this.name = name;
	}

	@Override
	public String getName() throws RemoteException {
		return name;
	}

	@Override
	public boolean isStarted() throws RemoteException {
		return isStarted;
	}

	@Override
	public String toString() {
		return name;
	}
}
