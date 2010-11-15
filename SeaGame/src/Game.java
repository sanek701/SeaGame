
public class Game {
	enum State {NEW, CREATESHIPS}
	final static int[] normalShipCnt = {-1, 2, 5, 6, 6, 6, 6, 6, 2, 1, 6, 6};
	int[] shipCnt =  {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	int shipSum = 0;
	Gui gui;
	State state;
	
	public Game(String pName, String gName, String host, String port, int gameId, Gui g) {
		gui = g;
		Ship.setGame(this);
		Connection srv = new Connection(host, port);
		if(gameId==0) {
			srv.newGame(pName, gName);
			//state = State.NEW;
			state = State.CREATESHIPS;
		} else {
			srv.joinGame(gameId, pName);
		}
	}
	
	public void exit() {
	}
	
	public void joinGame(int id, String plName){
	}
	
	public void createShip(int x, int y, int type) {
		shipCnt[type] += 1;
		shipSum += 1;
		setShip(x, y, type);
	}
	
	public void deleteShip(int x, int y, int type) {
		shipCnt[type] -= 1;
		shipSum -= 1;
		setShip(x, y, -1);
	}
	
	private void setShip(int x, int y, int t) {
		gui.field[x][y].setType(t);
	}
	
	public static void main(String[] args) {
		new Gui();
	}
}
