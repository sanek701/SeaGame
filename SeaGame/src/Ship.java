import javax.swing.JComponent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics;

public class Ship extends JComponent {
	private static final long serialVersionUID = 1L;
	static Game game = null;
	static Image[] lib = null;
	static Ship oldPosition = null;
	static Ship[] block = {null, null, null};
	static Ship attacker = null;
	int x, y, type;
	boolean selected;
	
	public Ship(int t, int i, int j) {
		y = i;
		x = j;
		type = t;
		final Ship thisShip = this;
		
		addMouseListener(new MouseAdapter( ) {
			public void mousePressed(MouseEvent me) {
				if(game==null) return;
				switch(game.state) {
					case CREATESHIPS:
						if(type==-1 && game.shipSum < 52) {
							game.gui.showCreateShip(y, x);
						} else if(type>0) { 
							game.gui.showDeleteShip(y, x);
						}
						break;
					case MOVE:
						if(oldPosition==null) {
							if(type<=0) break; // Enemy or an empty field
							oldPosition = thisShip;
							selected = true;
							repaint();
						} else {
							if(oldPosition != thisShip) {
								if(type!=-1) break; //Not an empty field
								game.srv.moveShip(oldPosition.y, oldPosition.x, y, x);
								oldPosition.selected = false;
								oldPosition.repaint();
							}
							
							oldPosition = null;
							selected = false;
							repaint();
						}
						break;
					case ASK:
						if(type==0) { //Enemy
							game.srv.askShip(attacker.y, attacker.x,
									thisShip.y, thisShip.x);
							attacker.selected = false;
							attacker.repaint();
							attacker = null;
							break;
						}
						
						if(selected) {
							attacker.selected = false;
							attacker.repaint();
							attacker = null;
						} else {
							if(type<=0) break;
							if(attacker!=null) {
								attacker.selected = false;
								attacker.repaint();
							}
							attacker = thisShip;
							attacker.selected = true;
							attacker.repaint();
						}
						break;
					case ANS:
						if(selected) {
							if(thisShip == block[0])
								break;
							deleteFromBlock(thisShip);
						} else {
							if(type<=0 || fullBlock())
								break;
							addToBlock(thisShip);
						}
						break;
				}
			}
		});
	}
	
	public void paint(Graphics g) {
		Color c = g.getColor();
		if(y>=0) g.setColor(Color.blue);
		if(y>=5) g.setColor(Color.black);
		if(y>=10) g.setColor(Color.blue);
		if(type!=-1) {
			g.drawImage(lib[type], 1, 1, 34, 52, this);
		}
		if(selected) {
			g.setColor(Color.red);
			g.drawRect(1, 1, 34, 54);
		} else {
			g.drawRect(0, 0, 34, 54);
		}
		g.setColor(c);
		super.paint(g);
	}
	
	public void setType(int t){
		type = t;
		repaint();
	}
	
	public static void setGame(Game g) {
		game = g;
	}
	
	public static void setLib(Image[] l) {
		lib = l;
	}
	
	private boolean fullBlock() {
		if(block[0]!=null && block[1]!=null
				&& block[2]!=null) return true;
		return false;
	}
	
	private void addToBlock(Ship s) {
		for(int i=0; i<3; i++) {
			if(block[i]==null) {
				block[i] = s;
				s.selected = true;
				s.repaint();
				return;
			}
		}
	}
	
	private void deleteFromBlock(Ship s) {
		for(int i=0; i<3; i++) {
			if(block[i]==s) {
				block[i] = null;
				s.selected = false;
				s.repaint();
				return;
			}
		}
	}
	
	public void freeBlock() {
		for(int i=0; i<3; i++) {
			if(block[i]!=null) {
				block[i].selected = false;
				block[i].repaint();
				block[i] = null;
			}
		}
	}
}
