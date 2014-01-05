package org.abdr.node;

import java.io.Closeable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote, Closeable {

	String getName() throws RemoteException;
	
	void init() throws Exception;
	
	boolean isStarted() throws RemoteException;
	
	void put(String key, String data) throws RemoteException;
	
	byte[] get(String key) throws RemoteException;
	
}
