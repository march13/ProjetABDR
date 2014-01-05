package org.abdr.node;

import java.io.IOException;
import java.rmi.RemoteException;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
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
	public void put(String key, String data) throws RemoteException {
		Key k = Key.createKey(key);
		Value value = Value.createValue(data.getBytes());
		store.put(k, value);
		System.out.println("Ajout de la donnée " + data);
	}

	@Override
	public synchronized void close() throws IOException {
		if (isStarted) {
			store.close();
			isStarted = false;
		}
	}

	@Override
	public synchronized void init() throws Exception {
		if (isStarted) {
			return;
		}
		store = KVStoreFactory.getStore(new KVStoreConfig("kvstore",
				"localhost" + ":" + 5000));
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
