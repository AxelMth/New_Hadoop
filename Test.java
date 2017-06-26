import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Test {

	public static void main(String[] args) {
		Scanner scan = new Scanner (System.in);
		int i = scan.nextInt();
		for(int j = 0; j < i; j++){
			System.out.println("S"+String.valueOf(j+1)+".txt");
		}

	}

}
