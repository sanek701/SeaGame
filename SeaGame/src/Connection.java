import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements Runnable {
	public enum Command {BAD, NEW, GLIST, JOIN}
	
	Socket sock;
	BufferedReader in;
	PrintWriter out;
	
	public Connection(String host, String port) {
		try {
			sock = new Socket(host, Integer.valueOf(port));
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
		} catch(Exception e) {
			//("Сервер недоступен");
			e.printStackTrace();
		}
	}
	
	public void newGame(String pName, String gName) {
		String[] req = {pName, gName};
		request(Command.NEW, req);
	}
	
	public void joinGame(int gameId, String pName) {
		
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
		}
	}
	
	private String read() {
		String line = null;
		try{
			line = in.readLine().trim();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		System.out.println("<- "+line); //debug
		return line;
	}
	
	private void request(Command cmd, String[] args) {
		String s = cmd.toString()+";";
		for(int i=0; i<args.length; i++)	// join args with ';'
			s += args[i]+";";

		System.out.println("-> "+s); //debug
		out.println(s);
	}
}
