import java.net.Socket;

public class Connection implements Runnable {
	Socket sock;
	
	public Connection(String host, String port) {
		try {
			sock = new Socket(host, Integer.valueOf(port));
		} catch(Exception e) {
				//("Сервер недоступен");
		}
	}
	
	public void newGame(String pName, String gName) {
		
	}
	
	public void joinGame(int gameId, String pName, String gName) {
		
	}
	
	public void run() {
		
	}
}
