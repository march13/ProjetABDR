import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Timer;

import com.sleepycat.je.log.Trace;

import oracle.kv.*;

/**
 * TME avec KVStore : Init
 */
public class AppliOneStore implements Comparator<ValueVersion>, Runnable {

	private final KVStore store;
	public int numThread;
	public float myTime;

	/**
	 * Runs Init
	 */

	public AppliOneStore() {
		super();
		store = null;
		myTime = 0;
	}

	/**
	 * Parses command line args and opens the KVStore.
	 */
	public AppliOneStore(String[] argv, int numThread) {

		String storeName = "kvstore";
		String hostName = "localhost";
		String hostPort = "5000";

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
			ObjectOutput out = null;
			Key k1;
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(obj);
				byte[] myBytes = bos.toByteArray();
				k1 = Key.createKey("P1", "O" + (j));
				// TODO gerer le hash en fonction du nombre de stores
				// int hash = k1.hashCode()%2;
				// pour le premier exo j'utilise qu'un store
				Version matchVersion;
				Version version = null;
				ValueVersion valVer;

				do {
					valVer = store.get(k1);
					// store.put(k1, Value.createValue(myBytes));
					matchVersion = valVer.getVersion();
					version = store.putIfVersion(k1,
							Value.createValue(myBytes), matchVersion);
				} while (version == null);
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
		myTime = (time - start) / 100;
	}

	/**
	 * Initialisation
	 */

	@Override
	public int compare(ValueVersion arg0, ValueVersion arg1) {
		return Integer.compare(
				Integer.parseInt(new String(arg0.getValue().getValue())),
				Integer.parseInt(new String(arg1.getValue().getValue())));
	}

	@Override
	public void run() {
		Interruptor inter = new Interruptor(Thread.currentThread());
		Timer tim = new Timer();
		long i = 0;
		float finalTime = 0;

		try {
			tim.schedule(inter, 1000);
			while (!Thread.currentThread().isInterrupted()) {
				i++;
				m1();
				finalTime += myTime;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		finalTime = finalTime / i;
		store.close();
		System.out.println("fini" + numThread + "temps moyen " + finalTime);
	}
}
