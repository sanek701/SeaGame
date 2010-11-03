import javax.swing.JComponent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Ship extends JComponent {
	static Game game;
	int x, y, type;
	boolean selected;
	
	public Ship() {
		addMouseListener(new MouseAdapter( ) {
			public void mousePressed(MouseEvent me) {
				
			}
		});
	}
	
	public static void setGame(Game g) {
		game = g;
	}
}
