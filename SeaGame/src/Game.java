
public class Game {
	int state;
	Gui gui;
	
	public Game(String pName, String gName, String host, String port, int gameId, Gui g) {
		state = 0;
		gui = g ;
		Connection srv = new Connection(host, port);
		if(gameId==0) {
			srv.newGame(pName, gName);
		} else {
			srv.joinGame(gameId, pName, gName);
		}
	}
	
	public void exit() {
		state = -1;
	}
	
	public static void main(String[] args) {
		new Gui();
	}

}
