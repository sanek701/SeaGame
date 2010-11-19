import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements Runnable {
	public enum Command {BAD, NEW, JOIN, MSG, QUIT}
	
	Socket sock;
	BufferedReader in;
	PrintWriter out;
	Game game = null;
	
	public Connection(String host, String port, Game g) {
		try {
			sock = new Socket(host, Integer.valueOf(port));
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
		} catch(Exception e) {
			//("Сервер недоступен");
			e.printStackTrace();
		}
		game = g;
		new Thread(this).start();
	}
	
	public void newGame(String pName, String gName) {
		String[] req = {pName, gName};
		request(Command.NEW, req);
	}
	
	public void joinGame(String gameId, String pName) {
		
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
				case MSG:
					game.gui.addMsg(args[1]);
					break;
				case QUIT:
					close();
					break;
			}
		}
	}
	
	private String read() {
		String line = null;
		try {
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
	
	public static String[][] getGameList(String host, String port) {
		Socket s = null;
		BufferedReader inc = null;
		PrintWriter outc = null;
		String[] games = null;
		
		try {
			s = new Socket(host, Integer.valueOf(port));
			inc = new BufferedReader(new InputStreamReader(s.getInputStream()));
			outc = new PrintWriter(s.getOutputStream(), true);
			outc.println("GAMELIST;");
			games = inc.readLine().split(",");
		} catch(Exception e) {
			e.printStackTrace();
		}
			
		String[][] gameList = new String[games.length-1][2];
		for(int i=0; i<(games.length-1); i++)
			gameList[i] = games[i].split("-");
		
		try {
			s.shutdownInput();
			s.shutdownOutput();
			s.close();
		} catch(Exception e) {
			//неважно
		}
		
		return gameList;
	}
	
	public void close() {
		System.out.println("Connection.close()");
		try {
			sock.shutdownInput();
			sock.shutdownOutput();
			sock.close();
		} catch(Exception e) {
			// ну и фиг
		}
		System.out.println("Connection.close() done");
		//game.over();
	}
}
