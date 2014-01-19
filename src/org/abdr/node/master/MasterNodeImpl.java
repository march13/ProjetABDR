package org.abdr.node.master;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.Version;

import org.abdr.client.KeyObject;
import org.abdr.node.AbstractNode;
import org.abdr.node.Node;
import org.abdr.node.KeyValueVersion;


public class MasterNodeImpl extends AbstractNode implements MasterNode {

	private static final long serialVersionUID = 1L;
	int nbStores;
	private List<Node> slaves;
	private List<Node> clients;
	ArrayList<KVStore> stores;

	ConcurrentHashMap<String, KeyInfo> keyMap;
	ConcurrentHashMap<Integer, AtomicInteger> storeUse;
	// ConcurrentHashMap<String, AtomicInteger> useMap;
	// ConcurrentHashMap<String, Mutex> mutexes;
	int useMax;
	AtomicInteger totalUse;
	AtomicBoolean divisionRunning;
	Mutex verrou;

	public MasterNodeImpl() throws RemoteException {
		super("master");
		nbStores = 3;
		stores = new ArrayList<KVStore>();

		slaves = new ArrayList<>();
		clients = new ArrayList<>();
		keyMap = new ConcurrentHashMap<String, KeyInfo>();
		storeUse = new ConcurrentHashMap<Integer, AtomicInteger>();
		useMax = 1000;
		totalUse = new AtomicInteger(0);
		divisionRunning = new AtomicBoolean(false);
		verrou = new Mutex();
	}

	private void incrementUse(KeyInfo ki) {
		ki.getUse().incrementAndGet();
		storeUse.get(ki.getStore()).incrementAndGet();
		totalUse.incrementAndGet();
	}

	private void divideByTwo() {
		if (!divisionRunning.getAndSet(true)) {
			if (totalUse.get() >= 1000) {
				for (KeyInfo entry : keyMap.values()) {
					AtomicInteger kUse = entry.getUse();
					kUse.getAndAdd(-kUse.get() / 2);
				}
				for (AtomicInteger entry : storeUse.values()) {
					entry.getAndAdd(-entry.get() / 2);
				}
				totalUse.addAndGet(-1000);
			}
			divisionRunning.set(false);
		}
	}

	/**
	 * Get the KeyInfo associated with the specified key
	 * The key is created in the default store (ie. matching its hashcode)
	 * if it doesn't exist.
	 * 
	 * @param key
	 * @return
	 */
	private KeyInfo getKeyInfo(String key){
		KeyInfo kInfo = keyMap.get(key);
		if (kInfo == null) { // new key
			kInfo = new KeyInfo(key.hashCode() % slaves.size());
			KeyInfo tmpInfo = keyMap.putIfAbsent(key, kInfo);
			if (tmpInfo != null) {
				// another thread just created the key
				kInfo = tmpInfo;
			}
		}
		return kInfo;
	}
	
	private KeyInfo migrateTo(String key, int store) throws RemoteException,
	InterruptedException {
		KeyInfo kInfo = getKeyInfo(key);

		int selectedStore = kInfo.getStore();

		// A partir d'ici on va demander aux esclaves la migration
		if (selectedStore != store) {
			kInfo.getMutex().lockWrite();

			// mise à jour de mes valeurs d'utilisation de store
			// System.out.println("minstore " + minStore + "value"
			// + storeUse.get(minStore));
			storeUse.get(store).addAndGet(kInfo.getUse().get());
			storeUse.get(selectedStore).addAndGet(-kInfo.getUse().get());

			String srcHost = "localhost";
			String srcPort = "500" + selectedStore * 2;

			Node slave = slaves.get(store);
			slave.takeMyData(srcHost, srcPort, key);
			// selectionner un autre store
			System.out.println("je migre de " + selectedStore + " a "
					+ store);
			kInfo.setStore(store);
			kInfo.getMutex().writeToRead();
		} else {
			kInfo.getMutex().lockRead();
		}
		return kInfo;
	}

	private KeyInfo balance(String key) throws RemoteException,
	InterruptedException {
		divideByTwo();
		KeyInfo kInfo = getKeyInfo(key);

		// A partir d'ici on va demander aux esclaves la migration
		if (kInfo.getUse().get() > useMax) {
			Entry<Integer, AtomicInteger> minStore = Collections.min(
					storeUse.entrySet(), new StoreUseComparator());
			migrateTo(key, minStore.getKey());
		}
		return kInfo;
	}

	@Override
	public void multiPut(List<KeyValueVersion> ops)
					throws RemoteException, InterruptedException {
		Node slave;
		if (ops.isEmpty())
				return;
		
		// choisir le plus petit store
		Entry<Integer, AtomicInteger> destStore = Collections.min(
				storeUse.entrySet(), new StoreUseComparator());
		
		// migrer toutes les profils sur ce store
		List<KeyValueVersion> sortedOps = new ArrayList<KeyValueVersion>();
		Collections.copy(sortedOps, ops);
		Collections.sort(sortedOps, ops.get(0));
		for (KeyValueVersion kvv : sortedOps){
			migrateTo(kvv.getKey(), destStore.getKey());
		}

		// effectuer toutes les transactions en appelant le put du store qui va
		// bien
		slave = slaves.get(destStore.getKey());
		for (KeyValueVersion kvv : sortedOps){
			KeyInfo kInfo = getKeyInfo(kvv.getKey());
			incrementUse(kInfo);

			slave.put(kvv.getKey(), kvv.getCat(), kvv.getData());
			kInfo.getMutex().unlockRead();
		}

	}

	@Override
	public Version putifVersion(String key, String cat, byte[] data,
			Version matchVersion) throws RemoteException, InterruptedException {
		Node slave;
		KeyInfo kInfo = balance(key);
		slave = slaves.get(kInfo.getStore());
		System.out.println("selected store " + kInfo.getStore());

		System.out.println("Redirection vers l'esclave " + slave.getName());
		Version returnVersion = slave
				.putifVersion(key, cat, data, matchVersion);
		kInfo.getMutex().unlockRead();
		return returnVersion;
	}

	@Override
	public void put(String key, String cat, byte[] data)
			throws RemoteException, InterruptedException {
		Node slave;
		KeyInfo kInfo = balance(key);
		slave = slaves.get(kInfo.getStore());
		System.out.println("selected store " + kInfo.getStore());

		System.out.println("Redirection vers l'esclave " + slave.getName());
		slave.put(key, cat, data);
		kInfo.getMutex().unlockRead();
	}

	@Override
	public void init(int numNode) throws Exception {
		if (isStarted) {
			return;
		}
		String storeName = "kvstore";
		String hostName = "localhost";
		int hostPort = 5000;

		for (int i = 0; i < nbStores; i++) {
			int port = hostPort + 2 * i;
			storeUse.put(new Integer(i), new AtomicInteger(0));
			System.out.println(port);
			stores.add(KVStoreFactory.getStore(new KVStoreConfig(storeName,
					hostName + ":" + port)));
		}

		for (int j = 1; j < 30; j++) {
			Key k1, compteur;
			int hash = ("P" + j).hashCode() % nbStores;
			keyMap.put("P" + j, new KeyInfo(hash));
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
	public void close() throws IOException {
		if (isStarted) {
			slaves.clear();
			isStarted = false;
		}
	}

	@Override
	public byte[] get(String key, String cat) throws RemoteException,
	InterruptedException {
		System.out.println("dit bonjour tonton" + key);
		KeyInfo kInfo = balance(key);
		byte[] returnValue = slaves.get(kInfo.getStore()).get(key, cat);
		incrementUse(kInfo);
		kInfo.getMutex().unlockRead();
		return returnValue;
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

	@Override
	public void takeMyData(String srcHost, String srcPort, String key)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

}
