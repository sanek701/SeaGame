import javax.swing.JComponent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics;

public class Ship extends JComponent {
	static Game game = null;
	static Image[] lib = null;
	int x, y, type;
	public static int sum;
	boolean selected, enemy;
	static String[] names = {"","Линкор","Крейсер", "Эсминец","Сторожевик",
			"Торпедный катер", "Тральщик", "Подводная лодка",
			"Форт", "Атомная бомба", "Торпеда", "Мина"};
	enum Type {L, K, E, S, TK, TR, PL, F, A, T, M}
	final static int[] normalCount = {-1, 2, 5, 6, 6, 6, 6, 6, 2, 1, 6, 6};
	public static int[] count =  {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
	public Ship(int t, int i, int j) {
		x = i;
		y = j;
		type = t;
		
		addMouseListener(new MouseAdapter( ) {
			public void mousePressed(MouseEvent me) {
				if(game==null) return;
				switch(game.state) {
					case SCRT:
						if(type==-1 && sum < 52) {
							game.gui.showCreateShip(x, y);
						} else if(type>0) { 
							game.gui.showDeletShip(x, y);
						}
				}
			}
		});
	}
	
	public void paint(Graphics g) {
		Color c=g.getColor();
		if(x>=0) g.setColor(Color.blue);
		if(x>=5) g.setColor(Color.black);
		if(x>=10) g.setColor(Color.blue);
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
	}
	
	public static void setGame(Game g) {
		game = g;
	}
	
	public static void setLib(Image[] l) {
		lib = l;
	}
}
