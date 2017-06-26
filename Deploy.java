import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Deploy {

	String fileName;
	ArrayList<String> splits;
	HashMap<String,String> map;
	HashMap<String,ArrayList<String>> cmap = null;
	HashMap<String,ArrayList<String>> key_file = new HashMap<String,ArrayList<String>> ();
	
	public Deploy (String fileName, ArrayList<String> splits){
		this.fileName = fileName;
		this.splits = splits;
		map = new HashMap<String,String>();
	}
	
	public void sendSplits () throws IOException{
		ArrayList<Thread_IP> t_array = new ArrayList<Thread_IP> ();
		BufferedReader br = new BufferedReader(
									new FileReader(
											new File(fileName)));
		String ip;
		int count = 0;
		int length = splits.size();
		try{
			while ((ip = br.readLine()) != null && count < length){
				t_array.add(new Thread_IP(ip,splits.get(count),0,null,null));
				t_array.add(new Thread_IP(ip,"/home/amathieu/workspace/SLR207/src/slave.jar",1,null,null));
				t_array.get(t_array.size()-1).start();
				t_array.get(t_array.size()-2).start();
				String mapping = "UM"+Integer.valueOf(count+1);
				if (!map.containsKey(mapping)){
					map.put(mapping, ip);
				}
				else {
					map.put(mapping,ip);
				}
				count++;
			}
			for (int i = 0; i < t_array.size(); i++){
				t_array.get(i).join();
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		br.close();
	}
	
	public void createDirectories () throws IOException{
		ArrayList<Thread_IP> t_array = new ArrayList<Thread_IP> ();
		BufferedReader br = new BufferedReader(
									new FileReader(
											new File(fileName)));
		String ip;
		try{
			while ((ip = br.readLine()) != null){
				t_array.add(new Thread_IP(ip,null,2,null,null));
				t_array.get(t_array.size()-1).start();
			}
			for (int i = 0; i < t_array.size(); i++){
				t_array.get(i).join();
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		br.close();
	}
	
	public HashMap<String,ArrayList<String>> launchSlave() throws IOException{
		HashMap<String,ArrayList<String>> map = new HashMap<String,ArrayList<String>> ();
		ArrayList<Thread_IP> t_array = new ArrayList<Thread_IP> ();
		BufferedReader br = new BufferedReader(
									new FileReader(
											new File(fileName)));
		String ip;
		int count = 0;
		try{
			while ((ip = br.readLine()) != null && count < splits.size()){
				String[] split = splits.get(count).split("[\\/]");
				char number = split[split.length-1].charAt(1);
				//String arg = "S"+String.valueOf(map.get(ip))+".txt";
				String arg = "S"+number+".txt";
				//System.out.println("Value of split : "+splits+" Value extracted : "+split[split.length-1]+" number extracted : "+number);
				//System.out.println(ip + " " + arg);
				t_array.add(new Thread_IP(ip,arg,3,map,null));
				t_array.get(t_array.size()-1).start();
				count++;
			}
			for (int i = 0; i < t_array.size(); i++){
				t_array.get(i).join();
			}
			System.out.println("Phase de map terminée !");
		}
		catch (Exception e){
			e.printStackTrace();
		}
		br.close();
		this.cmap = map;
		return map;
	}
	
	public void shuffle() throws IOException {
		String remote_path = "/tmp/amathieu/maps/";
		ArrayList<Thread_IP> array = new ArrayList<Thread_IP>();
		for (Entry<String,ArrayList<String>> entry : cmap.entrySet()) {
			int size = entry.getValue().size();
			if (size > 1){
				for (int i = 1; i < size; i++){
					// Ordinateur à qui envoyer le fichier
					String dest = map.get(entry.getValue().get(0));
					// Ordinateur d'où le fichier provient
					String src = map.get(entry.getValue().get(i));
					String[] txtFile = map.get(entry.getValue().get(i)).split("[\\.]");
					String hostname = txtFile[0]+".txt";
					String arg1 = remote_path+entry.getValue().get(i)+"\\ -\\ "+hostname;
					String arg2 = dest+":"+remote_path;
					String file = entry.getValue().get(i)+"\\ -\\ "+hostname;
					if (!key_file.containsKey(entry.getKey())){
						ArrayList<String> files = new ArrayList<String> ();
						String[] splits = dest.split("[\\.]");
						files.add(entry.getValue().get(0)+"\\ -\\ "+splits[0]+".txt");
						files.add(file);
						key_file.put(entry.getKey(),files);
					}
					else {
						ArrayList<String> files = key_file.get(entry.getKey());
						files.add(file);
						key_file.put(entry.getKey(),files);
					}
					String arg = "scp -pr "+arg1+" "+arg2;
					array.add(new Thread_IP(src,arg,6,null,null));
					array.get(array.size()-1).start();	
				}
			}
			else {
				String[] txtFile = map.get(entry.getValue().get(0)).split("[\\.]");
				String hostname = txtFile[0]+".txt";
				String file = entry.getValue().get(0)+"\\ -\\ "+hostname;				
				ArrayList<String> files = new ArrayList<String>();
				files.add(file);
				key_file.put(entry.getKey(),files);
			}
		}
		for (int i = 0; i < array.size();i++){
			try {
				array.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public HashMap<String,String> reduce() throws IOException {
		HashMap<String,String> rm_host = new HashMap<String,String> ();
		ArrayList<Thread_IP> array = new ArrayList<Thread_IP>();
		for (Entry<String,ArrayList<String>> entry : key_file.entrySet()){
			String[] splits = entry.getValue().get(0).split("[ \\.]");
			String ip = splits[splits.length-2];
			rm_host.put(entry.getKey(), ip);
			array.add(new Thread_IP(ip,entry.getKey(),4,key_file,null));
			array.get(array.size()-1).start();
		}
		try {
			for (int i = 0; i < array.size(); i++){
				array.get(i).join();
			}	
		}
		catch (Exception e){
			e.printStackTrace();
		}
		System.out.println("RM créés");
		this.map = rm_host;
		return rm_host;
	}
	
	public void printResult() throws IOException {
		ArrayList<Thread_IP> array = new ArrayList<Thread_IP>();
		for (Entry<String,String> entry : map.entrySet()){
			String arg = "RM_" + entry.getKey() + ".txt";
			array.add(new Thread_IP(entry.getValue(),arg,5,null,null));
			array.get(array.size()-1).start();
		}
		try {
			for (int i = 0; i < array.size(); i++){
				array.get(i).join();
			}	
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}
