import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements Runnable {
	public enum Command {NEW, JOIN}
	
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
		while(true) {
			str = read();
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
		return line;
	}
	
	private void request(Command cmd, String[] args) {
		String s = cmd.toString()+";";
		// join args with ';'
		out.println(s);
	}
}
