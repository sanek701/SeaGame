import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;


public class Gui extends JFrame {
	JFrame newGameFrm, selectGameFrm, setHostFrm, createShipFrm=null, deleteShipFrm=null;
	Gui mainFrm;
	Game game = null;
	Ship[][] field = new Ship[15][16];
	String host=null, port=null;
	static String[] shipNames = {"","Линкор","Крейсер", "Эсминец","Сторожевик",
		"Торпедный катер", "Тральщик", "Подводная лодка",
		"Форт", "Атомная бомба", "Торпеда", "Мина"};
	
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
				final JTextField gName = new JTextField("Моя игра", 32);
				newGameFrm.add(gName);
				newGameFrm.add(new JLabel("Имя Игрока"));
				final JTextField pName = new JTextField("Вася", 32);
				newGameFrm.add(pName);
				JButton ok = new JButton("Создать");
				ok.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e1) {
						if(game!=null) game.exit();
						game = new Game(pName.getText(), gName.getText(), getHost(), getPort(), 0, mainFrm);
						newGameFrm.setVisible(false);
					}
				});
				
				newGameFrm.add(ok);
				newGameFrm.setVisible(true);
			}
		});
		
		i2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[][] gameList = Connection.getGameList(getHost(), getPort());
				
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
						saveChanges(hostTextField.getText(), portTextField.getText());
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
		
		importImages();
		createField();
		setVisible(true);
	}
	
	public void createField() {
		int i, j;
		for(i=0; i<15; i++) {
			for(j=0; j<16; j++) {
				Ship t = new Ship(-1, i, j);
				field[i][j] = t;
				t.setBounds(j*36+30, i*54+16, 36, 54);
				add(t);
			}
		}
	}
	
	public void showCreateShip(int x, int y) {
		if(createShipFrm != null) createShipFrm.setVisible(false);
		createShipFrm = new JFrame("Выберите Тип Корабля");
		JRadioButton[] type = new JRadioButton[11];
		ButtonGroup bg = new ButtonGroup();
		createShipFrm.setLayout(new BoxLayout(createShipFrm.getContentPane(), BoxLayout.Y_AXIS));
		createShipFrm.setBounds(200,200,250,270);
			
		final int p = x;
		final int q = y;
		int[] sCnt = game.shipCnt;
		int[] nsCnt = Game.normalShipCnt;
		
		for(int i=1; i<=11; i++) {
			if(sCnt[i] == nsCnt[i]) continue;
			final int k = i;
			type[i-1] = new JRadioButton(shipNames[i]+"("+sCnt[i]+"/"+nsCnt[i]+")\n", false);
			type[i-1].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					createShipFrm.setVisible(false);
					game.createShip(p, q, k);
				}
			});
			bg.add(type[i-1]);
			type[i-1].setAlignmentX(Component.LEFT_ALIGNMENT);
			createShipFrm.getContentPane().add(type[i-1]);
		}
		createShipFrm.setVisible(true);
	}
	
	public void showDeleteShip(final int x,final int y){
		if(deleteShipFrm != null) deleteShipFrm.setVisible(false);
		deleteShipFrm = new JFrame("Удаление");
		
		JButton ok= new JButton("Да");
		JButton cansel= new JButton("Нет");
		JLabel lab= new JLabel("Хотите удалить этот корабль?");
		deleteShipFrm.setBounds(400, 400, 240, 100);
		deleteShipFrm.setLayout(new FlowLayout());
		
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				game.deleteShip(x, y, field[x][y].type);
				deleteShipFrm.setVisible(false);
			}
		});

		cansel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteShipFrm.setVisible(false);
			}
		});			
  
		deleteShipFrm.add(lab);
		deleteShipFrm.add(ok);
		deleteShipFrm.add(cansel);
		deleteShipFrm.setVisible(true);
	}
	
	private void importImages() {
		Image[] lib= new Image[12];
		String fileName = "";
		
		for ( int i = 0 ; i < 12 ; i++ ) {
			fileName = "../images/" + Integer.toString(i) + ".jpg";
			lib[i]=this.getToolkit().getImage(fileName);
		}
		Ship.setLib(lib);
	}
	
	private void saveChanges(String hostNew, String portNew) {
		host = hostNew;
		port = portNew;
		try {
			 hostNew += "\n";
			 FileWriter fw = new FileWriter("settings.txt");
			 fw.write(hostNew);
			 fw.write(portNew);
			 fw.close();
		} catch(IOException exc) {}
	}
		
	private String getHost() {
		if(host==null) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader("settings.txt"));
				host = reader.readLine();
				port = reader.readLine();
				reader.close();
			} catch(IOException exc) {
				host="localhost";
			}
		}
		return host;
	}
	
	private String getPort() {
		if(port==null) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader("settings.txt"));
				host = reader.readLine();
				port = reader.readLine();
				reader.close();
			} catch(IOException exc) {
				port="3000";
			}
		}
		return port;
	}
}
