import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Deploy {

	String fileName;
	ArrayList<String> splits;
	
	public Deploy (String fileName, ArrayList<String> splits){
		this.fileName = fileName;
		this.splits = splits;
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
				t_array.add(new Thread_IP(ip,splits.get(count)));
				t_array.get(t_array.size()-1).start();
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
	
}
