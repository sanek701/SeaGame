import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Gui extends JFrame {
	JFrame newGameFrm;
	Gui mainFrm;
	Game game = null;
	Ship[][] field = new Ship[15][16];
	
	public Gui() {
		super("Sea Game");
		setLayout(null);
		setBackground(Color.white);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 940);
		mainFrm = this;
	
		JMenuBar mbar = new JMenuBar();
		JMenu m1 = new JMenu("Игра");
		JMenuItem i1, i2, i3, i4;
		m1.add(i1 = new JMenuItem("Новая"));
		m1.add(i2 = new JMenuItem("Присоединиться"));
		m1.add(i3 = new JMenuItem("Закончить игру"));
		m1.add(i4 = new JMenuItem("Выйти"));
		mbar.add(m1);
		setJMenuBar(mbar);
		
		i1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newGameFrm = new JFrame("Создание Игры");
				newGameFrm.setSize(400, 300);
				newGameFrm.setLayout(new FlowLayout());
				newGameFrm.add(new JLabel("Название Игры"));
				JTextField name = new JTextField("Моя игра", 32);
				JButton ok = new JButton("Создать");
				ok.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e1) {
						if(game!=null) game.exit();
						game = new Game("a", "b", "c", "d", 0, mainFrm);
						newGameFrm.setVisible(false);
					}
				});
				newGameFrm.add(name);
				newGameFrm.add(ok);
				newGameFrm.setVisible(true);
			}
		});
		
		setVisible(true);
	}
	
}
