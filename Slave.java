import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Slave {
	
	public static boolean contains_special_character(String s){
		CharSequence seq = "0123456789=\"”€↬³°";
		for (int i = 0; i < seq.length(); i++){
			if (s.indexOf(seq.charAt(i)) != -1){
				return true;
			}
		}
		return false;
	}
	
	public static String getHostName() throws IOException{
		ProcessBuilder pb = new ProcessBuilder("hostname");
		Process process = pb.start();
		InputStream stdout = process.getInputStream ();
		BufferedReader out_out = new BufferedReader(new InputStreamReader (stdout));
		String line;
		while ((line = out_out.readLine()) == null);
		return line;
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		//Thread.sleep(3000);
		String arg = args[0]; 
		String path = "/tmp/amathieu/";
		String hostname = getHostName();
		char number = arg.charAt(1);
		BufferedReader br = new BufferedReader(new FileReader(path+"splits/"+arg));
		PrintWriter pw = new PrintWriter(path+"maps/"+"UM"+number+" - "+hostname+".txt");
		String line = br.readLine();
		while(line != null){
			String[] words = line.split("[ \\.\\(\\)\\'\\;\\:\\-\\/\\,\\?\\!\\*\\<\\>]");
			for (int i = 0; i < words.length; i++){
				String word = words[i].toLowerCase();
				if (!contains_special_character(word) && !((word.length()) <= 1)){
					pw.println(word+" 1");
					pw.flush();
				}
			}
			line = br.readLine();
		}
		br.close();
		pw.close();
	}
}
