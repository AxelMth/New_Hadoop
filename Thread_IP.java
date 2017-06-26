import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Thread_IP extends Thread {
	
	static int total_thread_number = 0;
	static int count = 0;
	String ip;
	String split;
	int mode;
	HashMap<String,ArrayList<String>> map;
	ArrayList<String> array;
	
	public Thread_IP (String ip, String split, int mode, HashMap<String,ArrayList<String>> map,ArrayList<String> array){
		this.ip = ip;
		this.split = split;
		this.mode = mode;
		this.map = map;
		this.array = array;
	}
	
	@Override
	public void run(){
		total_thread_number++;
		ProcessBuilder pb;
		if (mode == 0){
			ip += ":/tmp/amathieu/splits";
			pb = new ProcessBuilder("scp","-pr",split,ip);	
		}
		else if (mode == 1){
			ip += ":/tmp/amathieu";
			pb = new ProcessBuilder("scp","-pr",split,ip);
		}
		else if (mode == 2){
			pb = new ProcessBuilder("ssh",ip,"rm -fr /tmp/amathieu; mkdir /tmp/amathieu/ /tmp/amathieu/splits/ /tmp/amathieu/maps/ ");
		}
		else if (mode == 3){
			String launch = "java -jar /tmp/amathieu/slave.jar 0 "+split;
			pb = new ProcessBuilder("ssh",ip,launch);
		}
		else if (mode == 4){
			ArrayList<String> args = new ArrayList<String> ();
			args.add("ssh");
			args.add(ip);
			String arg = "java -jar /tmp/amathieu/slave.jar 1 "+split;
			ArrayList<String> files = map.get(split);
			for (int i = 0; i < files.size(); i++){
				arg += " "+files.get(i);
			}
			args.add(arg);
			//System.out.println("Commande lancée : "+args);
			pb = new ProcessBuilder(args);
		}
		else if (mode == 5){
			pb = new ProcessBuilder("ssh",ip,"cat /tmp/amathieu/maps/"+split);
		}
		else {
			pb = new ProcessBuilder("ssh",ip,split);
		}
		Process process;
		try {
			process = pb.start();
			InputStream stderr = process.getErrorStream ();
			InputStream stdout = process.getInputStream ();
			BufferedReader error_out = new BufferedReader (new InputStreamReader(stderr));
			BufferedReader out_out = new BufferedReader(new InputStreamReader (stdout));
			ArrayBlockingQueue<String> array = new ArrayBlockingQueue<String>(200);
			Print_Output p_err = new Print_Output (error_out,array,"err");
			Print_Output p_out = new Print_Output (out_out,array,"out");
			p_err.start();
			p_out.start();		
			String line;
			try {	
				while (p_err.getState() != Thread.State.TERMINATED || p_out.getState() != Thread.State.TERMINATED){
					line = array.poll(10,TimeUnit.SECONDS);
					if (line != null){
						System.out.println(line);
						if (mode == 3){
							String[] keys = line.split(" ");
							String key = keys[keys.length-1];
							String numberOfUM = String.valueOf(split.charAt(1));
							synchronized(map){
								if (!map.containsKey(key)){
									ArrayList<String> UMList = new ArrayList<String>();
									UMList.add("UM"+numberOfUM);
									map.put(key,UMList);
								}
								else {
									ArrayList<String> temp_array = map.get(key);
									temp_array.add("UM"+numberOfUM);
									map.put(key, temp_array);
								}
							}
						}
					}
					else {
						if (p_err.getState() != Thread.State.TERMINATED || p_out.getState() != Thread.State.TERMINATED)
							p_err.interrupt();
							p_out.interrupt();
							error_out.close();
							out_out.close();
							throw new Deploy_Exception("Timeout detected on : "+ip);
					}
				}
				System.out.println("Connection closed with no errors");
				count++;
				error_out.close();
				out_out.close();
			}
			catch (InterruptedException | Deploy_Exception e){
				e.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
