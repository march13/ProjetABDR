package org.abdr.node;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.SortedMap;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.Version;

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
	public void takeMyData(String srcHost, String srcPort, String key) throws RemoteException {
		try {
			KVStore storeSrc, storeDest;
			storeDest  = this.store;
			storeSrc = KVStoreFactory.getStore(new KVStoreConfig(
					"kvstore", srcHost + ":" + srcPort));
			SortedMap<Key, ValueVersion> values;
			Key k = Key.createKey(key);
			values = storeSrc.multiGet(k, null, null);
			System.out.println("nb values rec " + values.size());
			for (java.util.Map.Entry<Key, ValueVersion> obj : values.entrySet()) {
				storeDest.put(obj.getKey(), obj.getValue().getValue());
			}
			storeSrc.multiDelete(k, null, null);
			storeSrc.close();
		

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	public byte[] get(String key, String cat ) throws RemoteException {
		ValueVersion valueVersion = store.get(Key.createKey(key, cat));
		if (valueVersion == null) {
			System.out.println(this.store.toString());
			return null;
		}
		return valueVersion.getValue().getValue();
	}

	@Override
	public Version putifVersion(String key, String cat, byte[] data, Version matchVersion)
			throws RemoteException {
		// TODO Auto-generated method stub
		Key k = Key.createKey(key, cat);
		Value value = Value.createValue(data);
		return store.putIfVersion(k, value, matchVersion);
	}

	@Override
	public void multiPut(List<KeyValueVersion> ops) throws RemoteException,
			InterruptedException {
		// TODO Auto-generated method stub
		
	}




}
