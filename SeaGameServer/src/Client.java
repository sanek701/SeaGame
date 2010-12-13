import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
	Socket sock;
	BufferedReader in;
	PrintWriter out;
	
	public enum Command {BAD, NEW, JOIN, GAMELIST, SET,
		DEL, READY, MOVE, ASK, ANS, BOMB}
	
	public int[] ShipCount = new int[10];
	public boolean ready;
	int playerNum;
	String playerName;
	Game game;
	
	public Client(Socket s) {
		sock = s;
		try {
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
			if(game!=null) game.exit();
			return null;
		}
		return line;
	}
	
	public void write(String s) {
		out.println(s);
	}
	
	public int y(int j) {
		return (playerNum == 2) ? (j) : (14-j);
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
				sndMsg("Неверный запрос");
			}
			
			switch(cmd) {
				case GAMELIST:
					write(Game.gameList()+";");
					break;
				case NEW:
					if(args.length != 3) write("BAD;");
					game = new Game(this, args[1], args[2]);
					break;
				case JOIN:
					if(args.length != 3) write("BAD;");
					game = Game.getGame(args[1]).addPlayer(this, args[2]);
					break;
				case SET:
					game.setShip(this, Integer.parseInt(args[1]), Integer.parseInt(args[2]),
							Integer.parseInt(args[3]));
					break;
				case DEL:
					game.deleteShip(this, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
					break;
				case READY:
					game.proceedReady(this);
					break;
				case MOVE:
					game.moveShip(this, Integer.parseInt(args[1]), Integer.parseInt(args[2]),
							Integer.parseInt(args[3]),Integer.parseInt(args[4]));
					break;
				case ASK:
					game.ask(this, Integer.parseInt(args[1]), Integer.parseInt(args[2]),
							Integer.parseInt(args[3]),Integer.parseInt(args[4]));
					break;
				case ANS:
					game.ans(this, args[1].split("#"));
					break;
				case BOMB:
					game.bomb(this);
					break;
			}
		}
	}
	
	public void sndMsg(String s) {
		write("MSG;"+s+";");
	}
	
	public void setShip(int i, int j, int t) {
		write("SET;"+y(i)+";"+j+";"+t+";");
	}
	
	public void deleteShip(int i, int j) {
		write("DEL;"+y(i)+";"+j+";");
	}
	
	public void setState(String st) {
		if(game!=null && game.state==Game.State.OVER) return;
		write("STATE;"+st+";");
	}
	
	public void quit() {
		write("QUIT;");
		try {
			sock.shutdownInput();
			sock.shutdownOutput();
			sock.close();
		} catch(Exception e) {
				// Игнорировать ошибки
		}
	}
}
