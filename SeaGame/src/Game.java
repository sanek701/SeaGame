
public class Game {
	enum State {NEW, BAD, SCRT}
	State state = State.NEW;
	Gui gui;
	
	public Game(String pName, String gName, String host, String port, int gameId, Gui g) {
		gui = g;
		Ship.setGame(this);
		Connection srv = new Connection(host, port);
		if(gameId==0) {
			srv.newGame(pName, gName);
			state = State.SCRT;//test create Ship
		} else {
			srv.joinGame(gameId, pName);
		}
	}
	
	public void exit() {
	}
	
	public void createShip(int x, int y, int type){
		Ship.count[type] += 1;
		Ship.sum += 1;
		gui.field[x][y].setType(type);
	}
	
	public void deleteShip(int x, int y, int type){
		Ship.count[type] -= 1;
		Ship.sum -= 1;
		gui.field[x][y].setType(-1);
	}
	
	public static void main(String[] args) {
		new Gui();
	}

}
