import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JOptionPane;


import java.awt.Font;
import java.awt.Image;

import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.io.IOException;


import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.awt.Dimension;

import java.awt.Rectangle;



import javax.swing.border.LineBorder;

import java.awt.Cursor;
import javax.swing.JTextField;
import javax.swing.JSeparator;

import javax.swing.JPasswordField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */

public class MainGUI_WQ extends JFrame{

	//__________  *** COMPONENTI DELL'INTERFACCIA  ***  ___________//
	
	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private JTextField textUsername;
	private JPasswordField textPassword;
	
	//etichette per immagini login
	private JLabel label1;
	private JLabel label2;
	private JLabel label4;
	private JLabel label5;
	
	//immagini 
	private Image img1;
	private Image img2;
	private Image img4;
	private Image img5;
	
	String username;
	String password;
	
	//____________   ***   PORTA DI DEFAULT   ***  _____________//
	
	
    static int default_port_TCP = 1888;
    
   
	
	//_______________   *** DICHIARAZIONI VARIABILI  ***  _____________//
    
    //socketChannel statico visibile anche nei metodi fuori dal main
    SocketChannel client;
   
    String request;
    ByteBuffer bbuf = ByteBuffer.allocate(1024);
    ByteBuffer length = ByteBuffer.allocate(256);
    
    
	//____________    *** LANCIO L'APPLICAZIONE  ***   ______________//
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//creo window 
					MainGUI_WQ window = new MainGUI_WQ();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	} 
	/**
	 * Create the application.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public MainGUI_WQ() throws UnknownHostException, IOException {
		
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	private void initialize() throws UnknownHostException, IOException {
		
		
		frame = new JFrame();
		frame.getContentPane().setForeground(new Color(0, 0, 0));
		frame.getContentPane().setBackground(new Color(255, 204, 0));
		frame.getContentPane().setLayout(null);
		
		//---------   *** titolo Word Quizzle ***   --------------------//
		
		
		JLabel lblWordquizzle = new JLabel("Word  Quizzle");
		lblWordquizzle.setBounds(85, -20, 269, 92);
		lblWordquizzle.setForeground(new Color(204, 51, 51));
		lblWordquizzle.setHorizontalAlignment(SwingConstants.CENTER);
		lblWordquizzle.setFont(new Font("Winter Festival", Font.BOLD | Font.ITALIC, 45));
		frame.getContentPane().add(lblWordquizzle);
		
		
		//----------    ***    Inserisci nomeutente   ***     -------------//
	
		
		JLabel lblNickname = new JLabel("Nickname");
		lblNickname.setFont(new Font("Georgia", Font.ITALIC, 14));
		lblNickname.setBounds(142, 131, 82, 30);
		frame.getContentPane().add(lblNickname);
		
		textUsername = new JTextField();
		textUsername.setBounds(142, 155, 178, 35);
		frame.getContentPane().add(textUsername);
		textUsername.setColumns(10);
				
			
		//----------    ***    Inserisci password   ***       -----------//
	
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Georgia", Font.ITALIC, 14));
		lblPassword.setBounds(142, 184, 74, 25);
		frame.getContentPane().add(lblPassword);
		textPassword = new JPasswordField();
		textPassword.setBounds(142, 203, 178, 35);
		frame.getContentPane().add(textPassword);
		
		
		//-----------    *** bottone "CREA ACCOUNT"  ***     ---------------//
		
		
		JButton btnAccount = new JButton("Crea un nuovo account");
		btnAccount.setVerticalTextPosition(SwingConstants.BOTTOM);
		btnAccount.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		
		btnAccount.setHorizontalTextPosition(SwingConstants.CENTER);
		
		//eventi 
		btnAccount.addMouseListener(new MouseAdapter() {
			
			//evento mouse senza cliccare:cambia colore
			@Override
			public void mouseEntered(MouseEvent e) {
				btnAccount.setBackground(Color.LIGHT_GRAY);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				btnAccount.setBackground(Color.WHITE);
			}
			
			//evento mouse cliccando: cambia interfaccia "registrati"
			@Override
			public void mouseClicked(MouseEvent e) {
				
				//azzero i campi 
				textPassword.setText(null);
				textUsername.setText(null);
				
				//lancio nuova window per registrazione
				Reg_GUI reg = new Reg_GUI();
				
				
			}	
		});
		
		//proprietà del bottone
		
		btnAccount.setForeground(new Color(0, 0, 0));
		btnAccount.setBorder(new LineBorder(new Color(0, 0, 0)));
		btnAccount.setOpaque(true);
		btnAccount.setFont(new Font("Helvetica", Font.BOLD, 10));
		btnAccount.setBounds(175, 317, 123, 25);
		btnAccount.setBackground(new Color(255, 255, 255));
		btnAccount.setMinimumSize(new Dimension(10, 10));
		frame.getContentPane().add(btnAccount);
		
		
		//-----------------    ***      bottone "LOGIN"       ***     --------------------//
		
		
		JButton btnLogin = new JButton("LOGIN");
		
		//EVENTI DEL MOUSE
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				btnLogin.setBackground(Color.GREEN);
				btnLogin.setForeground(Color.BLACK);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				btnLogin.setBackground(Color.BLACK);
				btnLogin.setForeground(Color.WHITE);
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				username = textUsername.getText().trim();
				password = textPassword.getText().trim();
				
				if(textUsername.getText().isEmpty() || textPassword.getText().isEmpty()){
					JOptionPane.showMessageDialog(frame, "Campi nulli. Inserisci il tuo username e la tua password.","Login fallita",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				//inizia il gioco
				try {
					Game_GUI match = new Game_GUI(username,frame);
					match.Login(username, password);
				} catch (IOException e1) {e1.printStackTrace();}	
			}
		});
		
		//proprietà del bottone
		
		btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLogin.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		btnLogin.setOpaque(true);
		btnLogin.setBounds(new Rectangle(1, 1, 1, 1));
		btnLogin.setForeground(new Color(255, 255, 255));
		btnLogin.setFont(new Font("Zapf Dingbats", Font.BOLD, 15));
		btnLogin.setBackground(new Color(0, 0, 0));
		btnLogin.setBounds(150, 240, 74, 30);
		frame.getContentPane().add(btnLogin);
		
		
		//-------------     ***    Bottene "Reset"   ***    ----------------//
		
		
		JButton btnReset = new JButton("RESET");
		btnReset.addMouseListener(new MouseAdapter() {
			
			//EVENTI DEL MOUSE: cambia colore senza cliccare
			@Override
			public void mouseEntered(MouseEvent e) {
				btnReset.setBackground(Color.RED);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				btnReset.setBackground(Color.BLACK);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				//resetto i campi, in caso di errore di digitazione
				textPassword.setText(null);
				textUsername.setText(null);
			}
		});
		
				
		//proprietà del bottone
		
		btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnReset.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		btnReset.setOpaque(true);
		btnReset.setBounds(new Rectangle(1, 1, 1, 1));
		btnReset.setForeground(new Color(255, 255, 255));
		btnReset.setFont(new Font("Zapf Dingbats", Font.BOLD, 15));
		btnReset.setBackground(new Color(0, 0, 0));
		btnReset.setBounds(236, 240, 74, 30);
		frame.getContentPane().add(btnReset);
		
		
		//----------------   ***    DESIGN INTERFACE   ***   -------------- //
		
		//linea separatrice
		JSeparator separator = new JSeparator();
		separator.setForeground(Color.BLACK);
		separator.setBounds(0, 354, 465, 12);
		frame.getContentPane().add(separator);
		
		//immagine torre
		label1 = new JLabel("");
		img1 = new ImageIcon(this.getClass().getResource("/torre.png")).getImage();
		label1.setIcon(new ImageIcon(img1));
		label1.setBounds(-37, 145, 177, 237);
		frame.getContentPane().add(label1);
		
		//immagine londoneye
		label2 = new JLabel("");
		img2 = new ImageIcon(this.getClass().getResource("/bigben.png")).getImage();
		label2.setIcon(new ImageIcon(img2));
		label2.setBounds(310, 120, 155, 238);
		frame.getContentPane().add(label2);
		
		//introduzione login
		JLabel lblTraduciPiParole = new JLabel("Traduci più parole dei tuoi amici e vinci!");
		lblTraduciPiParole.setForeground(new Color(0, 51, 204));
		lblTraduciPiParole.setBackground(new Color(255, 51, 0));
		lblTraduciPiParole.setFont(new Font("Kannada MN", Font.ITALIC, 13));
		lblTraduciPiParole.setBounds(95, 84, 288, 16);
		frame.getContentPane().add(lblTraduciPiParole);
		JLabel lblImparareLingleseNon =new JLabel("Imparare l'inglese non è mai stato così facile! Cosa aspetti? ");
		lblImparareLingleseNon.setBackground(new Color(0, 153, 51));
		lblImparareLingleseNon.setForeground(new Color(0, 0, 0));
		lblImparareLingleseNon.setFont(new Font("Kannada MN", Font.ITALIC, 12));
		lblImparareLingleseNon.setBounds(52, 68, 407, 16);
		frame.getContentPane().add(lblImparareLingleseNon);
		
		//england flag
		label4 = new JLabel("");
		img4 = new ImageIcon(this.getClass().getResource("/it_flag.png")).getImage();
		label4.setIcon(new ImageIcon(img4));
		label4.setBounds(75, -9, 74, 87);
		frame.getContentPane().add(label4);
		
		//italian flag
		label5 = new JLabel("");
		img5 = new ImageIcon(this.getClass().getResource("/en_flag.png")).getImage();
		label5.setIcon(new ImageIcon(img5));
		label5.setBounds(337, -20, 81, 102);
		frame.getContentPane().add(label5);
		
		//già registrato?
		JLabel lblSeiGiRegistrato = new JLabel("Sei già registrato?");
		lblSeiGiRegistrato.setForeground(Color.RED);
		lblSeiGiRegistrato.setFont(new Font("Hiragino Sans", Font.BOLD, 13));
		lblSeiGiRegistrato.setBounds(171, 120, 155, 16);
		frame.getContentPane().add(lblSeiGiRegistrato);
		

		//non sei ancora registrato?
		JLabel lblNonSeiAncora = new JLabel("Non sei ancora registrato?");
		lblNonSeiAncora.setForeground(Color.RED);
		lblNonSeiAncora.setFont(new Font("Hiragino Sans", Font.BOLD, 12));
		lblNonSeiAncora.setBounds(150, 302, 176, 16);
		frame.getContentPane().add(lblNonSeiAncora);
		
		
		frame.setAlwaysOnTop(true);
		frame.setBounds(100, 100, 465, 426);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	
	
		
	}
}

	

