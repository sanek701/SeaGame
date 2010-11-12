import java.util.HashMap;

public class Game {
	public enum State{NEW, CONNECTED}
	
	static int[] shipPower = {0, 0};
	static HashMap<Integer, Game> games = new HashMap<Integer, Game>();
	static int gameCount = 0;
	
	Client p1, p2;
	String name;
	State state = State.NEW;
	int[][] field = new int[15][16];
	int[][] attackers, defenders;
	int id;
	int ready = 0;
	int order = 1;
	
	public Game(Client p, String pName, String gName) {
		name = gName;
		p1 = p;
		p1.setPlayer(1, pName);
		gameCount += 1;
		id = gameCount;
		games.put(id, this);
	}
	
	public static void join(Client p, String pName, int gameId) {
		games.get(gameId).addPlayer(p, pName);
	}
	
	public void addPlayer(Client p, String pName) {
		p2 = p;
		p2.setPlayer(2, pName);
		state = State.CONNECTED;
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
