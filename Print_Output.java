import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

public class Print_Output extends Thread {

	BufferedReader br;
	ArrayBlockingQueue<String> array;
	String name;
	
	public Print_Output (BufferedReader br, ArrayBlockingQueue<String> array, String name){
		this.br = br;
		this.name = name;
		this.array = array;
	}
	
	@Override
	public void run(){
		String line;
		try {
			while ((line = br.readLine()) != null){
				if (name.equals("err")){
					//System.out.println("err : " + line);
					array.put("err : " + line);
				}
				else if (name.equals("out")){
					//System.out.println("out : "+ line);
					array.put("out : " + line);
				}
				else {
					System.out.println("Sortie non reconnue !");
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
