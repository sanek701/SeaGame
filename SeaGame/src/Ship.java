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
	int x, y, type;
	boolean selected, enemy;
	enum Type {L, K, E, S, TK, TR, PL, F, A, T, M}
	
	public Ship(int t, int i, int j) {
		x = j;
		y = i;
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
								game.srv.moveShip(oldPosition.y, oldPosition.x, x, y);
							}
							
							oldPosition = null;
							selected = false;
							repaint();
						}
						break;
				}
			}
		});
	}
	
	public void paint(Graphics g) {
		Color c=g.getColor();
		if(y>=0) g.setColor(Color.blue);
		if(y>=5) g.setColor(Color.black);
		if(y>=10) g.setColor(Color.blue);
		if(type!=-1) {
			g.drawImage(lib[type], 1, 1, 34, 52, this);
		}
		if(selected || enemy) {
			g.setColor(Color.red);
			g.drawRect(1, 1, 34, 54);
		} else {
			g.drawRect(0, 0, 34, 54);
		}
		g.setColor(c);
		super.paint(g);
	}
	
	public void setType(int t){
		type=t;
		repaint();
	}
	
	public static void setGame(Game g) {
		game = g;
	}
	
	public static void setLib(Image[] l) {
		lib = l;
	}
}
