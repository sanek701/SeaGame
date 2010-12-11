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
		 
		 if(field[y][x]!=null) {
			 p.sndMsg("Неверный ход");
			 return;
		 }
		 
		 acceptMove(p, i, j, y, x, type);
		 /* for testing purposes
		 if(dist == 1.0) {
		     if(type == 11 && checkTral(p, i, j, y, x)) {
		    	 acceptMove(p, i, j, y, x, type);
			 } else if (type != 8 && type != 11) {
				 acceptMove(p, i, j, y, x, type);
			 }
		 } else if(dist>1.0 && dist<=2.0 && type==5) {
			// Провераем если катер пошел через клетку прямо
			 if(abs(i-y)==2 || abs(j-x)==2) {
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
		 */
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
		if(p.playerNum!=order) return;
		
		i = p.y(i); y = p.y(y);
		Client op = opponent(p);
		
		if(distance(i,j,y,x) != 1) {
			p.sndMsg("Вопрос не корректный");
			p.setState("ASK");
			return;
		} 
		
		if(blockIsPossible(op, y, x)) {
			atdef[0] = i;
			atdef[1] = j;
			atdef[2] = y;
			atdef[3] = x;
			state = State.ANS;
			op.write("ANS;"+op.y(y)+";"+x+";");
			return;
		}
		
		int attackerLength = findBlock(p, i, j, 0);
		int result = compareBlocks(attackerLength, field[i][j].type, 1, field[y][x].type);
		switch(result) { //что когда делаем
			case 1: //win
				field[y][x] = null;
				p.deleteShip(y, x);
				opponent(p).deleteShip(y, x);
				break;
			case -1: //loss
				field[i][j] = null;
				p.deleteShip(i, j);
				opponent(p).deleteShip(i, j);
				break;
			case 0: //equal
				field[y][x] = null;
				p.deleteShip(y, x);
				opponent(p).deleteShip(y, x);
				field[i][j] = null;
				p.deleteShip(i, j);
				opponent(p).deleteShip(i, j);
				break;
		}
	}
	
	public void ans(Client p, String[] block) {
		int k;
		String[] sh;
		int[][] defenders = new int[block.length][2];
		
		for(k=0; k < block.length; k++) {
			sh = block[k].split(",");
			defenders[k][0] = Integer.parseInt(sh[0]);
			defenders[k][0] = Integer.parseInt(sh[1]);
		}
		
		if(!IsBlockCorrect(defenders)) {
			p.sndMsg("Неправильный блок");
			return;
		}
		
		// а дальше?
	}
	
	private boolean IsBlockCorrect(int[][] block) {
		if(block[0][0] == atdef[2] && block[0][0] == atdef[3]) {
			return false;
		}
		
		switch(block.length) {
			case 1:
				return true;
			case 2:
				if(distance(block[0][0], block[0][1], block[1][0],block[1][1]) == 1)
					return true;
				break;
			case 3:
				double d12 = distance(block[0][0], block[0][1], block[1][0], block[1][1]);
				double d13 = distance(block[0][0], block[0][1], block[2][0], block[2][1]);
				double d23 = distance(block[1][0], block[1][1], block[2][0], block[2][1]);
				
				if( (d12==1 && d13==1) || (d12==1 && d23==1) || (d13==1 && d23==1) )
					return true;
				break;
		}
		
		return false;
	}
	
	private int findBlock(Client p, int i, int j, int step) {
		int len = 1;
		int t = field[i][j].type;
		int[][] coordinates = new int[3][2];
		coordinates[0][0] = i;
		coordinates[0][1] = j;
	
		if(i-1 >= 0 && field[i-1][j]!=null && field[i-1][j].owner==p && field[i-1][j].type==t) {
			len += 1;
			coordinates[len-1][0] = i-1;
			coordinates[len-1][1] = j;
		}
		if(j-1>=0 && field[i][j-1]!=null && field[i][j-1].owner==p && field[i][j-1].type==t) {
			len += 1;
			coordinates[len-1][0] = i;
			coordinates[len-1][1] = j-1;
		}
		if(i+1<=14 && field[i+1][j]!=null && field[i+1][j].owner==p && field[i+1][j].type==t) {
			len+=1;
		}
		if(j+1<=15 && field[i][j-1]!=null && field[i][j+1].owner==p && field[i][j+1].type==t) {
			len += 1;
			coordinates[len-1][0] = i;
			coordinates[len-1][1] = j+1;
		}
		
		if(len==2 && step != 1) {
			return findBlock(p, coordinates[len-1][0], coordinates[len-1][1], 1);
		}
		
		if(len > 3) return 3;
		
		return len;
	} 
	
	public void bomb(Client p) {
		int bi = 0, bj= 0,k ,t, top, bottom, right, left;
		
		for(bi=0; bi<15; bi++) {
			for(bj=0; bj<16; bj++) {
				if(field[bi][bj]!=null && field[bi][bj].type==9 && field[bi][bj].owner==p)
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
				if (field[k][t]!= null) {
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
	
	private int compareBlocks(int l1, int type1, int l2, int type2) {
		int power1 = rateShipPower[l1-1]+type1*3;
		int power2 = rateShipPower[l2-1]+type2*3;
		return signum(power2-power1); 
	}
	
	private double distance(int x1, int y1, int x2, int y2) {
		return Math.sqrt((double)(x1-x2)*(x1-x2)+(y1-y2)*(y1-y2)); 
	}
	
	private boolean checkTral(Client p,int i,int j, int y, int x) {
		int k, t;
		int top    = i-1;
		int bottom = i+1;
		int right  = j+1;
		int left   = j-1;
		
		if ((i-1) < 0)  top = 0;
		if ((i+1) > 14) bottom = 14;
		if ((j-1) < 0)  left = 0;
		if ((j+1) > 15) right = 15;
		
		for(k=top; k<=bottom; k++) {
			for(t=left; t<=right; t++) {
				if (field[k][t]!=null && field[k][t].type==5 && field[k][t].owner==p) { //нашли тральщик
					if(abs(k-y)<=1 && abs(t-x)<=1)
						return true;
				}
			}
		}
		
		return false;
	}
	
	private int signum(int i) {
		if(i > 0) return 1;
		if(i < 0) return -1;
		return 0;
	}
	
	private int abs(int i) {
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
