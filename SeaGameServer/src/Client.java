import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
	Socket sock;
	BufferedReader in;
	PrintWriter out;
	
	public int[] ShipCount = new int[10];
	int playerNum;
	String playerName;
	
	public Client(Socket s) {
		sock = s;
		try{
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		for(int i=0; i<ShipCount.length; i++) ShipCount[i] = 0;
	}
	
	public String read() {
		String line = null;
		try{
			line = in.readLine().trim();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return line;
	}
	
	public void write(String s) {
		out.println(s);
	}
	
	public int y(int j) {
		return (playerNum==1) ? (j) : (15-j);
	}
	
	public void setPlayer(int n, String s) {
		playerNum = n;
		playerName = s;
	}
	
	public void run() {
		String str;
		while(true) {
			str = read();
			if(str==null) break;
			
			write(str);
		}
	}
}
