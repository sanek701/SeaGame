
public class Game {
	
	static int[] shipPower = {0, 0};
	
	Client p1, p2;
	String name;
	int[][] field = new int[15][16];
	int[][] attackers, defenders;
	int state;
	int ready = 0;
	int order = 1;
	
	public Game(Client p, String pName, String gName) {
		state = 0;
		name = gName;
		p1 = p;
		p1.setPlayer(1, pName);
	}
	
	public void join(Client p, String pName) {
		p2 = p;
		p2.setPlayer(2, pName);
	}
	
	public void setShip(Client p, int i, int j, int t) {
		
	}
	
	public void checkShips(Client p) {
		
	}
	
	public void begin() {
		
	}
	
	public void moveShip(Client p, int i, int j, int x, int y) {
		// y = p.y(y);
	}
	
	public void ask(Client p, int[][] attackers, int i, int j) {
		if(blockIsPossible(opponent(p), i, j)) {
			
		}
	}
	
	public void setDefenders(Client p, int[][] defenders) {
		
	}
	
	private Client opponent(Client p) {
		return (p.playerNum==1) ? (p2) : (p1);
	}
	
	private boolean blockIsPossible(Client p, int i, int j) {
		return false;
	}
	
	private int blockPower() {
		// READ THE FINE RULES
		return 0;
	}
	
}
