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
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

public class Gui extends JFrame {
	JFrame newGameFrm, selectGameFrm, setHostFrm;
	Gui mainFrm;
	Game game = null;
	Ship[][] field = new Ship[15][16];
	String host=null, port=null;
	
	public Gui() {
		super("Sea Game");
		setLayout(null);
		setBackground(Color.white);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 940);
		mainFrm = this;
	
		JMenuBar mbar = new JMenuBar();
		JMenu m1 = new JMenu("Игра");
		JMenu m2 = new JMenu("Настройки");
		JMenuItem i1, i2, i3, i4, i5;
		m1.add(i1 = new JMenuItem("Новая"));
		m1.add(i2 = new JMenuItem("Присоединиться"));
		m1.add(i3 = new JMenuItem("Закончить игру"));
		m1.add(i4 = new JMenuItem("Выйти"));
		m2.add(i5 = new JMenuItem("Настройки подключения"));
		mbar.add(m1);
		mbar.add(m2);
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
						game = new Game("a", "b", getHost(), getPort(), 0, mainFrm);
						newGameFrm.setVisible(false);
					}
				});
				newGameFrm.add(name);
				newGameFrm.add(ok);
				newGameFrm.setVisible(true);
			}
		});
		
		i2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectGameFrm = new JFrame("Выбор Игры");
				selectGameFrm.setSize(400, 300);
				selectGameFrm.setLayout(new FlowLayout());
				//Формируется список ожидающих игроков
				JButton ok = new JButton("Подключиться");
				ok.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e1) {
						if(game!=null) game.exit();
						game = new Game("Vasya", "", getHost(), getPort(), 1, mainFrm);
						selectGameFrm.setVisible(false);
					}
				});
				selectGameFrm.add(ok);
				selectGameFrm.setVisible(true);
			}
		});
		
		i3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				game.exit();
			}
		});
		
		i4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		i5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setHostFrm = new JFrame("Настройки");
				setHostFrm.setSize(275, 120);
				setHostFrm.setLayout(new FlowLayout());
				final JTextField hostTextField = new JTextField(getHost(), 17);
				final JTextField portTextField = new JTextField(getPort(), 17);
				JButton ok = new JButton("Подтвердить");
				JButton cansel = new JButton("Отменить");
				
				ok.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e1) {
						saveChanges(hostTextField.getText(),portTextField.getText());
						setHostFrm.setVisible(false);
					}
				});
				cansel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e1) {
						setHostFrm.setVisible(false);
					}
				});
				
				setHostFrm.add(new JLabel("Хост"));
				setHostFrm.add(hostTextField);
				setHostFrm.add(new JLabel("Порт"));
				setHostFrm.add(portTextField);
				setHostFrm.add(ok);
				setHostFrm.add(cansel);
				setHostFrm.setVisible(true);
			}
		});
		
		setVisible(true);
	}
	
	private void saveChanges(String hostNew, String portNew){
		hostNew+="\n";
		portNew+="\n";
		try {
			 FileWriter fw=new FileWriter("settings.txt");
			 fw.write(hostNew);
			 fw.write(portNew);
			 fw.close();
			 host=hostNew;
			 port=portNew;
		} catch(IOException exc) {}
	}
		
	private String getHost() {
		String strport;
		
		if(host==null) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader("settings.txt"));
				host = reader.readLine();
				strport = reader.readLine();
				reader.close();
			} catch(IOException exc) {
				host="";
			}
		}
		return host;
	}
	
	private String getPort() {
		String strhost;
		
		if(port==null) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader("settings.txt"));
				strhost = reader.readLine();
				port = reader.readLine();
				reader.close();
			} catch(IOException exc) {
				port="";
			}
		}
		return port;
	}
}
