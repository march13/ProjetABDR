package org.abdr.client;

import java.util.ArrayList;



public class Client {

	public Client() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		/*String name;
		int nbThreads = 2;
		if (!args[1].toString().equals(null))
			 name = "client"+args[1].toString();
		else
			return;
		Node node = new ClientNode(name);
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind(name, node);
		node.init(new Integer(args[1].toString()));
		System.out.println("Le noeud client a démarré");
		MasterNode master = (MasterNode) registry.lookup("master");
		master.addClient("localhost", 1099, name);
		KeyObject obj = new KeyObject(0, 1, 2, 3, 4, "blall", "jkhc",
				"jkhqskl", "khhkzejh", "lkjlmj");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(obj);
		byte[] myBytes = bos.toByteArray();
		for(int i = 0; i<30; i++)
			master.put("P1", "43"+i,myBytes);
		byte[] myReturn = master.get("P1", "431");
		
		
		ByteArrayInputStream bis = new ByteArrayInputStream(myReturn);
		ObjectInput in = null;
		try {
		  in = new ObjectInputStream(bis);
		  KeyObject o = (KeyObject) in.readObject(); 
		  System.out.println(o.getTxt1());
		} finally {
		  try {
		    bis.close();
		  } catch (IOException ex) {
		    // ignore close exception
		  }
		  try {
		    if (in != null) {
		      in.close();
		    }
		  } catch (IOException ex) {
		    // ignore close exception
		  }
		}
		
		ArrayList<String> multiKeys = new ArrayList<String>();
		ArrayList<String> multiCats = new ArrayList<String>();
		ArrayList<byte[]> multiDatas = new ArrayList<byte[]>();
		for (int i=1; i<5; i++){
			multiCats.add("test2");
			multiDatas.add(myBytes);
			multiKeys.add("P"+1);
			
		}
		//master.multiPut(multiKeys, multiCats, multiDatas, null);
		master.deleteClient(name);
		node.close();
		registry.unbind(name);
		System.exit(0);*/
		try {
			int nbThreads = 5;
			
			ArrayList<Thread> threadList = new ArrayList<Thread>();
			for (int j = 0; j<nbThreads; j++){
				threadList.add(new Thread(new ClientThread(j)));
				threadList.get(j).start();
				
			}
			for (int j=0; j<nbThreads; j++){
				threadList.get(j).join();
			}

		
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Fini.");
		
	}

}
