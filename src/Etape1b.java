import java.util.ArrayList;




public class Etape1b {

	/**
	 * @param args
	 */
	
	
	public static void main(String[] args) {
		//Pour taper en même temps sur le store je crée plusieurs threads qui utilisent le timer
		try {
			int nbThreads = 5;
			int nbStores = 2;
			Init i = new Init(args, nbStores);
			i.go();
			
			ArrayList<Thread> threadList = new ArrayList<Thread>();
			for (int j = 0; j<nbThreads; j++){
				threadList.add(new Thread(new AppliTwoStores(args, j+1)));
				threadList.get(j).start();
				
			}
			for (int j = 5; j<2*nbThreads; j++){
				threadList.add(new Thread(new AppliTwoStores(args, j+1)));
				threadList.get(j).start();
				
			}
			for (int j=0; j<nbThreads*2; j++){
				threadList.get(j).join();
			}

		
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Fini.");
		
	}

}
