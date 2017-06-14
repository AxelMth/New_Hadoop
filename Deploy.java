import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Deploy {

	String fileName;
	ArrayList<String> splits;
	HashMap<String,String> map;
	
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
		return map;
	}
	
}
