import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Thread_IP extends Thread {
	
	String ip;
	String split;
	
	public Thread_IP (String ip, String split){
		this.ip = ip;
		this.split = split;
	}
	
	@Override
	public void run(){
		String[] splits = split.split("[\\/]");
		ip += ":/tmp/amathieu/" + splits[splits.length-1];
		ProcessBuilder pb = new ProcessBuilder("scp","-pr",split,ip);
		//ProcessBuilder pb  = new ProcessBuilder("ssh",ip,"'ls /tmp'");
		//Map<String, String> env = pb.environment();
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
