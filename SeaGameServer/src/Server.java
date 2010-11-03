import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	static final int PORT=3002;
	
	public static void main(String[] args) {
		try {
			ServerSocket s = new ServerSocket(PORT);
			System.out.println("Server started at port "+PORT);
			
			while(true) {
				Socket incoming = s.accept();
				Thread t = new Thread(new Client(incoming));
				t.start();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
