package org.abdr.node;

import java.io.Closeable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.SortedMap;
import java.util.SortedSet;

import oracle.kv.Key;
import oracle.kv.ValueVersion;

public interface Node extends Remote, Closeable {

	String getName() throws RemoteException;
	
	void init(int numNode) throws Exception;
	
	boolean isStarted() throws RemoteException;
	
	void put(String key, String cat, byte[] data) throws RemoteException;
	
	byte[] get(String key) throws RemoteException;
	
}
