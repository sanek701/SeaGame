import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;

public class Game {
	public enum State{NEW, CONNECTED}
	
	static int[] shipPower = {0, 0};
	static HashMap<Integer, Game> games = new HashMap<Integer, Game>();
	static int gameCount = 0;
	
	Client p1=null, p2=null;
	public String name;
	State state = State.NEW;
	Ship[][] field = new Ship[15][16];
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
	
	public Game addPlayer(Client p, String pName) {
		if(p2!=null) return null; //Player already exists
		p2 = p;
		p2.setPlayer(2, pName);
		p1.sndMsg("Player "+pName+" joined the game.");
		p2.sndMsg("Welcome to "+name+"("+p1.playerName+").");
		state = State.CONNECTED;
		return this;
	}
	
	public void setShip(Client p, int i, int j, int t) {
		i = p.y(i);
		if( (p.playerNum==1 && i>=0 && i<=4) || (p.playerNum==2 && i>=10 && i<=14) ) {
			 if(field[i][j] == null) {
				 field[i][j] = new Ship(p, t);
				 p.setShip(i, j, t);
				 opponent(p).setShip(i, j, 0);
				 return;
			 }
		}
	}
	
	public void deleteShip(Client p, int i, int j) {
		i = p.y(i);
		field[i][j] = null;
		p.deleteShip(i, j);
		opponent(p).deleteShip(i, j);
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
	
	public static String gameList() {
		Set<Map.Entry<Integer, Game>> s = games.entrySet();
		String result = "";
		Iterator<Map.Entry<Integer, Game>> i = s.iterator();
		while(i.hasNext()) {
			Map.Entry<Integer, Game>t = i.next();
			result += t.getKey().toString()+"-"+((Game)t.getValue()).getName()+",";
		}
		return result;
	}
	
	public static Game getGame(String gameId) {
		return games.get(Integer.valueOf(gameId));
	}
	
	public String getName() {
		return name;
	}
	
	public void exit() {
		System.out.println("game.exit();");
		if (p1!=null) p1.quit();
		if (p2!=null) p2.quit();
		if(games.containsKey(id))
			games.remove(id);
	}
	
	private Client opponent(Client p) {
		return (p.playerNum==1) ? (p2) : (p1);
	}
	
	private boolean blockIsPossible(Client p, int i, int j) {
		return false;
	}
}
