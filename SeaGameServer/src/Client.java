import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
	Socket sock;
	BufferedReader in;
	PrintWriter out;
	
	public enum Command {BAD, NEW, JOIN}
	
	public int[] ShipCount = new int[10];
	int playerNum;
	String playerName;
	Game game;
	
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
		out.println(s+"\n");
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
		String[] args;
		Command cmd;
		while(true) {
			str = read();
			if(str==null) break;
			
			args = str.split(";");
			cmd = Command.BAD;
			try {
				cmd = Enum.valueOf(Command.class, args[0]);
			} catch(Exception e) {
				//wrong cmd
			}
			
			switch(cmd) {
				case NEW:
					write("NEW");
					game = new Game(this, args[1], args[2]);
					break;
				case JOIN:
					write("JOIN");
					break;
				case BAD:
					write("BAD");
					break;
			}
		}
	}
}
