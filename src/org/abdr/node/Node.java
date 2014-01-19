package org.abdr.node;

import java.util.List;
import java.io.Closeable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import oracle.kv.Version;

public interface Node extends Remote, Closeable {

	String getName() throws RemoteException;
	
	void init(int numNode) throws Exception;
	
	boolean isStarted() throws RemoteException;
	
	void put(String key, String cat, byte[] data) throws RemoteException, InterruptedException;
	
	Version putifVersion(String key, String cat, byte[] data, Version matchVersion) throws RemoteException, InterruptedException;
	
	byte[] get(String key, String cat) throws RemoteException, InterruptedException;

	void takeMyData(String srcHost, String srcPort, String key)
			throws RemoteException;

	void multiPut(List<KeyValueVersion> ops)
			throws RemoteException, InterruptedException;
	
}
