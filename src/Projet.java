import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedMap;
import oracle.kv.*;
import oracle.kv.stats.*;

/**
 * TME avec KVStore : Init
 */
public class Projet implements Comparator<ValueVersion>{

	private final KVStore store;

	/**
	 * Runs Init
	 */
	public static void main(String args[]) {
		try {
			Projet a = new Projet(args);
			a.go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Projet() {
		super();
		store = null;
	}

	/**
	 * Parses command line args and opens the KVStore.
	 */
	private Projet(String[] argv) {

		String storeName = "kvstore";
		String hostName = "localhost";
		String hostPort = "5000";

		final int nArgs = argv.length;
		int argc = 0;

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

		store = KVStoreFactory.getStore
				(new KVStoreConfig(storeName, hostName + ":" + hostPort));
	}

	private void usage(String message) {
		System.out.println("\n" + message + "\n");
		System.out.println("usage: " + getClass().getName());
		System.out.println("\t-store <instance name> (default: kvstore) " +
				"-host <host name> (default: localhost) " +
				"-port <port number> (default: 5000)");
		System.exit(1);
	}

	void m1() throws Exception {
		String key = "produit";
		for(int i=0;i<1000;i++){
			for (int j= 1; j<=5; j++){

				Key k = Key.createKey("C1",key+j);
				Version matchVersion;
				Version version = null;
				ValueVersion valVer;
				int val2;

				do {
					valVer = store.get(k);

					String value = new String(valVer.getValue().getValue());
					matchVersion = valVer.getVersion();
					System.out.println("value = "+value+" key = "+k);
					val2 = Integer.parseInt(value);
					val2 ++;
					System.out.println("val2= "+val2);

					value = Integer.toString(val2);

					version = store.putIfVersion(k, Value.createValue(value.getBytes()), matchVersion);
				}while (version == null);

			}
		}
	}



	void m2() throws Exception {
		String key = "produit";

		for(int i=0;i<1000;i++){
			Key k = Key.createKey("C1",key+String.format("%03d", 1));
			Key k2 = Key.createKey("C1");
			Version version = null;
			SortedMap<Key, ValueVersion> sorted;
			int val2;
			String value;
			do {
				Depth depth = Depth.CHILDREN_ONLY;
				KeyRange kr = new KeyRange("produit001",true, "produit005", true);
				sorted = store.multiGet(k2, kr, depth);
				val2 = Integer.parseInt(new String(Collections.max(sorted.values(), new Projet()).getValue().getValue()));
				val2 ++;
				System.out.println("val2= "+val2);
				value = Integer.toString(val2);

				version = store.putIfVersion(k, Value.createValue(value.getBytes()), sorted.get(k).getVersion());

			}while (version == null);
			for (int j= 2; j<=5; j++){
				k = Key.createKey("C1",key+String.format("%03d", j));
				version = store.putIfVersion(k, Value.createValue(value.getBytes()), sorted.get(k).getVersion());
			}



		}
		
	}
	void m3() throws Exception {
		String key = "produit";
		Key k2 = Key.createKey("C1");
		
		OperationFactory of = store.getOperationFactory();
		Depth depth = Depth.CHILDREN_ONLY;
		boolean execOk = false;

		for(int i=0;i<1000;i++){
			Key k;
			SortedMap<Key, ValueVersion> sorted;
			int val2;
			String value;
			
			do {
				ArrayList<Operation> operations = new ArrayList<Operation>();
				KeyRange kr = new KeyRange("produit001",true, "produit005", true);
				sorted = store.multiGet(k2, kr, depth);

				val2 = Integer.parseInt(new String(Collections.max(sorted.values(), new Projet()).getValue().getValue()));
				val2++;
				System.out.println(val2);
				value = Integer.toString(val2);

				for (int j= 1; j<=5; j++){
					k = Key.createKey("C1",key+String.format("%03d", j));
					operations.add(of.createPutIfVersion(k, Value.createValue(value.getBytes()), sorted.get(k).getVersion(), ReturnValueVersion.Choice.NONE, true));
					//version = store.putIfVersion(k, Value.createValue(value.getBytes()), matchVersion);
				}
				try{
					store.execute(operations);
					execOk =true;
				}
				catch(OperationExecutionException oee){
					System.out.println("je t'ai bien niqué");
					execOk = false;
				}
			}while (execOk == false);
			



		}
		
	}

	/**
	 * Initialisation
	 */
	void go() throws Exception {
		System.out.println("Initialisation...");


		m2();
		store.close();
	}

	@Override
	public int compare(ValueVersion arg0, ValueVersion arg1) {
		return Integer.compare(Integer.parseInt(new String(arg0.getValue().getValue())),
				Integer.parseInt(new String(arg1.getValue().getValue())));
	}
}
