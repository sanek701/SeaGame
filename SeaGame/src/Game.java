
public class Game {
	enum State {WAITING, CREATESHIPS, MOVE}
	final static int[] normalShipCnt = {-1, 2, 5, 6, 6, 6, 6, 6, 2, 1, 6, 6};
	int[] shipCnt =  {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	int shipSum = 0;
	Gui gui;
	State state;
	Connection srv = null;
	
	public Game(String pName, String gName, String host, String port, String gameId, Gui g) {
		gui = g;
		Ship.setGame(this);
		srv = new Connection(host, port, this);
		if(gameId=="") {
			srv.newGame(pName, gName);
			state = State.WAITING;
		} else {
			srv.joinGame(gameId, pName);
			state = State.WAITING;
		}
	}
	
	public void exit() {
		Ship.setGame(null);
		gui.setGame(null);
		gui.emptyField();
		srv.close();
	}
	
	public void ready() {
		srv.plReady();
		gui.showReadyButton(false);
	}
	
	public void createShip(int x, int y, int type) {
		if(type > 0) { 
			shipCnt[type] += 1;
			shipSum += 1;
			if(shipSum == 52) gui.showReadyButton(true);
		}
		setShip(x, y, type);
	}
	
	public void deleteShip(int x, int y) {
		int type = getShip(x, y).type;
		if(type > 0) { // our ship
			if(shipSum == 52) gui.showReadyButton(false);
			shipSum -= 1;
			shipCnt[type] -= 1;
		}
		
		setShip(x, y, -1);
	}
	
	public void moveShip() {
		
	}
	
	public void setState(String str) {
		State st = Enum.valueOf(State.class, str);
		state = st;
	}
	
	private Ship getShip(int x, int y) {
		return gui.field[x][y];
	}
	
	private void setShip(int x, int y, int t) {
		gui.field[x][y].setType(t);
	}
	
	public static void main(String[] args) {
		new Gui();
	}
}
