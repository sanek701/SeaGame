import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;


public class Game {
	public enum State{NEW, CONNECTED, MOVE, ASK, ANS}
	
	static int[] shipPower = {0, 0};
	static HashMap<Integer, Game> games = new HashMap<Integer, Game>();
	static int gameCount = 0;
	private static int[] rateShipPower = {3, -2 , -4}; //магические чиселки для вычисления мощности блока
	
	Client p1=null, p2=null;
	public String name;
	State state = State.NEW;
	Ship[][] field = new Ship[15][16];
	int id;
	int playersReady = 0;
	int order = 0;
	int[] atdef = new int[4];
	
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
		p1.setState("CREATESHIPS");
		p2.setState("CREATESHIPS");
		return this;
	}
	
	public void setShip(Client p, int i, int j, int t) {
		if(state!=State.CONNECTED) return;
		
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
		if(state!=State.CONNECTED) return;
		
		i = p.y(i);
		if(field[i][j].owner == p) {
			field[i][j] = null;
			p.deleteShip(i, j);
			opponent(p).deleteShip(i, j);
		} else {
			p.setShip(i, j, 0);
		}
	}
	
	public void proceedReady(Client p) {
		switch(state) {
			case CONNECTED:
				checkShips(p);
				break;
			case ASK:
				if(order==1) {
					order = 2;
					p2.setState("MOVE");
					p1.setState("WAITING");
				} else {
					order = 1;
					p1.setState("MOVE");
					p2.setState("WAITING");
				}
				state = State.MOVE;
				break;
		}
	}
	
	public void checkShips(Client p) {
		if(state!=State.CONNECTED) return;
		
		boolean result = true;
		int[] normalShipCnt = {-1, 2, 5, 6, 6, 6, 6, 6, 2, 1, 6, 6};
		int[] shipCnt =  {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		for(int i=0; i<15; i++) {
			for(int j=0; j<16; j++) {
				if(field[i][j]!=null && field[i][j].owner==p) {
					shipCnt[field[i][j].type] = shipCnt[field[i][j].type] + 1;
				}
			}
		}
		for(int i=1; i<12; i++) {
			if(normalShipCnt[i] != shipCnt[i]) {
					result = false;
			}
		}
		if(result && !p.ready) {
			p.setState("WAITING");
			p.ready = true;
			playersReady += 1;
			if(playersReady==2) { // Starts the game
				state = State.MOVE;
				order = 1;
				p1.setState("MOVE");
			}
		} else {
			String text = "Неверное количество кораблей";
			p.sndMsg(text);
		}
	}
	
	public void moveShip(Client p, int i, int j, int y, int x) {
		if(state!=State.MOVE) return;
		
		 i = p.y(i); y = p.y(y);
		 int type = field[i][j].type;
		 double dist = distance(i,j,y,x);
		 
		 if (field[y][x]!=null) {
			 p.sndMsg("Неверный ход");
			 return;
		 }

		 if(dist == 1.0) {
		     if(type == 11 && checkTral(p, i, j, y, x)) {
		    	 acceptMove(p, i, j, y, x, type);
			 }else if (type != 8 && type != 11) {
				 acceptMove(p, i, j, y, x, type);
			 }
		 } else if(dist > 1.0 && dist <= 2.0 && type == 5) {
			// Провераем если катер пошел через клетку прямо
			 if( abs(i-y)==2 || abs(j-x)==2) {
				 //если занята ячейка через которую от "прыгает"
				 if(field[i+signum(y-i)][j+signum(x-j)] == null) {
					acceptMove(p, i, j, y, x, type);
				 }
				 //Если прыгаем в угловую проверяем есть хоть одна своюодная клетка
			 } else if(field[i+signum(y-i)][j] == null || field[i][j+signum(x-j)] == null ) {
				 acceptMove(p, i, j, y, x, type);
			 }
		 } else {
				 p.sndMsg("Неверный ход");
		 }
	}
	
	private void acceptMove(Client p, int i, int j, int y, int x, int type) {	
		p.setShip(y, x, type);
		opponent(p).setShip(y, x, 0);
		 
		p.setShip(i, j, -1);
		opponent(p).setShip(i, j, -1);
		 
		field[y][x] = field[i][j];
		field[i][j] = null;
		
		state = State.ASK;
		p.setState("ASK");
	}
	
	public void ask(Client p, int i, int j, int y, int x) {
		if(state!=State.ASK) return;
		i = p.y(i); y = p.y(y);
		Client op = opponent(p);
		
		if(blockIsPossible(op, i, j)) {
			atdef[0] = i;
			atdef[1] = j;
			atdef[2] = y;
			atdef[3] = x;
			state = State.ANS;
			op.sndMsg("ANS;"+op.y(y)+";"+x+";");
		}
	}
	
	public void ans(Client p, String[] block) {
		
	}
	
	public void bomb(Client p){
		int bi = 0, bj= 0,k ,t, top, bottom, right, left; 
		
		for(bi = 0; bi < 15; bi++){
			for(bj = 0; bj < 16; bj ++){
				if(field[bi][bj] != null && field[bi][bj].type == 9 && field[bi][bj].owner == p)
					break;
			}
		}
		
		top = bi-2;
		bottom = bi+2;
		right = bj+2;
		left = bj-2;
		if (top < 0) top = 0;
		if (bottom > 14) bottom = 14;
		if (left < 0) left = 0;
		if (right > 15)right = 15;
		
		for(k = top; k <= bottom; k++) {
			for(t = left; t <= right; t++) {
				if (field[k][t]!= null){
					//удалить корабль
				}
			}
		}
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
	
	private int compareBlocks(Ship[] block1, Ship[] block2) {
		int power1 = rateShipPower[block1.length-1]+block1[0].type*3;
		int power2 = rateShipPower[block2.length-1]+block2[0].type*3;
		return signum(power1-power2);//-1-> 1<2; 0-> 1=2; 1-> 1>2; 
	}
	
	private double distance(int x1, int y1, int x2, int y2) {
		return Math.sqrt( (double)(x1-x2)*(x1-x2)+(y1-y2)*(y1-y2)); 
	}
	
	private boolean checkTral(Client p,int i,int j, int y, int x) {
		int k, t;
		int top = i-1;
		int bottom = i+1;
		int right = j+1;
		int left = j-1;
		
		if ((i-1) < 0) top = 0;
		if ((i+1) > 14) bottom = 14;
		if ((j-1) < 0) left = 0;
		if ((j+1) > 15)right = 15;
		
		for(k = top; k <= bottom; k++) {
			for(t = left; t <= right; t++) {
				if (field[k][t]!= null && field[k][t].type == 5 && field[k][t].owner == p){ //нашли тральщика
					if(abs(k-y)<=1 && abs(t-x)<=1) return true;
				}
			}
		}
		
		return false;
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
		int t = field[i][j].type;
		
		if(i-1 >= 0 && field[i-1][j] != null &&
				field[i-1][j].type == t && field[i-1][j].owner == p) return true;
		if(i+1 <= 14 && field[i+1][j] != null && 
				field[i+1][j].type == t && field[i+1][j].owner == p) return true;
		if(j-1 >= 0  && field[i][j-1] != null && 
				field[i][j-1].type == t && field[i][j-1].owner == p) return true;
		if(j+1 <= 15 && field[i][j+1] != null &&
				field[i][j+1].type == t && field[i][j+1].owner == p) return true;
		
		return false;
	}
}
