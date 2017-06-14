import java.awt.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Thread_IP extends Thread {
	
	String ip;
	String split;
	int mode;
	HashMap<String,ArrayList<String>> map;
	
	public Thread_IP (String ip, String split, int mode, HashMap<String,ArrayList<String>> map){
		this.ip = ip;
		this.split = split;
		this.mode = mode;
		this.map = map;
	}
	
	@Override
	public void run(){
		ProcessBuilder pb;
		if (mode == 0){
			ip += ":/tmp/amathieu/splits";
			//System.out.println("scp -pr "+ split + ip);
			pb = new ProcessBuilder("scp","-pr",split,ip);	
		}
		else if (mode == 1){
			ip += ":/tmp/amathieu";
			pb = new ProcessBuilder("scp","-pr",split,ip);
			//System.out.println(split + ' ' + ip);
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
			//System.out.println(args);
			pb = new ProcessBuilder(args);
		}
		else {
			pb = new ProcessBuilder("ssh",ip,"cat /tmp/amathieu/maps/"+split);
		}
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
