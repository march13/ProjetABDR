import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import oracle.kv.*;

/**
 * TME avec KVStore : Init
 */
public class Init{

    private final KVStore store;
    private final KVStore store2;


    /**
     * Parses command line args and opens the KVStore.
     */
    public Init(String[] argv) {

    	
    	//TODO gerer plus de stores
        String storeName = "kvstore";
        String hostName = "localhost";
        String hostPort = "5000";
        String hostPort2 = "5002";
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
        store2 = KVStoreFactory.getStore
                (new KVStoreConfig(storeName, hostName + ":" + hostPort2));
    }

    private void usage(String message) {
        System.out.println("\n" + message + "\n");
        System.out.println("usage: " + getClass().getName());
        System.out.println("\t-store <instance name> (default: kvstore) " +
                           "-host <host name> (default: localhost) " +
                           "-port <port number> (default: 5000)");
        System.exit(1);
    }

    /**
     * Initialisation
     */
    void go() throws Exception {
        System.out.println("Initialisation...");

	    for (int j= 1; j<11; j++){
        	Key k1;
        	for(int i=1; i<101; i++){
        		//je crée l'objet avec ses attributs puis le sérialize
	        	KeyObject obj = new KeyObject(0, 1, 2, 3, 4, "blall", "jkhc", "jkhqskl", "khhkzejh", "lkjlmj");
	        	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        	ObjectOutput out = null;
	        	try {
	        	  out = new ObjectOutputStream(bos);   
	        	  out.writeObject(obj);
	        	  byte[] myBytes = bos.toByteArray();
	        	  k1 = Key.createKey("P" + j, "O" + i);
	        	  //TODO gerer le hash en fonction du nombre de stores
	        	 //int hash = k1.hashCode()%2;
	        	  //pour le premier exo j'utilise qu'un store
	        	  int hash = 0;
	        	  if (hash ==0){
	        		  store.put(k1, Value.createValue(myBytes));
	        	  }
	        	  else{
	        		  store2.put(k1, Value.createValue(myBytes));
	        	  }
	              store.put(k1, Value.createValue(myBytes));
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
        }
        System.out.println("Fin d'initialisation");


        store.close();
    }
}
