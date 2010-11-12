
public class Game {
	enum State {NEW, BAD}
	State state = State.NEW;
	Gui gui;
	
	public Game(String pName, String gName, String host, String port, int gameId, Gui g) {
		gui = g;
		Ship.setGame(this);
		Connection srv = new Connection(host, port);
		if(gameId==0) {
			srv.newGame(pName, gName);
		} else {
			srv.joinGame(gameId, pName);
		}
	}
	
	public void exit() {
	}
	
	public static void main(String[] args) {
		new Gui();
	}

}
