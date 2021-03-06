import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Classifier {
	
	// Define a SortedSet with a TreeMap.
	// The comparator defined here orders the elements by value (natural order)
	public static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() {
	            @Override 
	            public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                // Compare two elements e1 and e2
	            	// Return 0 if equal, negative integer if e2 is greater than e1 
	            	// and a positive integer if e1 is greater than e2
	            	int res = e1.getValue().compareTo(e2.getValue());
	                // if keys are equal compare the values
	            	if (e1.getKey().equals(e2.getKey())) {
	                    return res;
	                } 
	            	// else compare the keys and place them in the natural order
	            	else {
	                    return res != 0 ? -res : 1; 
	                }
	            }
	        }
	    );
	    // Add all the elements contained in the map
	    // The comparator defined above place the elements in the natural order (from lower to higher keys)
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	
	public static void afficher_50(Set<Entry<String, Integer>> sortedEntries){
		System.out.println("50 plus grandes occurences de mots dans le texte : ");
		int count = 0;
		for (Entry<String, Integer> entry  : sortedEntries) {
			if (count <= 50){
				System.out.println(entry.getKey());
				count++;
			}
			else {
				break;
			}
		}
	}
	
	public static void afficher(Set<Entry<String, Integer>> sortedEntries){
		System.out.println("Mots dans le texte : occurence");
		for (Entry<String, Integer> entry  : sortedEntries) {
				System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}
	
	/*public static boolean is_special_words(String s){
		String words[] = {"mais","ou","et","donc","or", "ni","car"
				,"je","tu", "il", "elle","nous", "vous", "ils", "elles","un","une","ce","cette"
				,"le","la","lui","les","nous","vous","leur","eux"
				,"celui","celle","celui-ci","celui-là","celle-ci","celle-là","ceci","cela", "ça"
				,"ceux","ceux-ci","ceux-là","celles-ci","celles-là"
				,"à","après","avant","avec","chez","concernant","contre","dans","de","depuis" 
				,"derrière","dès","devant","durant","en","entre","envers","hormis","hors","jusque"
				,"malgré","moyennant","nonobstant","outre","sur","parmi","pendant","pour","près"
				,"sans","sauf","selon","sous","suivant","par","touchant","vers","via","dehors"
				,"des","du","au","aux","que","qui","être","peut","ne","ces","pas","qu","ainsi","lorsque"
				,"ses","dont","tout","sa","si","i","ii","iii","son","sont","est","soit","fait"
				,"peuvent","toute","ier","leurs","autre","même","se","autres","deux","ont","chaque"
				,"non","été"};
		for (int i = 0; i < words.length;i++){
			if (s.equals(words[i])){
				return false;
			}
		}
		return true;
	}*/
	
	public static void main (String args[]) throws IOException{
		long start_time = System.nanoTime();
		BufferedReader br = new BufferedReader(new FileReader("/home/amathieu/workspace/SLR207/src/CC-MAIN-20170322212949-00140-ip-10-233-31-227.ec2.internal.warc.wet"));
		HashMap<String,Integer> cmap = new HashMap<String,Integer>();
		//ConcurrentHashMap<String,Integer> cmap = new ConcurrentHashMap<String,Integer>();
		ArrayList<CountingThread> array = new ArrayList<CountingThread>();
		for (int i = 0; i < 8; i++){
			array.add(new CountingThread(br,cmap));
			array.get(array.size()-1).start();
		}
		try {
			for (int i = 0; i < 8; i++){
				array.get(i).join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		br.close();
		Set<Map.Entry<String,Integer>> sortedEntries = entriesSortedByValues(cmap);
		long end_time = System.nanoTime();
		afficher_50(sortedEntries);
		//System.out.println("Liste des mots avec occurences : " + sortedEntries);
		//afficher(sortedEntries);
		double difference = (end_time - start_time)/1e9;
		System.out.println("Temps d'éxécution en secondes : " + difference);
	}
}
