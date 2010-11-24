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
		if(field[i][j].owner == p){
			field[i][j] = null;
			p.deleteShip(i, j);
			opponent(p).deleteShip(i, j);
		}else{
			p.setShip(i, j, 0);
		}
	}
	
	public void checkShips(Client p) {
		int[] normalShipCnt = {-1, 2, 5, 6, 6, 6, 6, 6, 2, 1, 6, 6};
		int[] shipCnt =  {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		for(int i=0; i<15; i++) {
			for(int j=0; j<16; j++) {
				if(field[i][j]!=null && field[i][j].owner==p) {
					shipCnt[field[i][j].type] = shipCnt[field[i][j].type] + 1;
				}
			}
		}
		if(normalShipCnt!=shipCnt) {			
			String text = "Bad boy";
			p.sndMsg(text);
		}
	}
	
	public void begin() {
		
	}
	
	public void moveShip(Client p, int i, int j, int y, int x) {
		 i = p.y(i); y = p.y(y);
		 int type = field[i][j].type;
		 double dist = distance(i,j,y,x);
		 
		 if (field[i][j]!=null) {
			 cancelMove(p, i, j, y, x, type);
			 return;
		 }
		 
		 if(dist == 1) {
		     if(type == 8) {
				cancelMove(p, i, j, y, x, type);
			 }else if (type == 11 && (!checkTral(i,j) || !checkTral(y,x))) {
				cancelMove(p, i, j, y, x, type);
			 }else{
				acceptMove(p, i, j, y, x, type);
			 }
		 }else if(dist > 1 && dist < 2) {
			// Провераем если катер пошел через клетку прямо
			 if( abs(i-y)==2 || abs(j-x)==2 ) {
				 //если занята ячейка через которую от "прыгает"
				 if (field[i+signum(y-i)][j+signum(x-i)]!=null){
					cancelMove(p, i, j, y, x, type);
				 }else{
					acceptMove(p, i, j, y, x, type);
				 }
				 //Если прыгаем в угловую проверяем есть хоть одна своюодная клетка
			 }else if(field[i+signum(y-i)][j]!=null && field[i][j+signum(x-j)]!=null){
				 cancelMove(p, i, j, y, x, type);
			 }else{
				 acceptMove(p, i, j, y, x, type);
			 }
		 }else{
			 cancelMove(p, i, j, y, x, type);
		 }
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
	
	private double distance(int x1, int y1, int x2, int y2) {
		return 0.0;
	}
	
	private boolean checkTral(int i,int j){
		return true;
	}
	
	private void cancelMove(Client p, int i, int j, int y, int x, int type){
		 p.setShip(i, j, type); 
		 p.setShip(y, x, -1);
	}
	
	private void acceptMove(Client p, int i, int j, int y, int x, int type){	
		p.setShip(y, x, type);
		opponent(p).setShip(y, x, 0);
		 
		p.setShip(i, j, -1);
		opponent(p).setShip(i, j, -1);
		 
		field[y][x] = field[i][j];
		field[i][j] = null;
	}
	
	private int signum(int i){
		if(i > 0) return 1;
		if(i < 0) return -1;
		return 0;
	}
	
	private int abs(int i){
		if(i<0) i*=-1;
		return i;
	}
	
	private Client opponent(Client p) {
		return (p.playerNum==1) ? (p2) : (p1);
	}
	
	private boolean blockIsPossible(Client p, int i, int j) {
		return false;
	}
}
