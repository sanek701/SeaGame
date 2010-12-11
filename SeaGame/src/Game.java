
public class Game {
	enum State {WAITING, CREATESHIPS, MOVE, ASK, ANS}
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
		gui.showReadyButton(false);
		
		if(state == State.ANS) {
			srv.ans(Ship.block);
			Ship.freeBlock();
		} else {
			srv.plReady();
		}
	}
	
	public void createShip(int x, int y, int type) {
		if(type > 0) { 
			shipCnt[type] += 1;
			shipSum += 1;
			if(shipSum == 52)
				gui.showReadyButton(true);
		}
		setShip(x, y, type);
	}
	
	public void deleteShip(int x, int y) {
		int type = getShip(x, y).type;
		if(type > 0) { // our ship
			if(shipSum == 52)
				gui.showReadyButton(false);
			shipSum -= 1;
			shipCnt[type] -= 1;
		}
		
		setShip(x, y, -1);
	}
	
	public void setState(String str) {
		State st = Enum.valueOf(State.class, str);
		state = st;
		switch(state) {
			case CREATESHIPS:
				gui.addMsg("Расставьте свои корабли");
				break;
			case MOVE:
				gui.addMsg("Ваш ход");
				break;
			case ASK:
				gui.addMsg("Ход сделан. Вы можете спросить корабль противника.");
				gui.showReadyButton(true);
				break;
			case WAITING:
				gui.addMsg("Ход сделан. Подождите.");
				gui.showReadyButton(false);
				break;
		}
	}
	
	public void ans(int y, int x) {
		Ship t = gui.field[y][x];
		Ship.block[0] = t;
		t.selected = true;
		t.repaint();
		
		gui.showReadyButton(true);
		state = State.ANS;
	}
	
	public void test(int p) {
		srv.createShip(14,0,1);
		srv.createShip(14,1,1);
		srv.createShip(14,2,2);
		srv.createShip(14,3,2);
		srv.createShip(14,4,2);
		srv.createShip(14,5,2);
		srv.createShip(14,6,2);
		srv.createShip(14,7,3);
		srv.createShip(14,8,3);
		srv.createShip(14,9,3);
		srv.createShip(14,10,3);
		srv.createShip(14,11,3);
		srv.createShip(14,12,3);
		srv.createShip(14,13,4);
		srv.createShip(14,14,4);
		srv.createShip(14,15,4);
		srv.createShip(13,13,4);
		srv.createShip(13,14,4);
		srv.createShip(13,15,4);
		srv.createShip(13,12,5);
		srv.createShip(13,11,5);
		srv.createShip(13,10,5);
		srv.createShip(13,9,5);
		srv.createShip(13,8,5);
		srv.createShip(13,7,5);
		srv.createShip(13,6,6);
		srv.createShip(13,5,6);
		srv.createShip(13,4,6);
		srv.createShip(13,3,6);
		srv.createShip(13,2,6);
		srv.createShip(13,1,6);
		srv.createShip(13,0,9);
		srv.createShip(12,0,7);
		srv.createShip(12,1,7);
		srv.createShip(12,2,7);
		srv.createShip(12,3,7);
		srv.createShip(12,4,7);
		srv.createShip(12,5,7);
		srv.createShip(12,6,8);
		srv.createShip(12,7,8);
		srv.createShip(12,8,10);
		srv.createShip(12,9,10);
		srv.createShip(12,10,10);
		srv.createShip(12,11,10);
		srv.createShip(12,12,10);
		srv.createShip(12,13,10);
		srv.createShip(11,0,11);
		srv.createShip(11,1,11);
		srv.createShip(11,2,11);
		srv.createShip(11,3,11);
		srv.createShip(11,4,11);
		srv.createShip(11,5,11);
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
