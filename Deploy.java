import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class Deploy {

	String fileName;
	ArrayList<String> splits;
	Map<String,Integer> map;
	
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
				t_array.add(new Thread_IP(ip,splits.get(count),0));
				t_array.add(new Thread_IP(ip,"/home/amathieu/workspace/SLR207/src/slave.jar",1));
				t_array.get(t_array.size()-1).start();
				t_array.get(t_array.size()-2).start();
				String[] split = splits.get(count).split(" ");
				int number = split[split.length-1].charAt(1);
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
				t_array.add(new Thread_IP(ip,null,2));
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
	
	
}
