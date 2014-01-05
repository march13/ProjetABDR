package org.abdr.node.master;

import java.rmi.RemoteException;

import org.abdr.node.Node;

public interface MasterNode extends Node {

	void addSlave(String host, int port, String name) throws Exception;
	
	void deleteSlave(String name) throws RemoteException;
	
}
