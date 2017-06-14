import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

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
				t_array.add(new Thread_IP(ip,splits.get(count),0,null));
				t_array.add(new Thread_IP(ip,"/home/amathieu/workspace/SLR207/src/slave.jar",1,null));
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
				t_array.add(new Thread_IP(ip,null,2,null));
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
				t_array.add(new Thread_IP(ip,arg,3,map));
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
		//System.out.println(cmap);
		//System.out.println(map);
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
					String arg1 = src+":"+remote_path+entry.getValue().get(i)+"\\ -\\ "+hostname;
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
					ProcessBuilder pb = new ProcessBuilder("scp","-pr",arg1,arg2);
					//System.out.println("ssh "+src+" scp -pr "+arg1+" "+arg2);
					Process process;
					try {
						process = pb.start();
						InputStream stderr = process.getErrorStream ();
						InputStream stdout = process.getInputStream ();
						BufferedReader error_out = new BufferedReader (new InputStreamReader(stderr));
						BufferedReader out_out = new BufferedReader(new InputStreamReader (stdout));
						ArrayBlockingQueue<String> array = new ArrayBlockingQueue<String>(50);
						Print_Output p_err = new Print_Output (error_out,array,"err");
						Print_Output p_out = new Print_Output (out_out,array,"out");
						p_err.start();
						p_out.start();		
						String line;
						try {	
							while (p_err.getState() != Thread.State.TERMINATED || p_out.getState() != Thread.State.TERMINATED){
									line = array.poll(1,TimeUnit.SECONDS);
									//System.out.println(p_err.getState() +" " + p_out.getState());
									if (line != null){
										System.out.println(line);
									}
									else if (p_err.getState() == Thread.State.RUNNABLE && p_out.getState() == Thread.State.RUNNABLE){
										break;
									}
									else {
										if (p_err.getState() != Thread.State.TERMINATED || p_out.getState() != Thread.State.TERMINATED)
											System.out.println("Timeout");
											p_err.interrupt();
											p_out.interrupt();
											break;
									}
									
									//PrintStream out = new PrintStream(process.getOutputStream());
									//out.println("hostname");
									//Map<String, String> environment = processBuilder.environment();
									//environment.put("name", "Alfredo Osorio");
									/*Process p = processBuilder.start();
									String ligne;
									BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
									while ((ligne = r.readLine()) != null) {
										System.out.println(ligne);
									}
									r.close();*/
									//	System.out.println(env);
								}
							//p_err.join();
							//p_out.join();
							error_out.close();
							out_out.close();
						}
						catch (InterruptedException e){
							e.printStackTrace();
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		//System.out.println(key_file);
		//System.out.println(cmap);
		//System.out.println(map);
	}
	
	public HashMap<String,String> reduce() throws IOException {
		HashMap<String,String> rm_host = new HashMap<String,String> ();
		ArrayList<Thread_IP> array = new ArrayList<Thread_IP>();
		for (Entry<String,ArrayList<String>> entry : key_file.entrySet()){
			String[] splits = entry.getValue().get(0).split("[ \\.]");
			String ip = splits[splits.length-2];
			rm_host.put(entry.getKey(), ip);
			//System.out.println(ip);
			array.add(new Thread_IP(ip,entry.getKey(),4,key_file));
			array.get(array.size()-1).start();
		}
		try {
			for (int i = 0; i < array.size(); i++){
				array.get(i).join();
			}	
		}
		catch (Exception e){
			
		}
		System.out.println("RM créés");
		this.map = rm_host;
		return rm_host;
	}
	
	public void printResult() throws IOException {
		ArrayList<Thread_IP> array = new ArrayList<Thread_IP>();
		for (Entry<String,String> entry : map.entrySet()){
			String arg = "RM_" + entry.getKey() + ".txt";
			array.add(new Thread_IP(entry.getValue(),arg,5,null));
			array.get(array.size()-1).start();
		}
		try {
			for (int i = 0; i < array.size(); i++){
				array.get(i).join();
			}	
		}
		catch (Exception e){
			
		}
	}
	
}
