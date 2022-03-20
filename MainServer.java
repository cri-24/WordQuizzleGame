
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;


/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */


//classe principale del server

public class MainServer {
	
	
	
	//_________________  *** Dichiarazioni variabili statiche ***   ___________________//
	
	
	private static final int default_port_TCP = 1888;
	private static final int BLOCK_SIZE = 1024;
	
	
	
	//_________________  *** Main ***   ___________________//
	
	
	
	public static void main(String[] args) throws IllegalArgumentException,RemoteException {
		
		
		
		//ServerSocketChannel classe per accettare le richieste dei client su un socket TCP
		ServerSocketChannel server = null;
		Selector selector = null;
		
		//Istanza del WQ_Server
		WQ_Server WQ_op = new WQ_Server();
		WQ_op.checkFile();
		
		//se c'è stato un precedente crash del server qui ho recuperato tutto
		
		//registrazione da remoto
		WQ_op.RegWQ_Service();
		System.out.println("_______   ***  SERVER CONNESSO  ***  _____\n");
		
		//connessione e select dei canali pronti
		Connect(server,selector,WQ_op);
		 
		
	}
		
	//metodo che implementa il ciclo di vita del main server, grazie a un selettore che, in base ai canali pronti, accetta,scrive e legge richieste da parte dei client e delega WQ_Server per le varie operazioni

	static public void Connect(ServerSocketChannel server,Selector selector, WQ_Server WQ_op) {
		
	
		int res=0;
		long punti=0;
		String lista_amici = null;
		String classifica = null;
		JSON_Op j = new JSON_Op(WQ_op.users);
		
		try {
			//apro il ServerSocketChannel
			server = ServerSocketChannel.open();
			//collego indirizzo e porta al ServerSocketChannel
			server.bind(new InetSocketAddress(InetAddress.getLocalHost(), default_port_TCP));
			//imposto modalià non bloccante, altrimenti sarebbe bloccante di default
			server.configureBlocking(false);
			//validOps: restituisce un set di operazioni supportate da questo canale.
			int ops = server.validOps();
			//creo il selector
			selector = Selector.open();
			//registro il canale alla lista dei canali gestiti dal selector e crea la sua key. null è l'attachmnet
			server.register(selector, ops, null);
			
			while(true) {

				
			  selector.selectedKeys().clear();
		      //select si blocca finchè non c'è almeno un canale pronto per qualche op I/O
			  selector.select();
			
				//insieme di chiavi: ognuna mantiene riferimento al proprio canale e lo stato del canale
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();
				
				// Itera sul set di chiavi: ogni volta che faccio la register, passandogli il selettore, creo una chiave che viene inserita nel set
				
				while(iterator.hasNext()) {
				
					//seleziona una (sicuramente rappresenta un canale pronto a fare qualcosa)e poi: key.channel(), key.attachment(), key.isReadble()...
					SelectionKey key = iterator.next(); 
					//rimuovo la chiave dal set dopo averla selezionata
					iterator.remove();
					System.out.println("\nkey iterata: "+key);
						//per ServerSocket
						if (key.isAcceptable()){
							
							System.out.println("\nkey accettata: "+key);
							SocketChannel client = null;
							try {
								client=((ServerSocketChannel)key.channel()).accept();
								
							    //modalità non bloccante per il socketChannel associato al client appena accettato
								client.configureBlocking(false);
								//creo allegato: bytebuffer
								ByteBuffer[] attachments= new ByteBuffer[2];
								attachments[0]=ByteBuffer.allocate(Integer.BYTES);
								attachments[1]=ByteBuffer.allocate(BLOCK_SIZE);
								client.register(selector, SelectionKey.OP_READ,attachments);
						
							    System.out.println("__  ***   Il Server ha accettato un nuovo cliente.\n  ***   ___\n");
							   
							}
							catch (IOException e){System.out.println("Error accepting client: "+e.getMessage());
							key.cancel();
							key.channel().close();
							if(client!=null) client.close();}
						}
						//per i canali leggibili
						else if (key.isReadable()){
							SocketChannel client = null;
							String[] tokens=null;
							
							try {
							
								System.out.println("key leggibile: "+key);
								//ottengo il canale grazie alla key
								client= (SocketChannel) key.channel();
								//ottengo l'attachment registrato al momento della register
							    ByteBuffer[] buf = (ByteBuffer[]) key.attachment();
							    
							    buf[0].clear();
							    buf[1].clear();
							    //leggo dal canale
							    client.read(buf);
							    
							    //position!=limit
							    if (!buf[0].hasRemaining()){ 
							    	buf[0].flip();
							    	//recupero la lunghezza della stringa
							    	int len= buf[0].getInt();
							    	
							    	if(buf[1].position()<len)len=buf[1].position();
							    	//controllo che nel buffer position=l (la stringa è stata letta tutta)
							    	if(buf[1].position()==len){ 
							    		
							    		buf[1].flip(); 
							    		String msg = new String(buf[1].array()).trim();
							    		
							    		
							    		System.out.println("____   ***  Stringa ricevuta dal client in Main Server: " + msg+"   ***  ___\n");
							    		
							    	
							    		
								    		tokens = msg.split(" ");
								    		
								    		//per "PRONTO1" e "PRONTO2"
								    		String c1=null;
								    		String c2=null;
								    		switch(tokens[0]) {
								    		
								    			case "LOGIN":
								    				res = WQ_op.Login(key,tokens[1],tokens[2]);
								    				break;
								    			case "LOGOUT":
								    				res = WQ_op.Logout(tokens[1]);
								    				break;
								    			case "AGGIUNGIAMICO":
								    				res = WQ_op.Aggiungi_Amico (tokens[1], tokens[2]);
								    				break;
								    			case "LISTAAMICI":
								    				lista_amici = WQ_op.Lista_amici(tokens[1]);
								    				res = 3;
								    				break;
								    			case "MOSTRAPUNTEGGIO":
								    				punti = WQ_op.Mostra_Punteggio(tokens[1]);
								    				res=4;
								    				break;
								    			case "MOSTRACLASSIFICA":
								    				classifica = WQ_op.Mostra_Classifica(tokens[1]);
								    				res=5;
								    				break;
								    			case "SFIDAAMICO":
								    				res = WQ_op.Sfida(tokens[1],tokens[2]);
								    				//res=6 accettata, res=-12 rifiutata, res=-13 timeout
								    				System.out.println("________________________   ***  \n SFIDA: utenti "+tokens[1]+" VS "+tokens[2]+" \nchiave dello sfidante : "+key+" \ncodice ricevuto: "+res+"  \n ***   _______________");
								    				break;
								    			
								    			case "OKPRONTO1":
								    				c1="OKPRONTO1";
								    				
								    				WQ_op.CreaSfida(tokens[1],tokens[2],selector);
								    				break;
								    				
								    			case "OKPRONTO2":
								    				c2="OKPRONTO2";
								    				
								    				break;	
								    			case "FINESFIDA":
								    				
									    			 //tornano online i due utenti
									                WQ_op.users.get(tokens[1]).set_Online();
									                j.Aggiorna_JSON();
								    				res= 7;
								    				break;
								    			case "OKDISCONNECT":
								    				WQ_op.users.get(tokens[1]).set_Online();
								    				WQ_op.users.get(tokens[2]).set_Offline();
								    				res=8;
								    			default:
								    				break;
								    		}
							    		
							    		
								    		//registro il canale creando la chiave con operazione scrittura e attachment buf
								    		if(c1==null && c2==null) client.register(selector, SelectionKey.OP_WRITE,msg);
								    		
								    		else if(c1!=null && c1.contains("OKPRONTO1")){ client.register(selector, 0); c1=null;}
								    		else if(c2!=null && c2.contains("OKPRONTO2")){ client.register(selector, 0); c2=null;}
								    
								    		else{System.out.println("errore c1 e c2");}
							    		}
							    	}
							    	else {
							    	System.out.println("Errore. Disconnessione client non aspettata."); 
							    	WQ_op.users.get(WQ_op.getName(key)).set_Offline(); 
							    	key.cancel();
							    	key.channel().close();}
							    	
								}
								
							
							
							catch (IOException e){System.out.println("Error reading from client ");
							key.cancel();
							key.channel().close();
							if(client!=null) client.close();
							}
							    	
					   }
					   else if (key.isWritable()){ //per i canali scrivibili
						   SocketChannel client = null;
						  
						   try{
							    client= (SocketChannel) key.channel();
							    ByteBuffer resp= ByteBuffer.allocate(256);
							    resp.clear();
							    System.out.println("\nCODICE: "+res);
							    String s=null;
							    switch(res) {
							    
							    	//______________   +++  ERRORI  +++   _____________//
							    	
								
								    case -14:
								    	s="PROBLEM";
								    	break;
								    //sfida
								    case -13:
								    	s="TIMEOUTACCEPT";
								    	break;
								    //sfida
								    case -12:
								    	s="SFIDARIFIUTATA";
								    	break;
								    //sfida
								    case -11:
								    	s="ALREADYINMATCH";
								    	break;
								    //sfida
								    case -10:
								    	s="NOTONLINE";
								    	break;
								    //sfida
								    case -9:
								    	s="NOTFRIENDS";
								    	break;
								    //sfida
								    case -8:
								    	s="SFIDASELF";
								    	break;
							    	//aggiungi_amico
							    	case -7:
							    		s="ALREADYFRIENDS";
							    		break;
							    	//aggiungi_amico 
							    	case -6:
							    		s="FRIENDNOTEXISTS";
							    		break;
							    	//aggiungi_amico
							    	case -5:
							    		s="FRIENDSELF";
							    		break;
							    	//logout
							    	case -4:
							    		s="NOLOGIN";
							    		break;
							    	//login
							    	case -3:
							    		s="ALREADYLOGIN";
							    		break;
							    	//login
							    	case -2:
							    		s="PWERR";
							    		break;
							    	//login
							    	case -1:
							    		s="NOREG";
							    		break;
							    		
							    		
							    	//______________   +++ SUCCESSI +++   ____________//
							    		
							    		
							    	//login
							    	case 0:
							    		s="OKLOGIN";
							    		break;
							    	//logout
							    	case 1:
							    		s="OKLOGOUT";
							    		break;
							    	//aggiugi amico
							    	case 2: 
							    		s="OKADDFRIEND";
							    		break;
							    	//lista amcici
							    	case 3:
							    		s="OKLISTA"+"--"+lista_amici;
							    		break;
							    	//mostra punteggio
							    	case 4:
							    		s="OKPUNTI"+"="+punti;
							    		break;
							    	//mostra classifica
							    	case 5:
							    		s="OKCLASSIFICA"+"/"+classifica;
							    		break;
							    	//sfida
							    	case 6:
							    		s="SFIDAACCETTATA";
							    		break;
							    	//sfida
							    	case 7:
							    		s="OKFINESFIDA";
							    		break;
							    	//disconnessione
							    	case 8:
							    		s="RICOMINCIA";
							    		break;
							    	default:
							    		break;
							    	
							    		
							    }
							 
							   System.out.println("\nStringa inviata al client: "+s);
							   
							   
							   	resp = ByteBuffer.wrap((s.getBytes())); 
							   	
							    client.write(resp);
							  
							    //limit!=position
							    if (!resp.hasRemaining()){
									// Non c'è più da scrivere
									resp.clear();
									ByteBuffer length= ByteBuffer.allocate(Integer.BYTES); 
									ByteBuffer message= ByteBuffer.allocate(1024);
									message.put(new byte[1024]);
									message.clear();
									
									ByteBuffer[] bbf = {length, message};
									
									client.register(selector, SelectionKey.OP_READ,bbf);	
									
									if(s.equals("OKLOGOUT")) {
										key.cancel();
							    		key.channel().close();
									}
								
										
							    }    
							    
						   } 
						
						   catch (IOException e){e.printStackTrace();
						   if (key.isValid()) {
							   key.cancel();
								key.channel().close();
						   }
							if(client!=null) client.close();
							}
					   }
				
				}
			}
		
		}
		
		catch (UnknownHostException ex) {ex.printStackTrace(); } 
		catch (IOException e) {e.printStackTrace();}
		
		
	}
	
		
}
