import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements Runnable {
	public enum Command {BAD, NEW, JOIN, MSG, QUIT, SET, DEL, STATE, READY, MOVE, ASK}
	
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
		String[] req = {gameId, pName};
		request(Command.JOIN, req);
	}
	
	public void createShip(int x, int y, int t) {
		String[] req = {Integer.toString(x), Integer.toString(y), Integer.toString(t)};
		request(Command.SET, req);
	}
	
	public void deleteShip(int x, int y) {
		String[] req = {Integer.toString(x), Integer.toString(y)};
		request(Command.DEL, req);
	}
	
	public void plReady() {
		request(Command.READY, null);
	}
	
	public void moveShip(int i, int j, int y, int x) {
		String[] req = {Integer.toString(i), Integer.toString(j),
						Integer.toString(y), Integer.toString(x)};
		request(Command.MOVE, req);
	}
	
	public void askShip(Ship[] ourShips, Ship enemyShip) {
		String our="";
		String enemy="";
		
		for(int i=0; i<ourShips.length; i++) {
			if(ourShips[i]==null) continue;
			our += ourShips[i].y+","+ourShips[i].x+"+";
		}
		enemy = enemyShip.y+","+enemyShip.x;
		
		String[] req = {our, enemy};
		request(Command.ASK, req);
	}
	
	public void run() {
		String str;
		String[] args;
		Command cmd;
		int x, y, t;

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
					game.exit();
					break;
				case SET:
					x = Integer.parseInt(args[1]);
					y = Integer.parseInt(args[2]);
					t = Integer.parseInt(args[3]);
					game.createShip(x, y, t);
					break;
				case DEL:
					x = Integer.parseInt(args[1]);
					y = Integer.parseInt(args[2]);
					game.deleteShip(x, y);
					break;
				case STATE:
					game.setState(args[1]);
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
		if(args!=null)
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
		try {
			sock.shutdownInput();
			sock.shutdownOutput();
			sock.close();
		} catch(Exception e) {
			// ну и фиг
		}
	}
}
