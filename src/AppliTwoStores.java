import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Timer;


import oracle.kv.*;

/**
 * TME avec KVStore : Init
 */
public class AppliTwoStores implements Runnable {

	private final KVStore store, store2;
	public int numThread;
	public float myTime;

	/**
	 * Runs Init
	 */

	public AppliTwoStores() {
		super();
		store = null;
		store2 = null;
		myTime = 0;
	}

	/**
	 * Parses command line args and opens the KVStore.
	 */
	public AppliTwoStores(String[] argv, int numThread) {

		String storeName = "kvstore";
		String hostName = "localhost";
		String hostPort = "5000";
		String hostPort2 = "5002";
		final int nArgs = argv.length;
		int argc = 0;
		this.numThread = numThread;
		while (argc < nArgs) {
			final String thisArg = argv[argc++];

			if (thisArg.equals("-store")) {
				if (argc < nArgs) {
					storeName = argv[argc++];
				} else {
					usage("-store requires an argument");
				}
			} else if (thisArg.equals("-host")) {
				if (argc < nArgs) {
					hostName = argv[argc++];
				} else {
					usage("-host requires an argument");
				}
			} else if (thisArg.equals("-port")) {
				if (argc < nArgs) {
					hostPort = argv[argc++];
				} else {
					usage("-port requires an argument");
				}
			} else {
				usage("Unknown argument: " + thisArg);
			}
		}

		store = KVStoreFactory.getStore(new KVStoreConfig(storeName, hostName
				+ ":" + hostPort));
		store2 = KVStoreFactory.getStore(new KVStoreConfig(storeName, hostName
				+ ":" + hostPort2));
	}

	private void usage(String message) {
		System.out.println("\n" + message + "\n");
		System.out.println("usage: " + getClass().getName());
		System.out.println("\t-store <instance name> (default: kvstore) "
				+ "-host <host name> (default: localhost) "
				+ "-port <port number> (default: 5000)");
		System.exit(1);
	}

	void m1() throws Exception {
		long start = System.currentTimeMillis();
		long time;
		for (int j = 1; j <= 100; j++) {
			// dans ce scénario je tape sur les 10 premiers produits
			// et une transaction ajoute 100 nouveaux objets
			KeyObject obj = new KeyObject(0, 1, 2, 3, 4, "blall", "jkhc",
					"jkhqskl", "khhkzejh", "lkjlmj");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(obj);
			byte[] myBytes = bos.toByteArray();
			
			Key k1, compteur;
			compteur = Key.createKey("P1", "compteur");
			OperationFactory of = store.getOperationFactory();
			boolean execOk;

			ValueVersion valVer;
			Version matchVersion;
			int val2;
			
			try {
				valVer = store.get(compteur);
				
				String value = new String(valVer.getValue().getValue());
				matchVersion = valVer.getVersion();
				OperationResult opRes;
				do {
					ArrayList<Operation> operations = new ArrayList<Operation>();

					val2 = Integer.parseInt(value);
					val2++;
					k1 = Key.createKey("P1", "O" + val2);
					value = Integer.toString(val2);
					operations.add(of.createPutIfVersion(compteur,
							Value.createValue(value.getBytes()), matchVersion,
							ReturnValueVersion.Choice.ALL, true));
					operations.add(of.createPut(k1, Value.createValue(myBytes),
							ReturnValueVersion.Choice.NONE, true));

					try{
						store.execute(operations).get(0);
						execOk =true;
					}
					catch(OperationExecutionException oee){
						opRes = oee.getFailedOperationResult();
						value = new String(opRes.getPreviousValue().getValue());
						matchVersion = opRes.getPreviousVersion();

						execOk = false;
					}
				} while (!execOk);

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
		time = System.currentTimeMillis();
		myTime = (time - start);
	}
	void m2() throws Exception {
		long start = System.currentTimeMillis();
		long time;
		for (int j = 1; j <= 100; j++) {
			// dans ce scénario je tape sur les 10 premiers produits
			// et une transaction ajoute 100 nouveaux objets
			KeyObject obj = new KeyObject(0, 1, 2, 3, 4, "blall", "jkhc",
					"jkhqskl", "khhkzejh", "lkjlmj");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(obj);
			byte[] myBytes = bos.toByteArray();
			
			Key k1, compteur;
			compteur = Key.createKey("P3", "compteur");
			OperationFactory of = store2.getOperationFactory();
			boolean execOk;

			ValueVersion valVer;
			Version matchVersion;
			int val2;
			
			try {
				valVer = store2.get(compteur);
				
				String value = new String(valVer.getValue().getValue());
				matchVersion = valVer.getVersion();
				OperationResult opRes;
				do {
					ArrayList<Operation> operations = new ArrayList<Operation>();

					val2 = Integer.parseInt(value);
					val2++;
					k1 = Key.createKey("P3", "O" + val2);
					value = Integer.toString(val2);
					operations.add(of.createPutIfVersion(compteur,
							Value.createValue(value.getBytes()), matchVersion,
							ReturnValueVersion.Choice.ALL, true));
					operations.add(of.createPut(k1, Value.createValue(myBytes),
							ReturnValueVersion.Choice.NONE, true));

					try{
						store2.execute(operations).get(0);
						execOk =true;
					}
					catch(OperationExecutionException oee){
						opRes = oee.getFailedOperationResult();
						value = new String(opRes.getPreviousValue().getValue());
						matchVersion = opRes.getPreviousVersion();

						execOk = false;
					}
				} while (!execOk);

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
		time = System.currentTimeMillis();
		myTime = (time - start);
	}

	/**
	 * Initialisation
	 */

	@Override
	public void run() {
		Interruptor inter = new Interruptor(Thread.currentThread());
		Timer tim = new Timer();
		int i = 0;
		float finalTime = 0;

		try {
			tim.schedule(inter, 10000);
			while (!Thread.currentThread().isInterrupted()) {
				i++;
				if (numThread<6)
					m1();
				else 
					m2();
				finalTime += myTime;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		finalTime = finalTime / i;
		store.close();
		tim.cancel();
		System.out.println("Tread n°" + numThread + " temps moyen: "
				+ finalTime);
	}
}
