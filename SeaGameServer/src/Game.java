import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;


public class Game {
	public enum State{NEW, CONNECTED, MOVE, ASK, ANS, OVER}
	static String[] shipNames = {"","Линкор","Крейсер", "Эсминец","Сторожевик",
		"Торпедный катер", "Тральщик", "Подводная лодка",
		"Форт", "Атомная бомба", "Торпеда", "Мина"};
	
	static int[] shipPower = {0, 0};
	static HashMap<Integer, Game> games = new HashMap<Integer, Game>();
	static int gameCount = 0;
	private static int[] rateShipPower = {3, -2 , -4}; //магические чиселки для вычисления мощности блока
	private int[] forts = {2, 2};
	
	Client p1=null, p2=null;
	public String name;
	State state = State.NEW;
	Ship[][] field = new Ship[15][16];
	int id;
	int playersReady = 0;
	int order = 0;
	int[] atdef = new int[4];
	int[][] attackers = new int[3][2];
	
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
				changeOrder(true);
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
		
		if(distance(i,j,y,x) != 1 || (field[i][j].type > 7 && field[i][j].type != 10)) {
			p.sndMsg("Вопрос не корректный");
			p.setState("ASK");
			return;
		} 
	
		if(field[y][x].type == 9) {  // Спросили атомную бомбу
			proceedBomb(op);
			changeOrder(true);
			return;
		} else if (field[i][j].type == 10 || field[y][x].type == 10) { //Спросили торпеду или торпедой
			if(field[y][x].type == 8) { //спросили форт торпедой
				p.sndMsg("Вы нашли форт противника (" + Integer.toString(y)+","+Integer.toString(x)+")");
				field[i][j] = null;
				p.deleteShip(i, j);
				op.deleteShip(i, j);
				changeOrder(false);
			} else { // Торпедой спросили торпеду
				field[y][x] = null;
				p.deleteShip(y, x);
				op.deleteShip(y, x);
				field[i][j] = null;
				p.deleteShip(i, j);
				op.deleteShip(i, j);
				changeOrder(true);
			}
			return;
		} else if (field[y][x].type == 11) { // Спросили мину
			p.sndMsg("Это МИНА");
			if (field[i][j].type == 6) { // Тральщик спросил мину, снимает ее с поля
				field[y][x] = null;
				p.deleteShip(y, x);
				op.deleteShip(y, x);
				changeOrder(false);
			} else { // Спросивший корабль подорвался на мине
				field[i][j] = null;
				p.deleteShip(i, j);
				op.deleteShip(i, j);
				changeOrder(true);
			}
			return;
		} else if(field[y][x].type == 8) { //Cпросили форт боевым кораблем
			p.deleteShip(y, x);
			op.deleteShip(y, x);
			killFort(p);
			changeOrder(false);
			return;
		}
		
		if(blockIsPossible(op, y, x)) {
			atdef[0] = i;
			atdef[1] = j;
			attackers[0][0] = i;
			attackers[0][1] = j;
			atdef[2] = y;
			atdef[3] = x;
			state = State.ANS;
			op.write("ANS;"+op.y(y)+";"+x+";");
			op.sndMsg("Что это?");
			return;
		}
		
		int attackerLength = findBlock(p, i, j, 0);
		int result = compareBlocks(attackerLength, field[i][j].type, 1, field[y][x].type);
		p.sndMsg("Это " + shipNames[field[y][x].type]);
		switch(result) { // что когда делаем
			case 1: //win
				field[y][x] = null;
				p.deleteShip(y, x);
				opponent(p).deleteShip(y, x);
				changeOrder(false);
				break;
			case -1: //loss
				field[i][j] = null;
				p.deleteShip(i, j);
				opponent(p).deleteShip(i, j);
				changeOrder(true);
				break;
			case 0: //equal
				field[y][x] = null;
				p.deleteShip(y, x);
				opponent(p).deleteShip(y, x);
				field[i][j] = null;
				p.deleteShip(i, j);
				opponent(p).deleteShip(i, j);
				changeOrder(true);
				break;
		}
	}
	
	private void killFort(Client p) {
		if(p == p1) {
			forts[0] -= 1;
			if(forts[0] == 0) {
				p2.write("WIN");
				p1.write("LOOSE");
				state = State.OVER;
			}
		} else {
			forts[1] -= 1;
			if(forts[1] == 0) {
				p1.write("WIN;");
				p2.write("LOOSE;");
				state = State.OVER;
			}
		}
	}
 
	public void ans(Client p, String[] block) {
		int k, i, j, x, y;
		String[] sh;
		int[][] defenders = new int[block.length][2];
		
		for(k=0; k < block.length; k++) {
			sh = block[k].split(",");
			defenders[k][0] = p.y(Integer.parseInt(sh[0]));
			defenders[k][1] = Integer.parseInt(sh[1]);
		}
		
		i = atdef[0];
		j = atdef[1];
		y = defenders[0][0];
		x = defenders[0][1];
		
		if(!IsBlockCorrect(defenders)) {
			p.sndMsg("Неправильный блок");
			p.write("ANS;"+p.y(atdef[2])+";"+atdef[3]+";");
			p.sndMsg("Что это?");
			return;
		}
		
		int attackerLength = findBlock(opponent(p), i, j, 0);
		int result = compareBlocks(attackerLength, field[i][j].type, defenders.length, field[y][x].type);
		
		opponent(p).sndMsg("Это " + Integer.toString(defenders.length)+ shipNames[field[y][x].type]);
		
		switch(result) { //что когда делаем
			case 1: //win
				deleteBlock(defenders, defenders.length);
				changeOrder(false);
				break;
			case -1: //loss
				field[i][j] = null;
				p.deleteShip(i, j);
				opponent(p).deleteShip(i, j);
				changeOrder(true);
				break;
			case 0: //equal
				deleteBlock(defenders, defenders.length);
				deleteBlock(attackers, attackerLength);
				changeOrder(true);
				break;
		}
	}
	
	private void changeOrder(boolean changes) {
		if(changes){	
			if(order==1) {
				order = 2;
				p2.setState("MOVE");
				p1.setState("WAITING");
			} else {
				order = 1;
				p1.setState("MOVE");
				p2.setState("WAITING");
			}
		} else {
			if(order==1) { // если в клиенте портятся статусы.
				p1.setState("MOVE");
				p2.setState("WAITING");
			} else {
				p2.setState("MOVE");
				p1.setState("WAITING");
			}
		}
		
		state = State.MOVE;
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

		if (step == 1) {
			attackers[0][0] = i;
			attackers[0][1] = j;
		}
	
		if(i-1 >= 0 && field[i-1][j]!=null && field[i-1][j].owner==p && field[i-1][j].type == t) {
			len+=1;
			attackers[len-1][0] = i-1;
			attackers[len-1][1] = j;
		}
		if(j-1 >= 0 && field[i][j-1]!=null && field[i][j-1].owner==p && field[i][j-1].type == t) {
			len+=1;
			attackers[len-1][0] = i;
			attackers[len-1][1] = j-1;
		}
		if(i+1 <= 14 && field[i+1][j]!=null && field[i+1][j].owner==p && field[i+1][j].type == t) {
			len+=1;
			attackers[len-1][0] = i+1;
			attackers[len-1][1] = j;
		}
		if(j+1 <= 15 && field[i][j+1]!=null && field[i][j+1].owner==p && field[i][j+1].type == t) {
			len+=1;
			attackers[len-1][0] = i;
			attackers[len-1][1] = j+1;
		}
		
		if(len==2 && step != 1) {
			return findBlock(p, attackers[len-1][0], attackers[len-1][1], 1);
		}
		
		if(len > 3) return 3;
		return len;
	} 
	
	private void deleteBlock(int[][] block, int len) {
		 for(int k = 0; k < len; k++) {
		 	field[block[k][0]][block[k][1]] = null;
			p1.deleteShip(block[k][0],block[k][1]);
			p2.deleteShip(block[k][0],block[k][1]);
		 }
	}
	
	public void bomb(Client p) {
		proceedBomb(p);
		changeOrder(true);
	}
	
	public void proceedBomb(Client p) {
		int bi = -1, bj = -1, k, t, top, bottom, right, left;
		Client op = opponent(p);
		
		/* find the bomb */
		for(k=0; k<15; k++) {
			for(t=0; t<16; t++) {
				if(field[k][t]!=null && field[k][t].type==9 && field[k][t].owner==p) {
					bi = k; bj = t;
					break;
				}
			}
		}
		
		if(bi<0) return; // Нету бомбы
		
		p.write("NOBOMB;"); // Убрать кнопку взрыва бомбы у игрока
		
		p.deleteShip(bi, bj);
		op.deleteShip(bi, bj);
		field[bi][bj] = null;
			
		top    = bi-2;
		bottom = bi+2;
		right  = bj+2;
		left   = bj-2;
		
		if (top < 0)     top = 0;
		if (bottom > 14) bottom = 14;
		if (left < 0)    left = 0;
		if (right > 15)  right = 15;
		
		for(k = top; k <= bottom; k++) {
			for(t = left; t <= right; t++) {
				if (field[k][t]!= null) {
					if(field[k][t].type == 9) { // Атомная бомба
						proceedBomb(op);
					} else {
						p.deleteShip(k, t);
						op.deleteShip(k, t);
						if(field[k][t].owner == p) {
							op.sndMsg("Взорвался " + shipNames[field[k][t].type]);
						} else {
							p.sndMsg("Взорвался " + shipNames[field[k][t].type]);
						}
						if(field[k][t].type == 8) // Форт
							killFort(field[k][t].owner);
						field[k][t] = null;
					}
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
			result += t.getKey().toString()+"-"+((Game)t.getValue()).name+",";
		}
		return result;
	}
	
	public static Game getGame(String gameId) {
		return games.get(Integer.valueOf(gameId));
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
		
		if(type1 == 7 && type2 == 1) {
			if(l1 >= 2 || (l1 == 1 && l2 != 2)) {
				power2 = 2;	power1 = 1;
			} else {
				power2 = 1;	power1 = 2;
			}
		}else if(type1 == 1 && type2 == 7) {
			if(l1 == 2 && l2 == 1) {
				power2 = 2;	power1 = 1;
			} else {
				power2 = 1;	power1 = 2;
			}
		}
		
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
				if (field[k][t]!=null && field[k][t].type==6 && field[k][t].owner==p) { //нашли тральщик
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
		
		if(t > 7) return false; // Не боевой корабль
		
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
