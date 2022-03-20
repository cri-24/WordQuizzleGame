
import java.awt.Image;
import javax.swing.JFrame;


import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JToggleButton;
import javax.swing.border.LineBorder;

import javax.swing.JSeparator;

import javax.swing.SwingConstants;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.Cursor;
import javax.swing.JList;

/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */

public class Game_GUI {

	private JFrame frame;
	private JFrame parentframe;
	
	private JLabel label_account;
	private Image img_account;
	private JLabel lblIlTuoAccount;

	
	private JTextField txtAggiungiAmico;
	private JTextField textSfida;

	
	private JButton btnListaAmici;
	private JToggleButton tglbtnNewToggleButton;
	private JButton btnMostraClassifica;
	private JButton btnMostraPunteggio;
	private JToggleButton tglbtnAggiungiAmico;
	
	private JSeparator separator;
	private JSeparator separator_1;
	
	private JLabel lblUsername;
	private JLabel lblStato;
	private JLabel lblOnline;
	
	private JLabel lblNome;
	private JLabel lblpunti;
	
	private JList<String> lista_amici;
	private JList classifica;
	
	private JLabel lblsf ;
	private JLabel lblSFIDA;
	private JButton btnAccetta;
	private JButton btnRifiuta;

	
	//SOCKET CHANNEL
    SocketChannel client;
    //DATAGRAM SOCKET
    DatagramSocket ds;
    SocketAddress sa;
    //inizializzato quando ClientUDP chiama UDPRequest e rende visibile interfaccia "ACCETTA" o "RIFIUTA",poi usato in bottone click di "ACCETTA" O "RIFIUTA" per rispedire dp al server con UDP
    DatagramPacket dp; 
   
    private String request;
    private String username;
    private String nickAmico;
    //inizializzato in UDPrequest 
  	private String sfidante;
  	//inizializzato in Login con il socket channel del client
   	private MsgTCP msgTCP;
  

    //thread listener UDP requests
    ClientUDP ClientUDP ;
    ExecutorService Listener;
    private JLabel lbltimer;
    
	/**
	 * Create the application.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public Game_GUI(String username, JFrame parentframe) throws UnknownHostException, IOException {
		this.username=username;
		this.parentframe=parentframe;
		this.sfidante=null;
		this.msgTCP=null;
		initialize();	
	   
	}
	
	

	/**
	 * Initialize the contents of the frame.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	private void initialize() throws UnknownHostException, IOException {
		
		
		//FRAME
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(255, 204, 51));
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		//TIMER ACCETTA O RIFIUTA SFIDA

		lbltimer = new JLabel("Hai  3 secondi   per accettare o rifiutare.");
		lbltimer.setHorizontalAlignment(SwingConstants.CENTER);
		lbltimer.setBackground(new Color(102, 204, 255));
		lbltimer.setOpaque(true);
		lbltimer.setFont(new Font("Kokonor", Font.PLAIN, 12));
		lbltimer.setBounds(113, 136, 277, 16);
		frame.getContentPane().add(lbltimer);
		lbltimer.setVisible(false);
		
		
		//BOTTONI RICEZIONE RICHIESTA SFIDA
		btnRifiuta = new JButton("Rifiuta");
		btnRifiuta.setBorder(new LineBorder(new Color(0, 0, 0)));
		btnRifiuta.setOpaque(true);
		btnRifiuta.setFont(new Font("Kokonor", Font.BOLD, 16));
		btnRifiuta.setBackground(new Color(255, 102, 102));
	
		btnRifiuta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnRifiuta.setBounds(273, 155, 117, 22);
		frame.getContentPane().add(btnRifiuta);
		btnRifiuta.setVisible(false);
		
		
		//evento click RIFIUTA da parte dell'avversario sfidato
			btnRifiuta.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					
					byte[] msg = "RIFIUTATA".getBytes();
					dp.setData(msg);
				    try {
						ds.send(dp);
						
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(parentframe,"Ci scusiamo, ma si è verificato un problema con il server. Aspetta qualche istante "+ username +", rifai login e riprova!! ","Problema", JOptionPane.INFORMATION_MESSAGE);
						//GUI LOGIN
						frame.setVisible(false);
						frame.dispose();
						parentframe.setVisible(true);
						parentframe.isAlwaysOnTop();
					}
				    
				    System.out.println("______   INVIO SFIDA RIFIUTATA   ____");
				    //riattivo l'ascolto di richieste UDP
					//ClientUDP.Riattiva();
					btnAccetta.setVisible(false);
					btnRifiuta.setVisible(false);
					lblSFIDA.setVisible(false);
					lblsf.setVisible(false);
					
				}
			});
			

			btnAccetta = new JButton("Accetta");
			btnAccetta.setBorder(new LineBorder(new Color(0, 0, 0)));
			btnAccetta.setOpaque(true);
			btnAccetta.setFont(new Font("Kokonor", Font.BOLD, 16));
			btnAccetta.setBackground(new Color(102, 255, 51));
			btnAccetta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			btnAccetta.setBounds(113, 155, 117, 22);
			frame.getContentPane().add(btnAccetta);
			btnAccetta.setVisible(false);
			
			
			
			//evento click ACCETTA da parte dell'avversario sfidato
			btnAccetta.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					
					byte[] msg = "ACCETTATA".getBytes();
					
					dp.setData(msg, 0, msg.length);
					
				    //invio e ricezione tramite datagram socket al server
				    try {
				    	
						ds.send(dp);
						
						//non ascolta più richieste udp finchè non riattivo gli ascolti con OKFINESFIDA da parte del server
						ClientUDP.StopAscolti();
						
					} catch (IOException e1) { 
						JOptionPane.showMessageDialog(parentframe,"Ci scusiamo, ma si è verificato un problema con il server. Aspetta qualche istante "+ username +", rifai login e riprova!! ","Problema", JOptionPane.INFORMATION_MESSAGE);
						//GUI LOGIN
						frame.setVisible(false);
						frame.dispose();
						parentframe.setVisible(true);
						parentframe.isAlwaysOnTop();
					}
				   
				    System.out.println("\n______   INVIO SFIDA ACCETTATA su UDP  ____\n");
				    String m="OKPRONTO2 "+username+" "+sfidante;
				    System.out.println("\n______   INVIO OKPRONTO2 su TCP  ____\n");
				    
				    
				    //invio conferma al server per allineare i giocatori
				    try {
				    	
						msgTCP.send_msg(m);
						
				    }
					catch(IOException e1){
							JOptionPane.showMessageDialog(parentframe, "Disconnessione improvvisa del server. Fai di nuovo login e riprova.","Disconnessione",JOptionPane.ERROR_MESSAGE);
							frame.setVisible(false);
							frame.dispose();
							parentframe.setVisible(true);
							parentframe.isAlwaysOnTop();
						}
							
				    
				    frame.setVisible(false);
				    try {
				    	 System.out.println("\n______   CAMBIO INTERFACCIA SU CHALLENGE  ____\n");
						Challenge_GUI sfi = new Challenge_GUI(username,sfidante,frame,parentframe,client,ClientUDP);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				    
				    btnAccetta.setVisible(false);
					btnRifiuta.setVisible(false);
					lblSFIDA.setVisible(false);
					lblsf.setVisible(false);
				}
			});
			
	
		
		
		lblsf = new JLabel("Accetti la sfida?");
		lblsf.setForeground(new Color(0, 0, 204));
		lblsf.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		lblsf.setHorizontalAlignment(SwingConstants.CENTER);
		lblsf.setBounds(172, 95, 151, 57);
		frame.getContentPane().add(lblsf);
		lblsf.setVisible(false);
		
		
		//etichetta azzurra sfida riceuta
		lblSFIDA = new JLabel("");
		lblSFIDA.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSFIDA.setForeground(new Color(0, 0, 204));
		
		lblSFIDA.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		lblSFIDA.setFont(new Font("Kohinoor Devanagari", Font.BOLD, 12));
		lblSFIDA.setVerticalAlignment(SwingConstants.TOP);
		lblSFIDA.setHorizontalAlignment(SwingConstants.CENTER);
		lblSFIDA.setBackground(new Color(102, 204, 255));
		lblSFIDA.setOpaque(true);
		lblSFIDA.setBounds(82, 92, 331, 101);
		frame.getContentPane().add(lblSFIDA);
		lblSFIDA.setVisible(false);
		
		

	
		
				//__________  *** Il tuo account  ***___________//
				
				
				lblIlTuoAccount = new JLabel("Il tuo account:");
				lblIlTuoAccount.setFont(new Font("Khmer Sangam MN", Font.PLAIN, 13));
				lblIlTuoAccount.setBounds(22, 7, 95, 16);
				frame.getContentPane().add(lblIlTuoAccount);
				
				lblNome = new JLabel("Nome:");
				lblNome.setFont(new Font("Heiti SC", Font.BOLD, 11));
				lblNome.setBounds(10, 70, 50, 27);
				frame.getContentPane().add(lblNome);
				
				lblUsername = new JLabel(" ");
				lblUsername.setOpaque(true);
				lblUsername.setHorizontalAlignment(SwingConstants.CENTER);
				lblUsername.setHorizontalTextPosition(SwingConstants.CENTER);
				lblUsername.setFont(new Font("Stella", Font.PLAIN, 11));
				lblUsername.setBounds(56, 72, 61, 23);
				frame.getContentPane().add(lblUsername);
				
				lblStato = new JLabel("Stato:");
				lblStato.setFont(new Font("Heiti SC", Font.BOLD, 11));
				lblStato.setBounds(10, 101, 38, 29);
				frame.getContentPane().add(lblStato);
				
				lblOnline = new JLabel("Online");
				lblOnline.setHorizontalAlignment(SwingConstants.CENTER);
				lblOnline.setHorizontalTextPosition(SwingConstants.CENTER);
				lblOnline.setBackground(new Color(0, 204, 0));
				lblOnline.setOpaque(true);
				lblOnline.setForeground(new Color(0, 0, 0));
				lblOnline.setFont(new Font("Stella", Font.PLAIN, 11));
				lblOnline.setBounds(56, 104, 61, 23);
				frame.getContentPane().add(lblOnline);
				
						
				//____________   ***   LOGOUT   ***   _________________//
				
				JButton btnLogout = new JButton("LOGOUT");
				btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				btnLogout.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (JOptionPane.showConfirmDialog(btnLogout, "Sei sicuro di voler uscire da WordQuizzle? ","Conferma", JOptionPane.YES_OPTION)==0) {
							
								Logout();
								
							
						}
					}
				});
				
				//proprietà bottone
				
				btnLogout.setFont(new Font("Kohinoor Devanagari", Font.PLAIN, 11));
				btnLogout.setForeground(new Color(255, 255, 255));
				btnLogout.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
				btnLogout.setOpaque(true);
				btnLogout.setBackground(new Color(255, 0, 0));
				btnLogout.setBounds(56, 35, 61, 23);
				frame.getContentPane().add(btnLogout);
				
				
				//immagine account
				label_account = new JLabel("");
				img_account = new ImageIcon(this.getClass().getResource("/account.png")).getImage();
				label_account.setIcon(new ImageIcon(img_account));
				label_account.setBounds(10, 22, 45, 48);
				frame.getContentPane().add(label_account);
							
		
		//_____________   *** SFIDA UN AMICO  ***   ______________//
		
		
		
		tglbtnNewToggleButton = new JToggleButton("Sfida un amico");
		tglbtnNewToggleButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(textSfida.getText().isEmpty()) JOptionPane.showMessageDialog(parentframe, "Inserisci il nome dell'amico che vuoi sfidare.","Campi vuoti",JOptionPane.ERROR_MESSAGE);
				
				else {
					//prelevo il nome dell'amico da sfidare
					nickAmico = textSfida.getText();
					try { 
					
						
						SfidaAmico(nickAmico);
						textSfida.setText("");
					} 
					catch (IOException e1) {e1.printStackTrace();} 
					catch (InterruptedException e1) {e1.printStackTrace();}
				}
			}

			
		});
		
		tglbtnNewToggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		tglbtnNewToggleButton.setForeground(new Color(255, 255, 0));
		tglbtnNewToggleButton.setFont(new Font("Kokonor", Font.PLAIN, 13));
		tglbtnNewToggleButton.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		tglbtnNewToggleButton.setOpaque(true);
		tglbtnNewToggleButton.setBackground(new Color(0, 102, 255));
		tglbtnNewToggleButton.setBounds(10, 155, 107, 23);
		frame.getContentPane().add(tglbtnNewToggleButton);
		
		//CASELLE DI TESTO
		textSfida = new JTextField();
		textSfida.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		textSfida.setBackground(new Color(204, 204, 255));
		textSfida.setBounds(10, 179, 107, 23);
		frame.getContentPane().add(textSfida);
		textSfida.setColumns(10);
		
		
		//________  ***   MOSTRA CLASSIFICA   ***_____________//
		
		btnMostraClassifica = new JButton("Mostra classifica");
		btnMostraClassifica.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				MostraClassifica();
			}
		});
		
		btnMostraClassifica.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnMostraClassifica.setForeground(new Color(255, 255, 0));
		btnMostraClassifica.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		btnMostraClassifica.setBackground(new Color(153, 102, 255));
		btnMostraClassifica.setOpaque(true);
		btnMostraClassifica.setHorizontalTextPosition(SwingConstants.CENTER);
		btnMostraClassifica.setFont(new Font("Korolev Rounded", Font.PLAIN, 13));
		btnMostraClassifica.setBounds(141, 55, 107, 25);
		frame.getContentPane().add(btnMostraClassifica);
		
		
		//classifica come lista
		classifica = new JList();
		classifica.setBounds(141, 92, 107, 149);
		frame.getContentPane().add(classifica);
		
		
		//_________  *** MOSTRA PUNTEGGIO  ***  _______________//
		
		
		btnMostraPunteggio = new JButton("Mostra punteggio");
		btnMostraPunteggio.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				MostraPunteggio();
			}
		});
		btnMostraPunteggio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnMostraPunteggio.setForeground(new Color(255, 255, 0));
		btnMostraPunteggio.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		btnMostraPunteggio.setBackground(new Color(153, 102, 255));
		btnMostraPunteggio.setOpaque(true);
		btnMostraPunteggio.setHorizontalTextPosition(SwingConstants.CENTER);
		btnMostraPunteggio.setFont(new Font("Korolev Rounded", Font.PLAIN, 13));
		btnMostraPunteggio.setBounds(228, 253, 129, 22);
		frame.getContentPane().add(btnMostraPunteggio);
		
		
		lblpunti = new JLabel(" ");
		frame.getContentPane().add(lblpunti);
		
		lblpunti.setFont(new Font("Geeza Pro", Font.PLAIN, 17));
		lblpunti.setHorizontalTextPosition(SwingConstants.CENTER);
		lblpunti.setHorizontalAlignment(SwingConstants.CENTER);
		lblpunti.setBounds(260, 167, 61, 47);
		
		
		//_________  *** LISTA AMICI  ***  _______________//
		
		
		//lista amici
		lista_amici = new JList();
		lista_amici.setBounds(341, 91, 103, 150);
		frame.getContentPane().add(lista_amici);
		
		
		
		//bottone
		btnListaAmici = new JButton("Lista Amici");
		btnListaAmici.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				ListaAmici();
			}
		});
		
		btnListaAmici.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnListaAmici.setForeground(new Color(255, 255, 0));
		btnListaAmici.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		btnListaAmici.setBackground(new Color(153, 102, 255));
		btnListaAmici.setOpaque(true);
		btnListaAmici.setHorizontalTextPosition(SwingConstants.CENTER);
		btnListaAmici.setFont(new Font("Korolev Rounded", Font.PLAIN, 13));
		btnListaAmici.setBounds(337, 56, 107, 23);
		frame.getContentPane().add(btnListaAmici);
		
		
		//_________  *** AGGIUNGI AMICO  ***  _______________//
		
		tglbtnAggiungiAmico = new JToggleButton("Aggiungi un amico");
		tglbtnAggiungiAmico.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		tglbtnAggiungiAmico.setHorizontalTextPosition(SwingConstants.CENTER);
		tglbtnAggiungiAmico.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if(txtAggiungiAmico.getText().isEmpty()) JOptionPane.showMessageDialog(parentframe, "Inserisci il nome dell'amico che vuoi aggiungere.","Campi vuoti",JOptionPane.ERROR_MESSAGE);
				
				else {nickAmico = txtAggiungiAmico.getText();
					try {
						
						AddFriend(nickAmico);
						txtAggiungiAmico.setText("");
					} 
					catch (IOException e1) {e1.printStackTrace();}
				}
			}	
		});
		
		//proprietà bottone
		
		tglbtnAggiungiAmico.setForeground(new Color(255, 255, 0));
		tglbtnAggiungiAmico.setFont(new Font("Kokonor", Font.BOLD, 12));
		tglbtnAggiungiAmico.setOpaque(true);
		tglbtnAggiungiAmico.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		tglbtnAggiungiAmico.setBackground(new Color(0, 102, 255));
		tglbtnAggiungiAmico.setBounds(10, 214, 107, 23);
		frame.getContentPane().add(tglbtnAggiungiAmico);
		
		//casella di testo di nickamico
		txtAggiungiAmico = new JTextField("");
		txtAggiungiAmico.setForeground(Color.BLACK);	
		txtAggiungiAmico.setFont(new Font("Kefa", Font.PLAIN, 13));
		txtAggiungiAmico.setColumns(10);
		txtAggiungiAmico.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		txtAggiungiAmico.setBackground(new Color(204, 204, 255));
		txtAggiungiAmico.setBounds(10, 238, 107, 23);
		frame.getContentPane().add(txtAggiungiAmico);
		
		
		//Separatori
		
		separator = new JSeparator();
		separator.setForeground(new Color(0, 0, 0));
		separator.setBounds(0, 137, 129, 12);
		frame.getContentPane().add(separator);
		
		separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setForeground(new Color(0, 0, 0));
		separator_1.setBounds(125, 6, 80, 266);
		frame.getContentPane().add(separator_1);
		
		
		//Immagini
		
		JLabel labelsfondo = new JLabel("");
		Image sfondo = new ImageIcon(this.getClass().getResource("/sfondo.png")).getImage();
		labelsfondo.setIcon(new ImageIcon(sfondo));
		labelsfondo.setBounds(179, -24, 278, 338);
		frame.getContentPane().add(labelsfondo);		
		
		
	}

	//____________ *** RICEZIONE RICHIESTA SFIDA ***  __________//
	
	
	
	//gestione GUI di chi riceve la richiesta di sfida, chiamata dalla classe ClientUDP
	public void UDPRequest(String sf,DatagramPacket dp) {
		
		
		sfidante=sf;
		this.dp=dp;
		lblSFIDA.setText("Il tuo amico "+ sfidante + " desidera giocare contro di te");
		lbltimer.setVisible(true);
		lblsf.setVisible(true);
		btnAccetta.setVisible(true);
		btnRifiuta.setVisible(true);
		lblSFIDA.setVisible(true);
		
		
	}
	
	//____________ *** TIMEOUT RICEZIONE SFIDA ***  __________//
	
	
	
		//gestione GUI di timeout
		public void TimeoutAccept() {
			
			lbltimer.setVisible(false);
			lblsf.setVisible(false);
			btnAccetta.setVisible(false);
			btnRifiuta.setVisible(false);
			lblSFIDA.setVisible(false);
			
		}


	//________________  *** "LOGIN"  ***  _______________//
	
	public  void Login(String name, String passw) {
		
		//campi nulli già controllati in main GUI
		//apro conn TCP con il server
		try {
			client = SocketChannel.open(new InetSocketAddress(InetAddress.getLocalHost(), 1888));
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(parentframe, "Host sconosciuto. Riprova","Connessione rifiutata",JOptionPane.ERROR_MESSAGE);
		
		} catch (IOException e) {
			JOptionPane.showMessageDialog(parentframe, "Server non ancora connesso. Riprova","Connessione rifiutata",JOptionPane.ERROR_MESSAGE);
			
		}
		
		msgTCP = new MsgTCP(client);
		
		request="LOGIN "+name+" "+passw;
		try {
			try {
				MsgServer(request,null);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, "Errore di comunicazione con il server. Riprova","Comunicazione rifiutata",JOptionPane.ERROR_MESSAGE);
			
		}	
		System.out.println("Richiesta da inviare al server:" + request);
		//inizializzazione comunicazione UDP
		try {
			//ricavo indirizzo locale associato all'indirizzo del socket locale(bindaddress, cioè socketAddress)
			ds = new DatagramSocket(client.getLocalAddress());
		} catch (SocketException e) {
			JOptionPane.showMessageDialog(frame, "Errore di creazione UDP socket. Riprova","Comunicazione rifiutata",JOptionPane.ERROR_MESSAGE);
			frame.setVisible(false);
			frame.dispose();
			parentframe.setVisible(true);
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(parentframe, "Non c'è connessione. Riprova","Connessione rifiutata",JOptionPane.ERROR_MESSAGE);
			frame.setVisible(false);
			frame.dispose();
			parentframe.setVisible(true);
		}
		
		//istanza del ClientUDP
	    ClientUDP = new ClientUDP(this,ds,name); 
	    
	    System.out.println("______   LISTENER UDP ATTIVO   ____");
	    
	    //avvio thread per richieste UDP 
	    Listener = Executors.newSingleThreadExecutor();
	    Listener.execute(ClientUDP);
	}

	
	//_____________  ***  "LOGOUT"  *** _____________//
	
	public void Logout()  {
		
		request="LOGOUT "+username;
		
		try {
			MsgServer(request,null);
		} catch (IOException | InterruptedException e) {
			
			e.printStackTrace();
		}
		//chiudo il datagram socket
		if(ds != null && client != null) {
	            if(!ds.isClosed()){ ds.close(); }
	            else{  ClientUDP = null;  }
        }
		//Termina il task UDP facendo terminare le operazioni già iniziate
		Listener.shutdown();
		
		
	}

	//_____________  ***  AGGIUGNI UN AMICO  ***  ____________//
	
	
	public void AddFriend(String amico) throws UnknownHostException, IOException {
		
		if(amico==null ) {JOptionPane.showMessageDialog(parentframe, "Campi nulli. Riprova.","Credenziali vuote",JOptionPane.ERROR_MESSAGE);}
		
		request="AGGIUNGIAMICO "+username+" "+amico;

		try {
			MsgServer(request,amico);
		} catch (IOException | InterruptedException e) {
			
			e.printStackTrace();
		}

		frame.setVisible(true);
		
	}
	
	
		//_____________  ***  LISTA AMICI  ***  ____________//
		
	
		public void ListaAmici() {
			
			request="LISTAAMICI "+username;
			

			try {
				MsgServer(request,null);
			} catch (IOException | InterruptedException e) {
				
				e.printStackTrace();
			}
	
			frame.setVisible(true);
		}
	
		//_____________  ***  MOSTRA CLASSIFICA  ***  ____________//
		
		
		public void MostraClassifica() {
				
			request="MOSTRACLASSIFICA "+username;
				

			try {
				MsgServer(request,null);
			} catch (IOException | InterruptedException e) {
			
				e.printStackTrace();
			}
	
			frame.setVisible(true);
		}
		
		//__________  *** MOSTRA PUNTEGGIO  ***  _____________//
		
		
		public void MostraPunteggio() {
			
			request="MOSTRAPUNTEGGIO "+username;
				
		
				try {
					MsgServer(request,null);
				} catch (IOException | InterruptedException e) {
					
					e.printStackTrace();
				}
		
			frame.setVisible(true);
		}
		
		
		//__________   *** SFIDA AMICO  ***   _________//
		
		
		public void SfidaAmico(String amico) throws UnknownHostException, IOException, InterruptedException {
			
			if(amico==null) {JOptionPane.showMessageDialog(parentframe, "Campi nulli. Riprova.","Credenziali vuote",JOptionPane.ERROR_MESSAGE);}
			
			request="SFIDAAMICO "+username+" "+amico;
		
			MsgServer(request,amico);	
			
		}
		
	//________________  *** Comunicazione con il server: invio e ricezione messaggi ***  ______________________//
	
	
	
	public void MsgServer(String request,String amico) throws IOException, InterruptedException {
		
		String esito =null;

		String[] tokens=null;
		String friendlist=null;
		String rank= null;
		String punti=null;
		
		try {
			//invio richiesta al server
			msgTCP.send_msg(request);
		
			//ricevo risposta dal server
			esito = msgTCP.receive_msg();
			
			System.out.println("\nRisposta del server: "+esito);
			
		}
		catch(IOException e){
			JOptionPane.showMessageDialog(parentframe, "Disconnessione improvvisa del server. Fai di nuovo login e riprova.","Disconnessione",JOptionPane.ERROR_MESSAGE);
			frame.setVisible(false);
			frame.dispose();
			parentframe.setVisible(true);
			parentframe.isAlwaysOnTop();
		}
			
			
			//caso lista amici: risposta "LISTAAMICI+{nome, nome, ..}"	
			if(esito.contains("--")) {
				//tokenizzo
				tokens = esito.split("--"); 
			    esito = tokens[0];
			    friendlist = tokens[1];
			  
			   
			}
			
			//caso mostra classifica: risposta "MOSTRACLASSIFICA/{<nome,punt>...}"
			else if(esito.contains("/")){
				//tokenizzo
				tokens = esito.split("/"); 
			    esito = tokens[0];
			    rank = tokens[1];
			    System.out.println("Esito classifica: "+esito);
			    
			}
			
			//caso mostra punteggio 
			
			else if(esito.contains("=")){
				//tokenizzo
				tokens = esito.split("="); 
			    esito = tokens[0];
			    punti = tokens[1];
			    System.out.println("punti: "+punti);
			    
			}
			
			switch(esito) {
			
				
				//_____________   +++   ERRORI  +++   _______________//
			
				
				//login
				case "ALREADYLOGIN":
						
						JOptionPane.showMessageDialog(parentframe, "Sei già loggato","login già effettuato",JOptionPane.ERROR_MESSAGE);
						
						break;
						
				case "PWERR":
					
					JOptionPane.showMessageDialog(parentframe, "Password errata. Inserisci di nuovo il tuo nick e la tua password.","Password errata",JOptionPane.ERROR_MESSAGE);
					break;
					
				case "NOREG":
					
					JOptionPane.showMessageDialog(parentframe, "Non sei registrato o hai sbagliato a digitare il nickUtente con cui ti sei registrato. Ritenta!","Login fallita",JOptionPane.ERROR_MESSAGE);
					break;
				
				//logout	
				case "NOLOGIN":
					JOptionPane.showMessageDialog(parentframe, "Non hai effettuato ancora il login","Logout fallita",JOptionPane.ERROR_MESSAGE);
					break;
					
				//aggiungi amico	
				case "ALREADYFRIENDS":
					JOptionPane.showMessageDialog(parentframe, "Sei già amico con "+ amico +"!","Aggiungi amico fallita",JOptionPane.ERROR_MESSAGE);
					break;
				
				case "FRIENDNOTEXISTS":
					JOptionPane.showMessageDialog(parentframe, "Mi dispiace, ma "+ amico + " non è registrato o forse hai sbagliato a digitare il suo nome. Attenzione alle maiuscole e riprova","Aggiungi amico fallita",JOptionPane.ERROR_MESSAGE);
					break;
				
				case "FRIENDSELF":
					JOptionPane.showMessageDialog(parentframe, "Sei già amico di te stesso, digita un altro nome e riprova!","Aggiungi amico fallita",JOptionPane.ERROR_MESSAGE);
					break;
					
				//sfida amico
				case "NOTFRIENDS":
					JOptionPane.showMessageDialog(parentframe, "Non sei ancora amico con " +nickAmico+"! Aggiungilo alla lista dei tuoi amici e sfidalo!","Non amici",JOptionPane.ERROR_MESSAGE);
					break;
					
				case "SFIDARIFIUTATA":
				
					JOptionPane.showMessageDialog(parentframe, "Purtroppo "+ amico + " non ha accettato la tua sfida. Scegli un altro amico da sfidare!","Sfida rifiutata",JOptionPane.INFORMATION_MESSAGE);
					
					break;
					
				case "TIMEOUTACCEPT":
					JOptionPane.showMessageDialog(parentframe, "Mi dispiace. Il tuo amico "+ amico + " non ha risposto alla tua richiesta entro 3 secondi. Riprova.","Timeout scaduto",JOptionPane.INFORMATION_MESSAGE);
					
					
					break;
					
				case "NOTONLINE":
					JOptionPane.showMessageDialog(parentframe, "Mi dispiace, ma il tuo amico "+ amico + " non è online. ","Non online",JOptionPane.INFORMATION_MESSAGE);
					break;
					
				case "SFIDASELF":
					JOptionPane.showMessageDialog(parentframe, "Non puoi sfidare te stesso. Scegli un amico e sfidalo! ","Non te stesso",JOptionPane.INFORMATION_MESSAGE);
					break;
					
				case "ALREADYINMATCH":
					JOptionPane.showMessageDialog(parentframe, "Mi dispiace, ma "+ nickAmico+ " è già impegnato in un'altra partita. Prova a sfidarlo più tardi!","Amico già impegnato",JOptionPane.INFORMATION_MESSAGE);
					break;
					
					
				//_____________   +++    SUCCESSI   +++   _____________//
					
					
				//login	
				case "OKLOGIN":
					
					JOptionPane.showMessageDialog(parentframe, "Bentornato/a "+username+"! Sfida i tuoi amici e buon divertimento!","Login ok",JOptionPane.INFORMATION_MESSAGE);
					lblUsername.setText(username);
					parentframe.setVisible(false);
					this.frame.setVisible(true);
					break;
				
				//logout	
				case "OKLOGOUT":
					
					JOptionPane.showMessageDialog(parentframe, "Logout effettuato con successo. Torna a trovarci presto "+ username +"!","Logout ok",JOptionPane.INFORMATION_MESSAGE);
					frame.setVisible(false);
					if (client!=null ) {
						System.out.println("Socket chiuso");
						client.close();
					}
					parentframe.setVisible(true);
					break;	
				//aggiungi amico	
				case "OKADDFRIEND":
					JOptionPane.showMessageDialog(parentframe, "Hai appena aggiunto "+ amico +" alla tua lista amici, fai click su <LISTA AMICI> per visualizzarla.","Aggiungi amico ok",JOptionPane.INFORMATION_MESSAGE);
					frame.setVisible(false);
					break;
					
				//mostra lista amici
				case "OKLISTA":
					//visualizzo la tabella degli amici:
					if(friendlist.equals("VUOTA")) JOptionPane.showMessageDialog(parentframe, "Non hai ancora amici, digita il nome del tuo primo amico e fai click su Aggiungi un amico ","Lista amici ok",JOptionPane.INFORMATION_MESSAGE);
					else {
						@SuppressWarnings("rawtypes")
						DefaultListModel model = new DefaultListModel();
						//tokenizzo la lista
						String[] toks = friendlist.split(",");
						 
						for (String t : toks){ model.addElement(t); System.out.println(t);}
						//popolo la lista della GUI
						lista_amici.setModel(model);
						
					}
					break;
					
				//mostra punteggio
				case "OKPUNTI":
					
					lblpunti.setText(punti);
					
					break;
					
				//mostra classfica	
				case "OKCLASSIFICA":
				
					//visualizzo la classifica
					if(rank.equals("VUOTA")) JOptionPane.showMessageDialog(parentframe, "Non hai ancora amici, digita il nome del tuo primo amico e fai click su Aggiungi un amico ","classifica ok",JOptionPane.INFORMATION_MESSAGE);
					
					else {
						
						DefaultListModel model = new DefaultListModel();
						//tokenizzo la lista
						String[] toks = rank.split(",");
						for (String t : toks){ model.addElement(t); System.out.println(t);}
						//popolo la classifica JList(componente della GUI)
						classifica.setModel(model);		
					}
					break;
					
				//il server invia SFIDAACCETTATA a chi invia la sfida (sfidante)
				case "SFIDAACCETTATA":
					try {
					//conferma al server
					msgTCP.send_msg("OKPRONTO1 "+ username + " "+ nickAmico);
					}
					catch(IOException e){
						JOptionPane.showMessageDialog(parentframe,"Ci scusiamo, ma si è verificato un problema con il server. Aspetta qualche istante "+ username +", rifai login e riprova!! ","Problema", JOptionPane.INFORMATION_MESSAGE);
						//GUI LOGIN
						frame.setVisible(false);
						frame.dispose();
						parentframe.setVisible(true);
						parentframe.isAlwaysOnTop();
					}
					frame.setVisible(false);
					//nickAmico è inizializzato solo per chi clicca su SFIDA AMICO
					Challenge_GUI sfida = new Challenge_GUI(username,nickAmico,frame,parentframe,client,ClientUDP);
					break;
					
				case  "RICOMINCIA":
						JOptionPane.showMessageDialog(parentframe,"Ci dispiace che il tuo avversario si sia disconnesso. Tranquillo, perchè ovviamente il punteggio della sfida sospesa non verrà considerato.","Avverario disconnesso", JOptionPane.INFORMATION_MESSAGE); 
						parentframe.setVisible(false);
					break;	
				default:
					JOptionPane.showMessageDialog(parentframe,"Game gui:Ci scusiamo, ma si è verificato un problema con il server. Aspetta qualche istante "+ username +", rifai login e riprova!! ","Problema", JOptionPane.INFORMATION_MESSAGE);
					//GUI LOGIN
					frame.setVisible(false);
					frame.dispose();
					parentframe.setVisible(true);
					parentframe.isAlwaysOnTop();
				
					break;
		
			}
			
		
		
	}
	
	
}

	


