import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

public class CountingThread extends Thread{
	
	private BufferedReader br;
	private HashMap<String,Integer> cmap;
	
	public CountingThread(BufferedReader br, HashMap<String,Integer> cmap){
		this.br = br;
		this.cmap = cmap;
	}
	
	public static boolean contains_special_character(String s){
		CharSequence seq = "0123456789-/:;;-<=\",”€↬³°";
		for (int i = 0; i < seq.length(); i++){
			if (s.indexOf(seq.charAt(i)) != -1){
				return true;
			}
		}
		return false;
	}
	
	@Override 
	public void run(){
		try {
			String line;
			synchronized (br){
				line = br.readLine();	
			}
			while(line != null){
				String[] words = line.split("[ \\.\\(\\)\\'\\;\\:\\-\\/\\,\\?\\!\\*\\<\\>]");
				for (int i = 0; i < words.length; i++){
					String word = words[i].toLowerCase();
					if (cmap.containsKey(word)){
						synchronized(cmap){
							cmap.put(word,cmap.get(word)+1);
						}
					}
					else if (!contains_special_character(word) && !(word.length() <= 1)){
						synchronized (cmap){
							cmap.put(word,1);
						}
					}
				}
				synchronized (br){
					line = br.readLine();		
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
