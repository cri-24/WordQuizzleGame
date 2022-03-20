
import java.awt.Font;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Panel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.border.LineBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.SwingConstants;
import java.awt.Cursor;


/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */

public class Reg_GUI extends JFrame{

	//___________   *** DICHIARAZIONE VARIABILI E COMPONENTI  ***   __________//
	
	private static final long serialVersionUID = 1L;
	private JFrame frame;
	
	//CREDENZIALI 
	private JTextField textUsername;
	private JPasswordField textPassword;
	private String Nickname;
	private String Password;
	private JLabel lblok;
	private JLabel lblno;
	private JLabel lblno1;
	private Image imgok;
	private Image img1;
	private Image img2;
	private Image imgno;
	private Image imgno1;
	private Image imgreg;
	private JLabel lblreg;
	private JLabel label1;
	private JLabel label2;
	private JButton btnRegistrati;
	private JLabel lblPassword;
	private JLabel lblinfo;
	
	//PORT
	static int default_port_RMI = 30000;
	//nameservice
	String nameServiceRMI = "REGISTER-SERVER";
	private JLabel lbli;
	
	
	//__________________  ***  COSTRUTTORE   ***  __________________//
	
	
	public Reg_GUI() {
		initialize();
	}

	//_________________   ***   INIZIALIZZA  ***  ___________________//
	
	
	private void initialize() {
		
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(255, 153, 51));
		frame.getContentPane().setLayout(null);
		
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		
		Panel panel = new Panel();
		panel.setBounds(0, 0, 0, 0);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JButton btnNewButton = new JButton("Registrati");
		btnNewButton.setBounds(163, 180, 117, 29);
		panel.add(btnNewButton);
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
				
		//------------------    ***   Design Interface   ***   --------------------------
		
		//immagine ok
		lblok = new JLabel("");
		lblok.setFocusCycleRoot(true);
		lblok.setVisible(false);
		imgok = new ImageIcon(this.getClass().getResource("/ok.png")).getImage();
		lblok.setIcon(new ImageIcon(imgok));
		lblok.setHorizontalAlignment(SwingConstants.CENTER);
		lblok.setBounds(67, 6, 294, 273);
		frame.getContentPane().add(lblok);
			
			
		//error nome
		lblno = new JLabel("");
		lblno.setVisible(false);
		imgno = new ImageIcon(this.getClass().getResource("/no.png")).getImage();
		lblno.setIcon(new ImageIcon(imgno));
		lblno.setBounds(329, 95, 28, 35);
		frame.getContentPane().add(lblno);
		//error password
		lblno1 = new JLabel("");
		lblno1.setVisible(false);
		imgno1 = new ImageIcon(this.getClass().getResource("/no.png")).getImage();
		lblno1.setIcon(new ImageIcon(imgno1));
		lblno1.setBounds(329, 156, 28, 32);
		frame.getContentPane().add(lblno1);
		
		//immagine nome utente
		label1 = new JLabel("");
		img1 = new ImageIcon(this.getClass().getResource("/profile.png")).getImage();
		label1.setIcon(new ImageIcon(img1));
		label1.setBounds(92, 83, 46, 47);
		frame.getContentPane().add(label1);
		
		//immagine password
		label2 = new JLabel("");
		img2 = new ImageIcon(this.getClass().getResource("/pw.png")).getImage();
		label2.setIcon(new ImageIcon(img2));
		label2.setBounds(92, 143, 46, 55);
		frame.getContentPane().add(label2);			
		
		//già registrato
		lblreg = new JLabel("");
		lblreg.setVisible(false);
		imgreg = new ImageIcon(this.getClass().getResource("/reg.png")).getImage();
		lblreg.setIcon(new ImageIcon(imgreg));
		lblreg.setBounds(369, 168, 74, 60);
		frame.getContentPane().add(lblreg);
		
		//------------    ***      inserisci nomeutente    ***    -------------------
		
		JLabel lblNickname = new JLabel("Nickname");
		lblNickname.setFont(new Font("Georgia", Font.ITALIC, 14));
		lblNickname.setBounds(139, 66, 82, 30);
		frame.getContentPane().add(lblNickname);
		textUsername = new JTextField();
		textUsername.setBackground(new Color(255, 204, 51));
		textUsername.setBounds(139, 95, 178, 35);
		frame.getContentPane().add(textUsername);
		textUsername.setColumns(10);
		textUsername.setText(null); 
		
		//------------    ***     inserisci password    ***     -------------------
	
		
		lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Georgia", Font.ITALIC, 14));
		lblPassword.setBounds(139, 134, 74, 25);
		frame.getContentPane().add(lblPassword);
		textPassword = new JPasswordField();
		textPassword.setBackground(new Color(255, 204, 51));
		textPassword.setBounds(139, 156, 178, 35);
		frame.getContentPane().add(textPassword);
		textPassword.setText(null);
		
		
		//------------    ***     info    ***     -------------------
		
		
		lblinfo = new JLabel("<html><center>Inserire solo caratteri alfanumerici.<br>Non sono ammessi nè caratteri speciali nè spazi</center></html>");
		lblinfo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblinfo.setHorizontalTextPosition(SwingConstants.CENTER);
		lblinfo.setHorizontalAlignment(SwingConstants.CENTER);
		lblinfo.setFont(new Font("Merriweather", Font.PLAIN, 6));
		lblinfo.setVisible(false);
		
		lblinfo.setBounds(369, 39, 74, 45);
		frame.getContentPane().add(lblinfo);
		
		lbli = new JLabel("i");
		lbli.setBackground(new Color(0, 0, 0));
		lbli.setOpaque(true);
		lbli.setForeground(new Color(255, 255, 255));
		lbli.setFont(new Font("ITF Devanagari", Font.BOLD, 19));
		lbli.setHorizontalAlignment(SwingConstants.CENTER);
		lbli.setHorizontalTextPosition(SwingConstants.CENTER);
		lbli.setBounds(400, 20, 17, 16);
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
	
		
		
		//-------------   ***    introduzione alla registrazione   ***   --------------------
		
		
		JLabel lblELaPrima = new JLabel("E' la prima volta che accedi a Word Quizzle? ");
		lblELaPrima.setBounds(78, 6, 318, 16);
		frame.getContentPane().add(lblELaPrima);
		
		JLabel lblScegliLeTue = new JLabel("Scegli le tue credenziali di accesso e registrati.");
		lblScegliLeTue.setFont(new Font("Lao Sangam MN", Font.BOLD, 13));
		lblScegliLeTue.setBounds(100, 22, 275, 16);
		frame.getContentPane().add(lblScegliLeTue);
		
		
		
		//------------    ***        bottone "REGISTRATI"         ***    ----------------------
		
		
		btnRegistrati = new JButton("REGISTRATI");
		btnRegistrati.setHorizontalTextPosition(SwingConstants.CENTER);
		btnRegistrati.setOpaque(true);
		btnRegistrati.setForeground(Color.WHITE);
		btnRegistrati.setBackground(Color.BLACK);
		btnRegistrati.setBorder(new LineBorder(Color.WHITE, 1, true));
		btnRegistrati.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		btnRegistrati.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					//registrazione vera e propria
					RegisterGUI();	
				} catch (RemoteException | NotBoundException | NullPointerException e1) {System.out.println("errore REG GUI");}
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				btnRegistrati.setBackground(Color.GREEN);
				btnRegistrati.setForeground(Color.BLACK);
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				btnRegistrati.setBackground(Color.BLACK);
				btnRegistrati.setForeground(Color.WHITE);
			}
		});
		
		//proprietà del bottone
		
		btnRegistrati.setBounds(170, 203, 117, 29);
		frame.getContentPane().add(btnRegistrati);
		
		
		
		
		
		
		//--------------  *** bottone TORNA AL LOGIN   ***   --------------------------
		
		
		JButton btnTornaAlLogin = new JButton("Torna al LOGIN");
		btnTornaAlLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnTornaAlLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				frame.dispose();
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				btnTornaAlLogin.setBackground(Color.YELLOW);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				btnTornaAlLogin.setBackground(Color.WHITE);
			}
		});
		
		//proprietà bottone
		
		btnTornaAlLogin.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		btnTornaAlLogin.setOpaque(true);
		btnTornaAlLogin.setBackground(Color.YELLOW);
		btnTornaAlLogin.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		btnTornaAlLogin.setBounds(313, 232, 117, 29);
		frame.getContentPane().add(btnTornaAlLogin);
		
	
		
	}
	
		//-------------   ***  METODO "RegisterGUI"  ***     -------------------//
	
	
		public void RegisterGUI() throws RemoteException, NotBoundException {
		
		//oggetto remoto
	    Remote remoteObject;
	    //interfaccia estende remote
	    RegisterService serverObject;
	    
		
	    try{
	    	//cerco oggetto remoto
	        Registry r = LocateRegistry.getRegistry(5555);
	        
	        remoteObject = r.lookup("REGISTER-SERVER");
	        
	        serverObject = (RegisterService) remoteObject;
	     
	        //credenziali
		    Nickname = textUsername.getText();
			Password = textPassword.getText();
			
			System.out.println("\nErrore registrazione\n");
			
			//utente prova a registrarsi tramite RMI, controlli in registra_utente
			int registered=serverObject.registra_utente(Nickname,Password);
			
			System.out.println("\nRegistrato\n");
			
			
			//------  ++++    controllo esito della registrazione  ++++   -----------//
				
				
			switch (registered) {
				//registrazione fallita
				case -1:
					JOptionPane.showMessageDialog(frame, "Esiste già un account con questo nome_utente. Scegli un altro nickname e riprova!","Registazione fallita",JOptionPane.ERROR_MESSAGE);
					lblno.setVisible(true);
					
					break;	
				//già registrato
				case 1:
					JOptionPane.showMessageDialog(frame, "Nome utente già esistente. Se non sei tu, scegli un altro nome. Riprova o torna al login!","Nome già esistente",JOptionPane.INFORMATION_MESSAGE);
					lblreg.setVisible(true);
					lblno.setVisible(false);
					lblno1.setVisible(false);
					break;
				//registrazione andata a buon fine
				case 0:
					JOptionPane.showMessageDialog(frame, "Ciao, " + Nickname +"!" +" Hai appena creato il tuo account, ora puoi effettuare il login per giocare contro i tuoi amici!","Registazione avvenuta con successo",JOptionPane.INFORMATION_MESSAGE);
					lblok.setVisible(true);
					lblno.setVisible(false);
					lblno1.setVisible(false);
					btnRegistrati.setVisible(false);
					break;	
				case -2:
					JOptionPane.showMessageDialog(frame, "Campi vuoti. Per registrarti scegli un nome e una password!","Registazione fallita",JOptionPane.ERROR_MESSAGE);
					lblno.setVisible(true);
					lblno1.setVisible(true);
					break;	
				default:
					break;
				}
				
				textPassword.setText(null);
				textUsername.setText(null);
			
		}	    
		//getRegistry
	    catch( RemoteException e) {
	    	// System.out.println("ERRORE");
	    	e.printStackTrace();
	    }
	    
	    //lookup
	    catch(NotBoundException e) {
	    	System.out.println("Not Bound exception");
	    }
	}
}
