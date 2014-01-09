package org.abdr.node.master;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Operation;
import oracle.kv.OperationFactory;
import oracle.kv.ReturnValueVersion;
import oracle.kv.Value;
import oracle.kv.ValueVersion;

import org.abdr.client.KeyObject;
import org.abdr.node.AbstractNode;
import org.abdr.node.Node;

import com.sleepycat.je.rep.stream.Protocol.Entry;

public class MasterNodeImpl extends AbstractNode implements MasterNode {

	private static final long serialVersionUID = 1L;
	private List<Node> slaves;
	private List<Node> clients;
	Map<String, Integer> keyMap;
	Map<String, Integer> useMap;
	Map<Integer, Integer> storeUse;

	public MasterNodeImpl() throws RemoteException {
		super("master");
	}

	@Override
	public void put(String key, String cat, byte[] data) throws RemoteException {

		Integer selectedStore = keyMap.get(key);
		Node slave;
		int useMax = 10;
		if (selectedStore.equals(null)) {
			selectedStore = key.hashCode() % slaves.size();
			keyMap.put(key, selectedStore);
			useMap.put(key, 1);
			storeUse.put(selectedStore, storeUse.get(selectedStore) + 1);
			slave = slaves.get(selectedStore);
		} else {
			Integer newUseVal = useMap.get(key) + 1;
			useMap.put(key, newUseVal);
			storeUse.put(selectedStore, storeUse.get(selectedStore) + 1);
			slave = slaves.get(selectedStore);
			// A partir d'ici on va demander aux esclaves la migration
			if ((useMap.get(key).intValue()) % useMax == 0) {
				System.out.println("je suis dans le if");
				Integer minStore = Collections.min(storeUse.values());
				for (Integer b : storeUse.keySet()) {
					if (storeUse.get(b) == minStore) {
						minStore = b;
						break;
					}
				}
				// mise à jour de mes valeurs d'utilisation de store
				System.out.println("minstore " + minStore + "value"
						+ storeUse.get(minStore));
				storeUse.put(minStore, storeUse.get(minStore) + useMap.get(key));
				storeUse.put(selectedStore, storeUse.get(selectedStore)
						- useMap.get(key));
				// slave.migrate(key);
				try {
					KVStore storeSrc, storeDest;
					storeSrc = KVStoreFactory.getStore(new KVStoreConfig(
							"kvstore", "localhost" + ":" + "500"
									+ selectedStore * 2));
					storeDest = KVStoreFactory.getStore(new KVStoreConfig(
							"kvstore", "localhost" + ":" + "500"
									+ minStore * 2));
					SortedMap<Key, ValueVersion> values;
					Key k = Key.createKey(key);
					values = storeSrc.multiGet(k, null, null);
					System.out.println("nb values rec " + values.size());
					for (java.util.Map.Entry<Key, ValueVersion> obj : values.entrySet()) {
						storeDest.put(obj.getKey(), obj.getValue().getValue());
					}
					storeSrc.multiDelete(k, null, null);
					storeDest.close();
					storeSrc.close();
				

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// selectionner un autre store
				System.out.println("je migre de " + selectedStore + " a "
						+ minStore);
				keyMap.put(key, minStore);
				selectedStore = minStore;
			}
		}
		System.out.println("selected store " + selectedStore);

		System.out.println(selectedStore);

		System.out.println("Redirection vers l'esclave " + slave.getName());
		slave.put(key, cat, data);
	}

	@Override
	public synchronized void init(int numNode) throws Exception {
		if (isStarted) {
			return;
		}
		int nbStores = 3;
		slaves = new ArrayList<>();
		clients = new ArrayList<>();
		ArrayList<KVStore> stores;
		stores = new ArrayList<KVStore>();
		String storeName = "kvstore";
		String hostName = "localhost";
		int hostPort = 5000;

		storeUse = new HashMap<Integer, Integer>();
		for (int i = 0; i < nbStores; i++) {
			int port = hostPort + 2 * i;
			storeUse.put(new Integer(i), new Integer(0));
			System.out.println(port);
			stores.add(KVStoreFactory.getStore(new KVStoreConfig(storeName,
					hostName + ":" + port)));
		}

		keyMap = new HashMap<String, Integer>();
		useMap = new HashMap<String, Integer>();
		for (int j = 1; j < 20; j++) {
			Key k1, compteur;
			int hash = ("P" + j).hashCode() % nbStores;
			keyMap.put("P" + j, new Integer(hash));
			useMap.put("P" + j, new Integer(0));

			compteur = Key.createKey("P" + j, "compteur");
			for (int i = 1; i < 101; i++) {
				// je crée l'objet avec ses attributs puis le sérialize
				KeyObject obj = new KeyObject(0, 1, 2, 3, 4, "blall", "jkhc",
						"jkhqskl", "khhkzejh", "lkjlmj");
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutput out = null;
				try {
					out = new ObjectOutputStream(bos);
					out.writeObject(obj);
					byte[] myBytes = bos.toByteArray();
					k1 = Key.createKey("P" + j, "O" + i);

					// TODO gerer le hash en fonction du nombre de stores
					stores.get(hash).put(k1, Value.createValue(myBytes));

				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException ex) {
						// ignore close exception
					}
					try {
						bos.close();
					} catch (IOException ex) {
						// ignore close exception
					}
				}
			}
			stores.get(hash).put(compteur,
					Value.createValue(Integer.toString(100).getBytes()));
		}
		System.out.println("Fin d'initialisation");

		for (int i = 0; i < stores.size(); i++) {
			stores.get(i).close();
		}

		isStarted = true;
	}

	@Override
	public synchronized void close() throws IOException {
		if (isStarted) {
			slaves.clear();
			isStarted = false;
		}
	}

	@Override
	public byte[] get(String key) throws RemoteException {
		return slaves.get(0).get(key);
	}

	@Override
	public void addSlave(String host, int port, String name) throws Exception {
		Registry registry = LocateRegistry.getRegistry();
		Node node = (Node) registry.lookup(name);

		slaves.add(node);
	}

	public void addClient(String host, int port, String name) throws Exception {
		Registry registry = LocateRegistry.getRegistry();
		Node node = (Node) registry.lookup(name);

		clients.add(node);
	}

	@Override
	public void deleteSlave(String name) throws RemoteException {
		for (Node node : slaves) {
			if (node.getName().equals(name)) {
				slaves.remove(node);
				break;
			}
		}
	}

	@Override
	public void deleteClient(String name) throws RemoteException {
		for (Node node : slaves) {
			if (node.getName().equals(name)) {
				slaves.remove(node);
				break;
			}
		}
	}

}
