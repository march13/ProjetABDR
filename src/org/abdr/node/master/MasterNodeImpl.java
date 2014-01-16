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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.Version;

import org.abdr.client.KeyObject;
import org.abdr.node.AbstractNode;
import org.abdr.node.Node;

public class MasterNodeImpl extends AbstractNode implements MasterNode {

	private static final long serialVersionUID = 1L;
	private List<Node> slaves;
	private List<Node> clients;
	Map<String, Integer> keyMap;
	Map<String, Integer> useMap;
	Map<Integer, Integer> storeUse;
	Map<String, Mutex> mutexes;
	int useMax = 1000;
	int totalUse = 0;
	Mutex verrou;
	public MasterNodeImpl() throws RemoteException {
		super("master");
	}
	
	public synchronized void divideByTwo(){
		if (totalUse>=1000){
			for (Map.Entry<String, Integer> entry : useMap.entrySet()){
				useMap.put(entry.getKey(), entry.getValue()/2);
			}
			for (Map.Entry<Integer, Integer> entry : storeUse.entrySet()){
				storeUse.put(entry.getKey(), entry.getValue()/2);
			}
			totalUse = 0;
			
		}
	}
	public synchronized int migrate(String key)
			throws RemoteException, InterruptedException {
		divideByTwo();
		verrou.lockWrite();
		Integer selectedStore = keyMap.get(key);
		if (selectedStore.equals(null)) {
			mutexes.put(key, new Mutex());
			selectedStore = key.hashCode() % slaves.size();
			keyMap.put(key, selectedStore);
			useMap.put(key, 1);
			
			storeUse.put(selectedStore, storeUse.get(selectedStore) + 1);
			verrou.unlockWrite();
		} else {
			verrou.unlockWrite();
			Integer newUseVal = useMap.get(key) + 1;
			useMap.put(key, newUseVal);
			storeUse.put(selectedStore, storeUse.get(selectedStore) + 1);
			// A partir d'ici on va demander aux esclaves la migration
			if ((useMap.get(key).intValue()) % useMax == 0) {
				mutexes.get(key).lockWrite();
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
				String srcHost = "localhost";
				String srcPort = "500" + selectedStore * 2;
				Node slave = slaves.get(minStore);
				slave.takeMyData(srcHost, srcPort, key);
				// selectionner un autre store
				System.out.println("je migre de " + selectedStore + " a "
						+ minStore);
				keyMap.put(key, minStore);
				selectedStore = minStore;
				mutexes.get(key).writeToRead();
			}
			else {
				mutexes.get(key).lockRead();
			}
		}
		return selectedStore;
	}

	@Override
	public void multiPut(ArrayList<String> keys, ArrayList<String> cats,
			ArrayList<byte[]> datas, ArrayList<Version> matchVersions)
			throws RemoteException, InterruptedException {
		Node slave;
		Integer selectedStore = 0;
		int destStore;
		// choisir le plus petit store
		destStore = Collections.min(storeUse.values());
		// migrer toutes les profils sur ce store store
		for (int i = 0; i < keys.size(); i++) {
			selectedStore = keyMap.get(keys.get(i));
			if (selectedStore.equals(null)) {
				selectedStore = destStore;
				keyMap.put(keys.get(i), selectedStore);
				useMap.put(keys.get(i), 1);
				mutexes.put(keys.get(i), new Mutex());
				storeUse.put(selectedStore, storeUse.get(selectedStore) + 1);
			}
			
			//je suis deja sur le bon store
			else if(selectedStore.equals(new Integer(destStore))){
			} 
			else {
				String srcHost = "localhost";
				String srcPort = "500" + selectedStore * 2;
				slave = slaves.get(destStore);
				
				if (i==0 || (!keys.get(i - 1).equals(null)) ) {
					if (i==0 || (!keys.get(i - 1).equals(i))) {
						mutexes.get(keys.get(i)).lockWrite();
						keyMap.put(keys.get(i), destStore);
						storeUse.put(selectedStore, storeUse.get(selectedStore)
								- useMap.get(keys.get(i)));
						storeUse.put(destStore, storeUse.get(destStore)
								+ useMap.get(keys.get(i)));
						slave.takeMyData(srcHost, srcPort, keys.get(i));
						mutexes.get(keys.get(i)).writeToRead();
						System.out.println("migration de "+selectedStore+" vers "+destStore);
					}
				}
			}
		}

		// effectuer toutes les transactions en appelant le put du store qui va
		// bien
		slave = slaves.get(destStore);
		for (int i = 0; i < keys.size(); i++) {
			int newVal;
			newVal = useMap.get(keys.get(i)) + 1;
			
			useMap.put(keys.get(i), newVal);
			newVal = storeUse.get(destStore) + 1;
			storeUse.put(selectedStore, newVal);
			slave.put(keys.get(i), cats.get(i), datas.get(i));
			mutexes.get(keys.get(i)).unlockRead();
		}

	}

	@Override
	public Version putifVersion(String key, String cat, byte[] data,
			Version matchVersion) throws RemoteException, InterruptedException {
		Node slave;
		int selectedStore = migrate(key);
		slave = slaves.get(selectedStore);
		System.out.println("selected store " + selectedStore);

		System.out.println("Redirection vers l'esclave " + slave.getName());
		Version returnVersion = slave.putifVersion(key, cat, data, matchVersion);
		mutexes.get(key).unlockRead();
		return returnVersion;
	}

	@Override
	public void put(String key, String cat, byte[] data) throws RemoteException, InterruptedException {
		Node slave;
		int selectedStore = migrate(key);
		slave = slaves.get(selectedStore);
		System.out.println("selected store " + selectedStore);

		System.out.println("Redirection vers l'esclave " + slave.getName());
		slave.put(key, cat, data);
		mutexes.get(key).unlockRead();
	}

	@Override
	public synchronized void init(int numNode) throws Exception {
		if (isStarted) {
			return;
		}
		int nbStores = 3;
		verrou = new Mutex();
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
		mutexes = new HashMap<String, Mutex>();
		for (int j = 1; j < 30; j++) {
			Key k1, compteur;
			int hash = ("P" + j).hashCode() % nbStores;
			keyMap.put("P" + j, new Integer(hash));
			useMap.put("P" + j, new Integer(0));
			mutexes.put("P"+j, new Mutex());
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
	public byte[] get(String key, String cat) throws RemoteException, InterruptedException {
		System.out.println("dit bonjour tonton" + key);
		int selectedStore = migrate(key);
		mutexes.get(key).lockRead();
		byte[]returnValue = slaves.get(selectedStore).get(key, cat);
		mutexes.get(key).unlockRead();
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
