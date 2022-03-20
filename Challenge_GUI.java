
import javax.swing.JFrame;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import javax.swing.border.LineBorder;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Cursor;

import javax.swing.JSeparator;
import static java.lang.Thread.sleep;


/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */

public class Challenge_GUI {

	private JFrame frame;
	//game gui
	private JFrame parentframe;
	//login gui
	private JFrame ppframe;
	private JTextField textField;
	
	String numero=null;
	Integer i=1;
	private String nickUtente;
	private String nickAmico;
	
	//elementi dell'interfaccia
	private JLabel lblSfids;
	private JLabel lblWinner;
	private JLabel lblperd;
	private JLabel lblwin;
	private JLabel lblgoal;
	private JLabel username;
	private JLabel lblVs;
	private JLabel userfriend;
	private JLabel lbltime ;
	private JLabel lbltrad;
	private JLabel label;
	private JButton btnOk;

	private JButton btnabbandona;
	private JButton btnInizia;
	private JButton btnProssimaParola;
	private JLabel lblnum;
	
	private JLabel lblitaly;
	

	//implementazione send e receive
	private MsgTCP msgTCP;
	//comunicazione udp
	private ClientUDP ClientUDP;
	
	private String parola_da_tradurre;
	
	//partita
	private JLabel lblok;
	private JLabel lblno;
	private JLabel lblen;
	private JLabel lblit;
	private JButton btnInvio;
	private JButton btnFinePartita;
	
	private JSeparator separator;
	private JSeparator separator_1;
	
	//fine partita
	private JButton btnMostraRisultati;
	private JLabel lblesito;
	private JButton btnTornaAllaHome;
	
	private JButton btnOkGrazie;
	private JLabel lblabbandona;
	
	
	//INFO
	private JLabel lblinfo;
	private JLabel lbli;
	private JLabel lblintro;
	private JLabel lblHelp;
	
	//timer
	private JLabel lbltimer;
	private int seconds;
	private Timer timer;
	
	private boolean stoppato=false;
	
	/**
	 * Create the application.
	 * @throws InterruptedException 
	 */
	public Challenge_GUI(String nickUtente, String nickAmico, JFrame parentframe, JFrame ppframe,SocketChannel sc,ClientUDP clientUDP) throws InterruptedException {
		this.nickUtente=nickUtente;
		this.nickAmico=nickAmico;
		this.parentframe=parentframe;
		this.ppframe=ppframe;
		
		//per riattivare l'ascolto di richieste al termine della sfida (torna alla home)
		this.ClientUDP=clientUDP;
		//riferimento alla GUI per sfruttare le operazioni di invio e ricezione messaggi
		msgTCP = new MsgTCP(sc);
		
		this.parola_da_tradurre=null;
		
		//per dare tempo di caricare al server
		sleep(500);
		initialize();	
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		
		frame = new JFrame();
		
		frame.getContentPane().setBackground(new Color(153, 204, 255));
		frame.getContentPane().setLayout(null);
		parentframe.setVisible(false);
		frame.setVisible(true);
		frame.isAlwaysOnTop();
		frame.setResizable(false); 
		
		frame.setSize(80,80);
		
		//__________  *** BOTTONE "TORNA ALLA HOME"  ***____________// ->visibile alla fine della partita
		
		btnTornaAllaHome = new JButton("Torna alla home");
		btnTornaAllaHome.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnTornaAllaHome.setOpaque(true);
		btnTornaAllaHome.setBorder(new LineBorder(new Color(0, 0, 0)));
		btnTornaAllaHome.setBackground(Color.RED);
		btnTornaAllaHome.setVisible(false);
		btnTornaAllaHome.addMouseListener(new MouseAdapter() {
		
			@Override
			public void mouseClicked(MouseEvent e) {
				
				//in cui riattivo
				FineSfida();
				
			}
			});
		
		btnTornaAllaHome.setBounds(269, 244, 117, 27);
		frame.getContentPane().add(btnTornaAllaHome);
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		//_______  *** ABBANDONA PARTITA  ***  __________//

		lblabbandona = new JLabel("<html><center>Ci dispiace che tu abbia <br>abbandonato la partita.<br> <font color='blue'><font size=16>A presto "+nickUtente+"!</font> <br><br> Torna alla home per continuare.</center></html>");
		lblabbandona.setForeground(new Color(255, 0, 51));
		lblabbandona.setHorizontalTextPosition(SwingConstants.CENTER);
		lblabbandona.setHorizontalAlignment(SwingConstants.CENTER);
		lblabbandona.setFont(new Font("Savoye LET", Font.BOLD, 20));
		lblabbandona.setBounds(213, 51, 229, 197);
		frame.getContentPane().add(lblabbandona);
		lblabbandona.setVisible(false);
		
		btnabbandona = new JButton("ABBANDONA");
		btnabbandona.setVisible(false);
		btnabbandona.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnabbandona.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
					if (JOptionPane.showConfirmDialog(btnabbandona, "Sei sicuro di voler abbandonare la sfida? \n Regalerai automaticamente 10 punti al tuo avversario!","Conferma", JOptionPane.YES_OPTION)==0) {
						
						timer.stop();
						//serve se entrambi clicckano su abbandona consecutivamente
						stoppato=true;
						timer=null;
						Abbandona();
						
						btnTornaAllaHome.setVisible(true);
						lblabbandona.setVisible(true);
						
						separator.setVisible(false);
						lbltime.setVisible(false);
						username.setVisible(false);
						userfriend.setVisible(false);
						
					
					}
			}
		});
		btnabbandona.setOpaque(true);
		btnabbandona.setBackground(new Color(255, 51, 0));
		btnabbandona.setBorder(new LineBorder(new Color(0, 0, 0)));
		btnabbandona.setFont(new Font("Lucida Grande", Font.BOLD, 9));
		btnabbandona.setBounds(6, 31, 73, 27);
		frame.getContentPane().add(btnabbandona);
		btnabbandona.setVisible(false);


			
				
				
		//_______   *** INTRODUZIONE  *** ________//
		
		
		//OK GRAZIE HELP 
		btnOkGrazie = new JButton("OK, GRAZIE");
		btnOkGrazie.setBounds(195, 242, 85, 29);
		btnOkGrazie.setMnemonic(KeyEvent.VK_DEAD_ABOVEDOT);
		btnOkGrazie.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnOkGrazie.setForeground(new Color(0, 0, 0));
		btnOkGrazie.setBackground(new Color(255, 255, 255));
		//non visibile subito, solo quando pigio su help
		btnOkGrazie.setVisible(false);
		
		btnOkGrazie.setFont(new Font("Raanana", Font.BOLD, 9));
		btnOkGrazie.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				btnOkGrazie.setVisible(false);
				lblintro.setVisible(false);
				
			}
		});
		frame.getContentPane().add(btnOkGrazie);
		
		//OK GRAZIE PRIMA VOLTA
		btnOk = new JButton("OK, GRAZIE");
		btnOk.setMnemonic(KeyEvent.VK_DEAD_ABOVEDOT);
		btnOk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnOk.setForeground(new Color(0, 0, 0));
		btnOk.setBackground(new Color(255, 255, 255));
		btnOk.setVisible(true);
		btnOk.setFont(new Font("Raanana", Font.BOLD, 10));
		btnOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				btnabbandona.setVisible(false);
				btnOk.setVisible(false);
				lblintro.setVisible(false);
				btnInizia.setVisible(true);
			}
		});
		btnOk.setBounds(185, 242, 95, 27);
		frame.getContentPane().add(btnOk);
	
		
		lblintro = new JLabel("<html><center><font size=3>REGOLE DEL GIOCO:</font><br><font color='green'> Fai click su INIZIA SFIDA, partirà il timer e comparirà la prima parola. </font><br>---> Hai <font color='red'>50 secondi </font>per tradurre <font color='red'>5 parole</font>.<br>---> Una parola alla volta comparirà sullo schermo a destra, scrivi la traduzione inglese corrispondente e <font color='fuchsia'> fai click su VERIFICA TRADUZIONE.</font><br>---> Guadagnerai <font color='red'>3 punti</font> per ogni risposta esatta, perderai <font color='red'>1 punto </font>per ogni parola errata.<br>---> Poi <font color='purple'> fai click su PROSSIMA PAROLA </font>per visualizzare un'altra parola da tradurre.<br> ---> Infine <font color='blue'>fai click su FINE PARTITA </font>per conoscere l'esito della sfida. <br>Se non ti ricordi le regole puoi fare <font color='grey'>click su HELP.</font> <br><b>Buon divertimento!</b></center></html>");
		lblintro.setForeground(new Color(0, 0, 0));
		lblintro.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lblintro.setVisible(true);
				
			}
		});
		lblintro.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblintro.setFont(new Font("Noteworthy", Font.PLAIN, 11));
		lblintro.setHorizontalTextPosition(SwingConstants.CENTER);
		lblintro.setHorizontalAlignment(SwingConstants.CENTER);
		lblintro.setOpaque(true);
		lblintro.setBackground(new Color(255, 255, 255));
		lblintro.setBounds(79, 12, 330, 232);
		frame.getContentPane().add(lblintro);
		
		lblHelp = new JLabel("HELP");
		lblHelp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblHelp.setForeground(new Color(0, 0, 205));
		lblHelp.setOpaque(true);
		lblHelp.setBackground(new Color(255, 255, 255));
		lblHelp.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblHelp.setHorizontalTextPosition(SwingConstants.CENTER);
		lblHelp.setHorizontalAlignment(SwingConstants.CENTER);
		lblHelp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lblintro.setVisible(true);
				btnOkGrazie.setVisible(true);
			}
		});
		lblHelp.setBounds(402, 12, 40, 27);
		frame.getContentPane().add(lblHelp);
		
		
		//__________   ***   INFO   ***   __________//

		lblinfo = new JLabel("<html><center>Non sono ammessi nè spazi nè caratteri speciali <br><. : ; / ,*+#></center></html>");
		lblinfo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblinfo.setHorizontalTextPosition(SwingConstants.LEFT);
		lblinfo.setHorizontalAlignment(SwingConstants.LEFT);
		lblinfo.setFont(new Font("Merriweather", Font.PLAIN, 6));
		lblinfo.setVisible(false);
		
		lblinfo.setBounds(339, 51, 78, 51);
		frame.getContentPane().add(lblinfo);
		

		lbli = new JLabel("i");
		lbli.setBackground(new Color(0, 0, 0));
		lbli.setOpaque(true);
		lbli.setForeground(new Color(255, 255, 255));
		lbli.setFont(new Font("ITF Devanagari", Font.BOLD, 19));
		lbli.setHorizontalAlignment(SwingConstants.CENTER);
		lbli.setHorizontalTextPosition(SwingConstants.CENTER);
		lbli.setBounds(424, 65, 18, 16);
		lbli.setVisible(false);
		frame.getContentPane().add(lbli);
		lbli.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				lblinfo.setVisible(true);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				lblinfo.setVisible(false);
			}
		});
		
		
		
		//______   *** TITOLO  *** _______//
		
		lblSfids = new JLabel("Word Quizzle Challenge");
		lblSfids.setForeground(new Color(0, 0, 255));
		lblSfids.setFont(new Font("Winter Festival", Font.PLAIN, 34));
		lblSfids.setBounds(79, -7, 303, 53);
		frame.getContentPane().add(lblSfids);
		
		//_________   ***   TABELLA RISULTATI   ***   ____________//
		
		
		
		//LABEL in cui compaiono i risultati della sfida
		lblesito = new JLabel("");
		lblesito.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblesito.setHorizontalAlignment(SwingConstants.CENTER);
		lblesito.setHorizontalTextPosition(SwingConstants.CENTER);
		lblesito.setBackground(new Color(255, 255, 0));
		lblesito.setFont(new Font("Bodoni 72", Font.BOLD, 12));
		lblesito.setOpaque(true);
		lblesito.setBounds(24, 70, 179, 160);
		frame.getContentPane().add(lblesito);		
		lblesito.setVisible(false);			
				
		//_______  *** VINCITORE ***_________//

		lblWinner = new JLabel("winner");
		lblWinner.setForeground(Color.BLUE);
		lblWinner.setHorizontalAlignment(SwingConstants.CENTER);
		lblWinner.setHorizontalTextPosition(SwingConstants.CENTER);
		lblWinner.setFont(new Font("Myanmar MN", Font.BOLD, 13));
		lblWinner.setBounds(3, 56, 188, 188);
		frame.getContentPane().add(lblWinner);
		lblWinner.setVisible(false);
		
		
	
		lblgoal = new JLabel("");
		lblgoal.setBounds(203, 10, 265, 220);
		frame.getContentPane().add(lblgoal);
		Image img_goal = new ImageIcon(this.getClass().getResource("/coppa.png")).getImage();
		lblgoal.setIcon(new ImageIcon(img_goal));
		lblgoal.setVisible(false);
		
		
		lblwin = new JLabel("");
		lblwin.setForeground(Color.BLUE);
		lblwin.setBounds(-15, -7, 483, 293);
		frame.getContentPane().add(lblwin);
		Image img_win = new ImageIcon(this.getClass().getResource("/winn.png")).getImage();
		lblwin.setIcon(new ImageIcon(img_win));
		lblwin.setVisible(false);
		
		
		//_________  *** PERDENTE  *** ______//
		
		lblperd = new JLabel("");
		lblperd.setBounds(235, 82, 226, 189);
		
		Image img_perd = new ImageIcon(this.getClass().getResource("/sad.png")).getImage();
		lblperd.setIcon(new ImageIcon(img_perd));
		lblperd.setVisible(false);
		frame.getContentPane().add(lblperd);
		

		//_________ ***   TIMER    ***  __________//
		
		
		
		lbltimer = new JLabel(" ");
		lbltimer.setHorizontalTextPosition(SwingConstants.CENTER);
		lbltimer.setHorizontalAlignment(SwingConstants.CENTER);
		lbltimer.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lbltimer.setBounds(199, 57, 139, 24);
		frame.getContentPane().add(lbltimer);
		lbltimer.setVisible(false);
	
		
		
		//___________   ***   BOTTONE "INIZIA SFIDA"  ***   __________//
		
		btnInizia = new JButton("INIZIA SFIDA");
		btnInizia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		//visibile subito
		btnInizia.setVisible(false);
		
		btnInizia.setOpaque(true);
		btnInizia.setBackground(new Color(102, 204, 0));
		btnInizia.setBorder(new LineBorder(new Color(0, 0, 0)));
		btnInizia.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				
				//conteggio parole-> i vale 1;
				numero= String.valueOf(i).toString();
				lblnum.setText(numero);
				
				IniziaSfida();
				textField.setVisible(true);
				lblitaly.setVisible(true);
				label.setVisible(true);
				lbltrad.setVisible(true);
				lblnum.setVisible(true);
				lblen.setVisible(true);
				lblit.setVisible(true);
				btnInvio.setVisible(true);
				btnInizia.setVisible(false);
				btnabbandona.setVisible(true);
				lbli.setVisible(true);
				
			}
		});
		
		btnInizia.setBounds(59, 238, 101, 34);
		frame.getContentPane().add(btnInizia);		
		
		
		
		
		//______________   ***  bottone MOSTRA RISULTATI  ***   _______________//
		
		
		
		btnMostraRisultati = new JButton("Mostra risultati");
		btnMostraRisultati.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnMostraRisultati.setForeground(Color.BLUE);
		btnMostraRisultati.setOpaque(true);
		btnMostraRisultati.setBorder(new LineBorder(new Color(0, 0, 0)));
		btnMostraRisultati.setBackground(Color.YELLOW);
		btnMostraRisultati.setVisible(false);
		btnMostraRisultati.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lblWinner.setVisible(false);
				lblesito.setVisible(true);
			}
		});
		btnMostraRisultati.setBounds(49, 242, 134, 29);
		frame.getContentPane().add(btnMostraRisultati);
		btnMostraRisultati.setVisible(false);
		
		
		
		//__________   *** INTRODUZIONE SFIDA  ***   ____________//
		
		
		
		username = new JLabel(nickUtente);
		username.setForeground(new Color(255, 255, 255));
		username.setHorizontalAlignment(SwingConstants.CENTER);
		username.setHorizontalTextPosition(SwingConstants.CENTER);
		username.setBounds(93, 85, 61, 27);
		username.setOpaque(true);
		username.setBackground(new Color(0, 0, 204));
		username.setFont(new Font("Raanana", Font.BOLD | Font.ITALIC, 16));
		frame.getContentPane().add(username);
		
		lblVs = new JLabel("VS");
		lblVs.setForeground(new Color(0, 0, 102));
		lblVs.setFont(new Font("Lucida Grande", Font.PLAIN, 33));
		lblVs.setBounds(79, 137, 61, 43);
		frame.getContentPane().add(lblVs);
		
		
		userfriend = new JLabel(nickAmico);
		userfriend.setForeground(new Color(255, 255, 255));
		userfriend.setHorizontalTextPosition(SwingConstants.CENTER);
		userfriend.setHorizontalAlignment(SwingConstants.CENTER);
		userfriend.setOpaque(true);
		userfriend.setBackground(new Color(0, 0, 204));
		userfriend.setFont(new Font("Raanana", Font.BOLD | Font.ITALIC, 16));
		userfriend.setBounds(45, 206, 61, 27);
		frame.getContentPane().add(userfriend);
		
		
	
		//immagine sfondo
		
		lbltime = new JLabel("");
		lbltime.setBounds(6, 20, 186, 272);
		frame.getContentPane().add(lbltime);
		Image img_time = new ImageIcon(this.getClass().getResource("/TIME.png")).getImage();
		lbltime.setIcon(new ImageIcon(img_time));	
		
		//_________  *** INSERISCI TRADUZIONE  ***  _________//
		
		lbltrad = new JLabel("TRADUZIONE");
		lbltrad.setForeground(new Color(0, 0, 153));
		lbltrad.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
		lbltrad.setBounds(235, 148, 147, 34);
		frame.getContentPane().add(lbltrad);
		lbltrad.setVisible(false);
		
		
		//__________  ***  BOTTONE "PROSSIMA PAROLA DA TRADURRE"  ***  ___________//
		
		
		btnProssimaParola = new JButton("Prossima parola");
		btnProssimaParola.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnProssimaParola.setVisible(false);
		btnProssimaParola.setForeground(new Color(255, 255, 0));
		btnProssimaParola.setBorder(new LineBorder(new Color(0, 0, 0)));
		btnProssimaParola.setOpaque(true);
		btnProssimaParola.setBackground(new Color(153, 0, 255));
		
		//click sul bottone
		btnProssimaParola.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//conto le parole
				i++;
				numero= String.valueOf(i).toString();
				lblnum.setText(numero);
				if(i<=5) {
					btnProssimaParola.setVisible(false);	
					btnInvio.setVisible(true);
					lblno.setVisible(false);
					lblok.setVisible(false);
					textField.setText(null);
					lblitaly.setText(null);
				
					//invio richiesta di prossima parola al server
					ProssimaParola();
					
				
				}
				
				
			}
		});
		
		btnProssimaParola.setBounds(264, 225, 134, 27);
		frame.getContentPane().add(btnProssimaParola);
		
		//cambio numero della parola ogni volta che clicko
		lblnum = new JLabel(numero);
		lblnum.setForeground(new Color(255, 255, 0));
		lblnum.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 25));
		lblnum.setBounds(317, 78, 48, 34);
		frame.getContentPane().add(lblnum);
		lblnum.setVisible(false);
		//BANDIERA ITA
		lblen = new JLabel("");
		lblen.setBounds(398, 175, 61, 43);
		Image en = new ImageIcon(this.getClass().getResource("/en_flag.png")).getImage();
		lblen.setIcon(new ImageIcon(en));
		lblen.setVisible(false);
		frame.getContentPane().add(lblen);
		//BANDIERA EN
		lblit = new JLabel("");
		lblit.setBounds(404, 110, 48, 47);
		Image ita = new ImageIcon(this.getClass().getResource("/it_flag.png")).getImage();
		lblit.setIcon(new ImageIcon(ita));
		lblit.setVisible(false);
		frame.getContentPane().add(lblit);		
	
		//parola errata
		lblno = new JLabel("");
		lblno.setBounds(372, 185, 61, 16);
		Image no = new ImageIcon(this.getClass().getResource("/no.png")).getImage();
		lblno.setIcon(new ImageIcon(no));
		frame.getContentPane().add(lblno);
		lblno.setVisible(false);		
				
		//PAROLA DA TRADURRE
		lblitaly = new JLabel("");
		lblitaly.setBackground(new Color(255, 255, 255));
		lblitaly.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblitaly.setHorizontalAlignment(SwingConstants.CENTER);
		lblitaly.setForeground(new Color(0, 0, 0));
		lblitaly.setOpaque(true);
		lblitaly.setBounds(235, 110, 179, 43);
		frame.getContentPane().add(lblitaly);
		lblitaly.setVisible(false);
		
		//parola corretta
		lblok = new JLabel("");
		lblok.setBounds(363, 156, 85, 78);
		Image ok1 = new ImageIcon(this.getClass().getResource("/oks.png")).getImage();
		lblok.setIcon(new ImageIcon(ok1));
		frame.getContentPane().add(lblok);
		lblok.setVisible(false);
		
		//PAROLA DA INSERIRE
		textField = new JTextField();
		textField.setBounds(235, 173, 179, 43);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		textField.setVisible(false);
		
		//PAROLA NUMERO
		label = new JLabel("PAROLA N. ");
		label.setForeground(new Color(0, 0, 153));
		label.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
		label.setBounds(235, 81, 134, 34);
		frame.getContentPane().add(label);
		label.setVisible(false);
		
		
		//____________   **** bottone VERIFICA TRADUZIONE  **** _____________//
		
		btnInvio = new JButton("VERIFICA TRADUZIONE");
		btnInvio.setOpaque(true);
		btnInvio.setBackground(new Color(204, 102, 255));
		btnInvio.setBorder(new LineBorder(new Color(0, 0, 0)));
		btnInvio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnInvio.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				btnInvio.setVisible(false);
				btnProssimaParola.setVisible(true);
				Traduzione();
				if(i==5) {btnFinePartita.setVisible(true); btnProssimaParola.setVisible(false);btnInvio.setVisible(false);}
			}
		});
		btnInvio.setBounds(245, 225, 169, 27);
		frame.getContentPane().add(btnInvio);
		btnInvio.setVisible(false);
		
		
		//_______________    ***  bottone   FINE PARTITA (VISIBILE DOPO LA QUINTA PAROLA)       ***     __________________//
		
		
		btnFinePartita = new JButton("FINE PARTITA");
		btnFinePartita.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnFinePartita.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//LABEL WAIT ATTIVA
				timer.stop();
				stoppato=true;
				timer=null;
				textField.setVisible(false);
				lblitaly.setVisible(false);
				label.setVisible(false);
				lbltrad.setVisible(false);
				lblnum.setVisible(false);
				lblno.setVisible(false);
				lblok.setVisible(false);
				btnInvio.setVisible(false);
				btnInizia.setVisible(false);
				lblit.setVisible(false);
				lblen.setVisible(false);
				btnFinePartita.setVisible(false);
				lbltime.setVisible(false);
				username.setVisible(false);
				userfriend.setVisible(false);
				separator.setVisible(false);
				lblabbandona.setVisible(false);
				lblVs.setVisible(false);
				lblinfo.setVisible(false);
				lbli.setVisible(false);
				FinePartita();
			}
		});
		btnFinePartita.setBorder(new LineBorder(new Color(0, 0, 0)));
		btnFinePartita.setBackground(new Color(0, 0, 204));
		btnFinePartita.setOpaque(true);
		btnFinePartita.setForeground(new Color(255, 255, 0));
		btnFinePartita.setBounds(274, 224, 117, 29);
		frame.getContentPane().add(btnFinePartita);
		btnFinePartita.setVisible(false);
		
		//separatori
		
		separator = new JSeparator();
		separator.setForeground(Color.BLUE);
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(185, 58, 18, 220);
		frame.getContentPane().add(separator);
		
		separator_1 = new JSeparator();
		separator_1.setForeground(Color.BLUE);
		separator_1.setBounds(3, 53, 447, 10);
		frame.getContentPane().add(separator_1);
		
		
		
		
	}		

		//_________   *** INIZIA SFIDA  ***  __________//
		
		public void IniziaSfida() {
			
			
			String reque1= "INIZIASFIDA "+nickUtente+" ";
			try {
				MsgThSfida(reque1);
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(parentframe, "Errore di comunicazione con il server. Riprova","Comunicazione rifiutata",JOptionPane.ERROR_MESSAGE);
				
			}
		}
		
		
		
		//_________  *** ABBANDONA ***  ________//
		
		

		public void Abbandona () {
			
			
			String requess ="ABBANDONA "+nickUtente+" ";
			try {
				MsgThSfida(requess);
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(parentframe, "Errore di comunicazione con il server. Riprova","Comunicazione rifiutata",JOptionPane.ERROR_MESSAGE);
				
			}
		}
		
		//________  ***  PROSSIMA PAROLA *** ___________//
		
		public void ProssimaParola () {
			 textField.setForeground(Color.BLACK);
			String r ="PROSSIMAPAROLA "+nickUtente+" ";
			try {
				MsgThSfida(r);
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(parentframe, "Errore di comunicazione con il server. Riprova","Comunicazione rifiutata",JOptionPane.ERROR_MESSAGE);
				
			}
		}
		
		//_______  *** invia TRADUZIONE *** ___________//
		
		public void Traduzione() {
			String req="TRADUZIONE "+nickUtente+" "+textField.getText()+ " "+ parola_da_tradurre+" ";
			try {
				MsgThSfida(req);
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(parentframe, "Errore di comunicazione con il server. Riprova","Comunicazione rifiutata",JOptionPane.ERROR_MESSAGE);
				
			}
		}
		
		//___________  *** FINE PARTITA  ***  __________//
		
		public void FinePartita() {
			String requ="FINEPARTITA "+nickUtente+" ";
			try {
				MsgThSfida(requ);
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(parentframe, "Errore di comunicazione con il server. Riprova","Comunicazione rifiutata",JOptionPane.ERROR_MESSAGE);
				
			}
		}
		
		//____________  ***  FINE SFIDA  ***  __________//
		
		
		public void FineSfida() {
			
			//ClientUDP.Riattiva();
			
			String reque="FINESFIDA "+nickUtente+" ";
			try {
				MsgThSfida(reque);
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(parentframe, "Errore di comunicazione con il server. Riprova","Comunicazione rifiutata",JOptionPane.ERROR_MESSAGE);
				
			}
		}
		

		//_________________   ***   GESTIONE TIMER  ***   ________________//
		
		
		private void SetTimer(int secs) {
			
			seconds= secs;
	        
	        //IMPLEMENTAZIONE DA PASSARE AL TIMER
	        ActionListener Timeout = new ActionListener() 
	        {
	        	//contiene il task da eseguire
	            @Override
	            public void actionPerformed(ActionEvent e)  {
	            	
	            	//temposcaduto
	               if(seconds == 0) {
	                   timer.stop();
	                   Toolkit.getDefaultToolkit().beep();
	                   String stop="TIMEOUT "+nickUtente;
	                   try {
						MsgThSfida(stop);
					} catch (InterruptedException e1) {
						JOptionPane.showMessageDialog(parentframe, "Errore di comunicazione con il server. Riprova","Comunicazione rifiutata",JOptionPane.ERROR_MESSAGE);
						
					}
	                  lbltimer.setVisible(false);
	                  
	                 
	               }
	               //decremento fino a 0
	               else
	               {
	                   seconds--;
	                   
	                   lbltimer.setBackground(new Color(238-seconds,238-seconds,238-seconds));
	                   lbltimer.setText("<html><center> Tempo rimasto: <font color='red'>"+seconds+"</font> </center><html>");
	                 
	               }
	            }
	        };
	        
	        //creo il timer, ogni secondo conta
	        timer = new Timer(1000,Timeout);
	       
	        try  {
	            sleep(100);
	        } 
	        catch (InterruptedException ex)  {
	            timer.stop();
	            timer=null;
	        }
	        
	        timer.start();   
	    }
		
		
		//___________  ***  comunicazione con server   ***    __________//
		
		
		public void MsgThSfida(String request) throws InterruptedException {
			
			
			//inviola richiesta al server
			try {
				msgTCP.send_msg(request);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(parentframe,"Ci scusiamo, ma si è verificato un problema con il server. Aspetta qualche istante "+ username +", rifai login e riprova!! ","Problema", JOptionPane.INFORMATION_MESSAGE);
				//GUI LOGIN
				frame.setVisible(false);
				frame.dispose();
				parentframe.setVisible(true);
				parentframe.isAlwaysOnTop();
			}
			//ricevo risposta dal server
			String esito=null;
			
			
			esito = msgTCP.receive_msg();
		
			System.out.println("Esito sfida : "+esito );
			
			
			
			
			String[] tokens=null;
			//server invia la prima parola
			if(esito.contains("INIZIATA:")){
					lbltimer.setVisible(true);
					btnInizia.setVisible(false);
					
					//iniza il timer della partita di 50 sec
					SetTimer(50);
					//tokenizzo
					tokens = esito.split(":"); 
				    esito = tokens[0];
				    
				    //PRIMA PAROLA
				    parola_da_tradurre = tokens[1];
				  
				   //etichetta per contare le parole tradotte
				   lblnum.setText(numero);
				   lblitaly.setText(parola_da_tradurre);
			}
			//il server invia la parola da tradurre
			else if(esito.contains("PAROLA:")) {
				//dalla seconda alla quinta parola
				//tokenizzo
				tokens = esito.split(":"); 
			    esito = tokens[0];
			    parola_da_tradurre=tokens[1];
			    lblitaly.setText(parola_da_tradurre);   
			   
			}	
			//se risposta esatta
			else if(esito.contains("CORRETTA")){
				//etichetta ok
				textField.setForeground(Color.GREEN);
				textField.setSelectedTextColor(Color.GREEN);
				lblok.setVisible(true);
				btnProssimaParola.setVisible(true);
				
			}
			//se risposta sbagliata
			else if(esito.contains("ERRATA:")) {
				 
				//etichetta no
				lblno.setVisible(true);
				tokens = esito.split(":"); 
			    esito = tokens[0];
			    //PRIMA PAROLA
			    String corretta = tokens[1];
			    textField.setText(corretta);
			    textField.setForeground(Color.RED);
				textField.setSelectedTextColor(Color.RED);
			    btnProssimaParola.setVisible(true);
			    
			}
			else if(esito.contains("WAIT")){
				
				//e aspetto che l'alro finisca prima di avere i risultati
				request="OKWAIT1 "+nickUtente;
				MsgThSfida(request);
				
			}
			
			//HAI ABBANDONATO
			else if(esito.contains("abbandonato:")) {
			
				textField.setVisible(false);
				lblitaly.setVisible(false);
				label.setVisible(false);
				lbltrad.setVisible(false);
				lblnum.setVisible(false);
				lblno.setVisible(false);
				lblok.setVisible(false);
				btnInvio.setVisible(false);
				btnInizia.setVisible(false);
				lblit.setVisible(false);
				lblen.setVisible(false);
				btnFinePartita.setVisible(false);
				lbltime.setVisible(false);
				username.setVisible(false);
				userfriend.setVisible(false);
				separator.setVisible(false);
				lblabbandona.setVisible(false);
				lblVs.setVisible(false);
				lblinfo.setVisible(false);
				lbli.setVisible(false);
				btnProssimaParola.setVisible(false);
				
				tokens = esito.split(":");
				lblesito.setText("<html><center>Hai abbandonato la sfida, quindi hai perso <font color='green'>"+tokens[1]+"</font>.<br>Ricordati che sbagliando si impara! <br> Gioca ancora per migliorare!</font><br>IL TUO PUNTEGGIO PARTITA:<font color='red'><font size=15> 0</font></center></html>");
				
				btnMostraRisultati.setVisible(true);
				btnTornaAllaHome.setVisible(true);
				
				System.out.println("ABBANDONATO:  GUI ");
				lblabbandona.setVisible(true);
				
				btnabbandona.setVisible(false);
			
				
				
			}
			//HAI VINTO
			else if(esito.contains("vincitore:"+nickUtente)) {
				
				
				
				
				textField.setVisible(false);
				lblitaly.setVisible(false);
				label.setVisible(false);
				lbltrad.setVisible(false);
				lblnum.setVisible(false);
				lblno.setVisible(false);
				lblok.setVisible(false);
				btnInvio.setVisible(false);
				btnInizia.setVisible(false);
				lblit.setVisible(false);
				lblen.setVisible(false);
				btnFinePartita.setVisible(false);
				lbltime.setVisible(false);
				username.setVisible(false);
				userfriend.setVisible(false);
				separator.setVisible(false);
				lblabbandona.setVisible(false);
				lblVs.setVisible(false);
				btnProssimaParola.setVisible(false);
				lblinfo.setVisible(false);
				lbli.setVisible(false);
				
				
					
				tokens = esito.split(":");
				
				//l'altro non ha abbandonato
				if(esito.contains("perdente")) {
					lblWinner.setText("<html><center>Congratulazioni!!! <br> Hai vinto la sfida!!!<br><br><font color='green'>Sei un maestro dell'inglese!<br> Continua così e diventerai<br> un vero fenomeno!</font></center><html>");
					
					//preparo i risultati
					lblesito.setText("<html><center></font><br>IL TUO PUNTEGGIO PARTITA:<font color='red'>"+tokens[3]+"</font><br>(+5 bonus)<br>TRADUZIONI ESATTE: <font color='red'>"+tokens[5]+"</font><br>TRADUZIONI SBAGLIATE: <font color='red'>"+tokens[7]+"</font><br><br>Il tuo avversario <font color='green'>"+tokens[9]+"</font> ha totalizzato <font color='blue'>"+tokens[11]+"</font> punti.</center></html>");
					//mostro il tasto per conoscere i risultati
					btnMostraRisultati.setVisible(true);
					//e il tasto per tornare alla home
					btnTornaAllaHome.setVisible(true);
					btnabbandona.setVisible(false);
					
					//hai vinto
					lblgoal.setVisible(true);
					lblwin.setVisible(true);
					lblWinner.setVisible(true);
					btnTornaAllaHome.setVisible(true);
					
				
				}
				//l'altro ha abbandonato
				else {
					//ma io non ho abbandonato subito dopo
					if(!stoppato && timer!=null) {
						timer.stop();
						timer=null;
					}
					//anch'io ho abbandonato, ma dopo, quindi vinco ugualmente
					else {
						lblabbandona.setVisible(false);
					}
					lblWinner.setText("<html><center>Hai vinto la sfida, perchè <br>il tuo avversario ha abbandonato!<br><br><font color='green'> Gioca ancora e divertiti con l'inglese!<br></font></center><html>");
					
					lblabbandona.setVisible(false);
					//preparo i risultati
					lblesito.setText("<html><center></font><br>IL TUO PUNTEGGIO PARTITA:<br><font color='red'><font size=20>"+tokens[3]+"</font> <br> Il tuo avversario ha guadagnato 0 punti.</font></center></html>");
					//mostro il tasto per conoscere i risultati
					btnMostraRisultati.setVisible(true);
					//e il tasto per tornare alla home
					btnTornaAllaHome.setVisible(true);
					btnabbandona.setVisible(false);
					
					
					//hai vinto
					lblgoal.setVisible(true);
					lblwin.setVisible(true);
					lblWinner.setVisible(true);
					btnTornaAllaHome.setVisible(true);
					
				}
			}
			
			//HAI PERSO
			else if(esito.contains("vincitore:"+nickAmico)) {

				textField.setVisible(false);
				lblitaly.setVisible(false);
				label.setVisible(false);
				lbltrad.setVisible(false);
				lblnum.setVisible(false);
				lblno.setVisible(false);
				lblok.setVisible(false);
				btnInvio.setVisible(false);
				btnInizia.setVisible(false);
				lblit.setVisible(false);
				lblen.setVisible(false);
				btnFinePartita.setVisible(false);
				lbltime.setVisible(false);
				username.setVisible(false);
				userfriend.setVisible(false);
				separator.setVisible(false);
				lblabbandona.setVisible(false);
				lblVs.setVisible(false);
				btnProssimaParola.setVisible(false);
				lblinfo.setVisible(false);
				lbli.setVisible(false);
				
				//hai perso
				lblperd.setVisible(true);
				lblWinner.setText("<html><center>Mi dispiace, hai perso!<br> Ma non preoccuparti,<br> perdere una battaglia non significa perdere la guerra!<br><br><font color='green'>Gioca ancora e migliora <br>il tuo inglese!</font></center></html>");
				lblWinner.setVisible(true);
				btnTornaAllaHome.setVisible(true);
				
				tokens = esito.split(":");
				//preparo i risultati
				lblesito.setText("<html><center> IL TUO PUNTEGGIO PARTITA: <font color='green'>"+tokens[11]+"<br></font><br><br>TRADUZIONI ESATTE: <font color='red'>"+tokens[13]+"</font><br>TRADUZIONI SBAGLIATE: <font color='red'>"+tokens[15]+"</font><br><br>Il tuo avversario <font color='green'>"+tokens[1]+"</font> ha totalizzato <font color='blue'>"+tokens[3]+"</font> punti.<center></html>");
				//mostro il tasto per conoscere i risultati
				btnMostraRisultati.setVisible(true);
				//e il tasto per tornare alla home
				btnTornaAllaHome.setVisible(true);
				btnabbandona.setVisible(false);
				lblinfo.setVisible(false);
				lbli.setVisible(false);
				
				
			}
			//PAREGGIO
			else if(esito.contains("pareggio:")) {
				

				textField.setVisible(false);
				lblitaly.setVisible(false);
				label.setVisible(false);
				lbltrad.setVisible(false);
				lblnum.setVisible(false);
				lblno.setVisible(false);
				lblok.setVisible(false);
				btnInvio.setVisible(false);
				btnInizia.setVisible(false);
				lblit.setVisible(false);
				lblen.setVisible(false);
				btnFinePartita.setVisible(false);
				lbltime.setVisible(false);
				username.setVisible(false);
				userfriend.setVisible(false);
				separator.setVisible(false);
				lblabbandona.setVisible(false);
				lblVs.setVisible(false);
				btnProssimaParola.setVisible(false);
				lblinfo.setVisible(false);
				lbli.setVisible(false);
				
				
				tokens = esito.split(":");
				lblWinner.setText("<html><center>Pareggio!!! <br> Una sfida all'ultimo sangue! Complimenti!<br><br>:<font color='green'>Scopri i risultati della sfida o <br>torna alla home per sfidarlo di nuovo!<br> </font></center><html>");
				
				lblesito.setText("<html><center> PAREGGIO CON </font> "+tokens[1]+" punti (+5 bonus).<br><br>TRADUZIONI ESATTE:<font color='red'>"+tokens[3]+"</font><br>TRADUZIONI SBAGLIATE:<font color='red'>"+tokens[5]+"</font><center></html>");
				//mostro il tasto per conoscere i risultati
				btnMostraRisultati.setVisible(true);
				//e il tasto per tornare alla home
				btnTornaAllaHome.setVisible(true);
				btnabbandona.setVisible(false);
				
				//hai vinto
				lblgoal.setVisible(true);
				lblwin.setVisible(true);
				lblWinner.setVisible(true);
				
				
			}
		
			
			else if(esito.contains("OKFINESFIDA")) {
				
				lblinfo.setVisible(false);
				lbli.setVisible(false);
				
				//riattivo ascolti di richieste di sfida che erano stati interrotti 
				ClientUDP.Riattiva();
				
				//chiudi schermata sfida
				parentframe.setVisible(true);	
				frame.dispose();
			}
			
			else if(esito.contains("DISCONNECT")) {
				
				JOptionPane.showMessageDialog(parentframe,"Il tuo avversario si è diconnesso in modo anomalo. Prova a sfidarlo di nuovo","Avversario disconnesso", JOptionPane.INFORMATION_MESSAGE); frame.dispose(); parentframe.dispose(); ppframe.setVisible(true);
				if (timer!=null) {
					timer.stop();
					timer=null;
				}
				String disc="OKDISCONNECT "+nickUtente+" "+nickAmico;
				
				MsgThSfida(disc);
				
				
				
			}
			
			else if(esito.contains("RICOMINCIA")) {
				JOptionPane.showMessageDialog(parentframe,"Ricomincia a giocare","Ricomincia", JOptionPane.INFORMATION_MESSAGE); 
				frame.setVisible(false);
				frame.dispose();
				
				ppframe.setVisible(false);
			}
			else {
				JOptionPane.showMessageDialog(ppframe,"Ci scusiamo, ma si è verificato un problema. Aspetta qualche istante e riprova! ","Problema", JOptionPane.INFORMATION_MESSAGE); 
				//GUI LOGIN

				ppframe.setVisible(false);
				frame.setVisible(false);
				frame.dispose();
				parentframe.setVisible(true);
				parentframe.isAlwaysOnTop();			
			}
		
			
			
			
		}
} 
