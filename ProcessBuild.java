import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ProcessBuild {

	public static void main(String argv[]) throws IOException{
		ProcessBuilder pb = new ProcessBuilder("java", "-jar","/tmp/amathieu/slave.jar");
		//File log = new File("/home/amathieu/workspace/SLR207/src/aaa.txt");
		Process process = pb.start();
		InputStream stderr = process.getErrorStream ();
		InputStream stdout = process.getInputStream ();
		BufferedReader error_out = new BufferedReader (new InputStreamReader(stderr));
		BufferedReader out_out = new BufferedReader(new InputStreamReader (stdout));
		ArrayBlockingQueue<String> array = new ArrayBlockingQueue<String>(1000);
		Print_Output p_err = new Print_Output (error_out,array,"err");
		Print_Output p_out = new Print_Output (out_out,array,"out");
		p_err.start();
		p_out.start();
		String line;
		try {
			while (p_err.getState() != Thread.State.TERMINATED || p_out.getState() != Thread.State.TERMINATED){
				line = array.poll(500,TimeUnit.MILLISECONDS);
				if (line != null){
					System.out.println(line);		
				}
				else {
					if (p_err.getState() != Thread.State.TERMINATED || p_out.getState() != Thread.State.TERMINATED)
						System.out.println("Timeout");
						break;
				}
			}
			p_err.join();
			p_out.join();
		}
		catch (InterruptedException e){
			e.printStackTrace();
		}
	}
}
