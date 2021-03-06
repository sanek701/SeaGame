import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.ButtonGroup;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

public class Gui extends JFrame {
	private static final long serialVersionUID = 1L;
	JFrame newGameFrm, selectGameFrm, setHostFrm,
		createShipFrm=null, deleteShipFrm=null, coefFrm=null;
	JLabel errorLbl;
	JTextArea msgBox;
	JScrollPane spane;
	JButton ready, test, bombButton;
	Gui mainFrm;
	Game game = null;
	Ship[][] field = new Ship[15][16];
	String host=null, port=null;
	static String[] shipNames = {"", "Линкор", "Крейсер", "Эсминец", "Сторожевик",
		"Торпедный катер", "Тральщик", "Подводная лодка",
		"Форт", "Атомная бомба", "Торпеда", "Мина"};
	static int coef = 1;
	
	public Gui() {
		super("Sea Game");
		setLayout(null);
		setBackground(Color.white);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 940);
		mainFrm = this;
		
		msgBox = new JTextArea();
		msgBox.setEditable(false);
		msgBox.setLineWrap(true);
		msgBox.setFocusable(false);
		spane = new JScrollPane(msgBox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		spane.setBounds(615, 570, 165, 250);
		add(spane);
		
			
		JMenuBar mbar = new JMenuBar();
		JMenu m1 = new JMenu("Игра");
		JMenu m2 = new JMenu("Настройки");
		
		JMenuItem i1, i2, i3, i4, i5, i6;
		m1.add(i1 = new JMenuItem("Новая"));
		m1.add(i2 = new JMenuItem("Присоединиться"));
		m1.add(i3 = new JMenuItem("Закончить игру"));
		m1.add(i4 = new JMenuItem("Выйти"));
		m2.add(i5 = new JMenuItem("Настройки подключения"));
		m2.add(i6 = new JMenuItem("Размер окна"));
		mbar.add(m1);
		mbar.add(m2);
		setJMenuBar(mbar);
		
		i1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showCreateGame();
			}
		});
		
		i2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showGameList();
			}
		});
		
		i3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(game!=null) game.exit();
				msgBox.setText("");
				bombButton.setVisible(false);
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
		
		i6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showCoefMenu();
			}
		});
		
		//---------------------TEMP---------------------
		test = new JButton("Player1");
		test.setBounds(660, 300, 100, 26);
		test.setVisible(true);
		test.setFocusable(false);
		test.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				game.test(1);
			}
		});
//		add(test);
		//---------------------END TEMP-----------------
		
		ready = new JButton("Завершить");
		ready.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				game.ready();
			}
		});
		ready.setBounds(615, 50, 165, 25);
		ready.setFocusable(false);
		ready.setVisible(false);
		add(ready);
		
		KeyAdapter kd = new KeyAdapter(){
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar()=='\n' && game!=null)
					game.ready();
			}
		};
		addKeyListener(kd);
		setFocusable(true);
		
		bombButton = new JButton("Бомба");
		bombButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (game.state == Game.State.ASK) {
					game.srv.bomb();
					bombButton.setVisible(false);
				} else {
					addMsg("Сейчас не Ваш ход");
				}
			}
		});
		bombButton.setBounds(615, 100, 165, 25);
		bombButton.setFocusable(false);
		bombButton.setVisible(false);
		add(bombButton);
		
		importImages();
		initField();
		setVisible(true);
	}
	
	private void initField() {
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
	
	public void emptyField() {
		int i, j;
		for(i=0; i<15; i++) {
			for(j=0; j<16; j++) {
				field[i][j].setType(-1);
			}
		}
	}
	
	public void showCreateShip(int x, int y) {
		if(createShipFrm != null) createShipFrm.setVisible(false);
		createShipFrm = new JFrame("Выберите Тип Корабля");
		JRadioButton[] type = new JRadioButton[11];
		ButtonGroup bg = new ButtonGroup();
		createShipFrm.setLayout(new BoxLayout(createShipFrm.getContentPane(), BoxLayout.Y_AXIS));
		createShipFrm.setBounds(200,200,250,280);
			
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
					game.srv.createShip(p, q, k);
				}
			});
			bg.add(type[i-1]);
			type[i-1].setAlignmentX(Component.LEFT_ALIGNMENT);
			createShipFrm.getContentPane().add(type[i-1]);
		}
		createShipFrm.setVisible(true);
	}
	
	public void showDeleteShip(final int x,final int y) {
		if(deleteShipFrm != null) deleteShipFrm.setVisible(false);
		deleteShipFrm = new JFrame("Удаление");
		
		JButton ok= new JButton("Да");
		JButton cansel= new JButton("Нет");
		JLabel lab= new JLabel("Хотите удалить этот корабль?");
		deleteShipFrm.setBounds(400, 400, 240, 100);
		deleteShipFrm.setLayout(new FlowLayout());
		
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				game.srv.deleteShip(x, y);
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
	
	public void addMsg(String s) {
		msgBox.append("> "+s+"\n");
		msgBox.setCaretPosition(msgBox.getDocument().getLength());
	}
	
	public void setGame(Game g) {
		game = g;
	}
	
	public void showReadyButton(boolean visible){
		ready.setVisible(visible);
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
	
	private void showCreateGame() {
		newGameFrm = new JFrame("Создание Игры");
		newGameFrm.setSize(400, 175);
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
				game = new Game(pName.getText(), gName.getText(), getHost(), getPort(), "", mainFrm);
				newGameFrm.setVisible(false);
			}
		});
		
		newGameFrm.add(ok);
		newGameFrm.setVisible(true);
	}
	
	private void showGameList() {
		final String[][] gameList = Connection.getGameList(getHost(), getPort(), this);
				
		selectGameFrm = new JFrame("Выбор Игры");
		selectGameFrm.setSize(400, 25*gameList.length+60);
		
		selectGameFrm.add(new JLabel("Ваше Имя"));
		final JTextField pName = new JTextField("Игрок", 17);
		selectGameFrm.getContentPane().add(pName);
		
		JRadioButton[] rb = new JRadioButton[gameList.length];
		final ButtonGroup bg = new ButtonGroup();
		selectGameFrm.setLayout(new BoxLayout(selectGameFrm.getContentPane(), BoxLayout.Y_AXIS));
		
		for(int i = 0; i < gameList.length; i++) {
			final int j = i;
			rb[i]= new JRadioButton(gameList[i][1]+"\n", false);
			rb[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selectGameFrm.setVisible(false);
					if(game!=null) game.exit();
					game = new Game(pName.getText(), "", getHost(), getPort(), gameList[j][0], mainFrm);
				}
			});
			bg.add(rb[i]);
			rb[i].setAlignmentX(Component.LEFT_ALIGNMENT);
			selectGameFrm.getContentPane().add(rb[i]);
		}
		
		selectGameFrm.setVisible(true);
	}
	
	public void showInfoWindow(String text) {
		final JFrame fr = new JFrame("Ифнормация");
		fr.setSize(150, 100);
		fr.setLayout(new FlowLayout());
		fr.add(new JLabel(text));
		JButton ok = new JButton("Закрыть");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e1) {
				fr.setVisible(false);
			}
		});
		fr.add(ok);
		fr.setVisible(true);
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
	
	public void showCoefMenu() {
		if(coefFrm != null) coefFrm.setVisible(false);
		coefFrm = new JFrame("Масштаб");
		coefFrm.setBounds(150,300,250,120);
		coefFrm.setLayout(new FlowLayout());
		JLabel lab1 = new JLabel("Уменьшить поле в \n");
		JLabel lab2 = new JLabel("раза. (только целые числа!)");
		coefFrm.add(lab1);
		JButton  ok = new JButton("Ok");
		JButton  cansel = new JButton("Отмена");
		final JTextField coeff = new JTextField("1", 5);
		
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e1) {
				setCoef(Integer.parseInt(coeff.getText()));
				coefFrm.setVisible(false);
			}
		});
		
		cansel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e1) {
				coefFrm.setVisible(false);
			}
		});
		
		coefFrm.add(lab1);
		coefFrm.add(coeff);
		coefFrm.add(lab2);
		coefFrm.add(ok);
		coefFrm.add(cansel);
		coefFrm.setVisible(true);
	}
	
	public void setCoef(int i) {
		if (i!=0) coef = i;
		setSize(800/coef, 940/coef);
		Ship.updateCoef();		
		updateField();
		ready.setBounds(660/coef, 20/coef, 100/coef, 26/coef);
		bombButton.setBounds(660/coef, 50/coef, 100/coef, 26/coef);
		spane.setBounds(620/coef, 550/coef, 155/coef, 270/coef);
		//tmp
		test.setBounds(660/coef, 300/coef, 100/coef, 26/coef);
		//
		
	}
	
	public void updateField() {
		int i, j;
		for(i=0; i<15; i++) {
			for(j=0; j<16; j++) {
				field[i][j].setBounds(j*36/coef+30/coef, i*54/coef+16/coef, 36/coef+1, 54/coef+1);
				field[i][j].repaint();
			}
		}
	}
}
