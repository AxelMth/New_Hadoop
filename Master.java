import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Master {

	public static void main(String[] args) throws IOException {
		String path = "/home/amathieu/workspace/SLR207/src/";
		String fileName = path + "ip.txt";
		ArrayList<String> splits = new ArrayList<String> ();
		System.out.println("Veuillez enter le nombre de splits");
		Scanner scan = new Scanner (System.in);
		int i = scan.nextInt();
		for(int j = 0; j < i; j++){
			splits.add(path+"S"+String.valueOf(j+1)+".txt");
		}
		scan.close();
		// Envoie les splits aux slaves
		Deploy deploy = new Deploy(fileName,splits);
		System.out.println("Début du lancement de l'algorithme");
		deploy.createDirectories();
		System.out.println("Répertoires créés");
		deploy.sendSplits();
		System.out.println("Splits envoyés");
		deploy.launchSlave();
		System.out.println("Jars lancés");
		System.out.println("Shuffle !");
		deploy.shuffle();
		System.out.println("Reduce !");
		deploy.reduce();
		deploy.printResult();
		int count = Thread_IP.count;
		int total = Thread_IP.total_thread_number;
		System.out.println(count/total+"% of thread exiting with success");
	}
}
