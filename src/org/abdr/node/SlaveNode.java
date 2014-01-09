package org.abdr.node;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.SortedMap;
import java.util.SortedSet;

import oracle.kv.Depth;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.KeyRange;
import oracle.kv.Value;
import oracle.kv.ValueVersion;

public class SlaveNode extends AbstractNode implements Node {

	private static final long serialVersionUID = 1L;
	private transient KVStore store;
	public SlaveNode() throws RemoteException {
		super("default");
	}

	public SlaveNode(String name) throws RemoteException {
		super(name);
	}

	@Override
	public void put(String key, String cat, byte[] data) throws RemoteException {
		//ici il faut faire l'ajout dans mon store
		
		Key k = Key.createKey(key, cat);
		Value value = Value.createValue(data);
		store.put(k, value);
	}

	@Override
	public synchronized void close() throws IOException {
		if (isStarted) {
			store.close();
			isStarted = false;
		}
	}

	@Override
	public synchronized void init(int numNode) throws Exception {
		if (isStarted) {
			return;
		}
		store = KVStoreFactory.getStore(new KVStoreConfig("kvstore",
				"localhost" + ":" + "500"+numNode));
		isStarted = true;
		
	}

	@Override
	public byte[] get(String key) throws RemoteException {
		ValueVersion valueVersion = store.get(Key.createKey(key));
		if (valueVersion == null) {
			return null;
		}
		return valueVersion.getValue().getValue();
	}




}
