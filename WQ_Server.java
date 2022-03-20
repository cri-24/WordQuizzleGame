
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;


/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */


	class WQ_Server implements WQ_Operations_Interface{
	
	//__________   ***  CONSTANT ***   _____________//
	
		
		
	
	int default_port_RMI = 5555;
	String NameServiceRMI = "REGISTER-SERVER";
	
	
	
		
	
	//__________   *** STRUTTURE DATI ***  __________//
	
	
	
	
	//HashMap di tutti gli utenti registrati a WQ
	ConcurrentHashMap<String,User> users;
	
	//HashMap di chiavi degli utenti attivi
	ConcurrentHashMap<String,SelectionKey> Keys;
	
	//HashMap di chiavi degli utenti attivi
	ConcurrentHashMap<SelectionKey,String> Names;
	
	//Threadpool per gestire le sfide in corso(può crescere dinamicamente riutilizzando i Thread creati precedentemente)
    ExecutorService ThreadpoolSfide = Executors.newCachedThreadPool();
	
	//operazioni su file json 
	public JSON_Op json_op;
	
	
	
	
	//____________  *** Costruttore  ***  _______________//
	
	public WQ_Server() {
		users = new ConcurrentHashMap <String,User>();
		Keys = new ConcurrentHashMap <String,SelectionKey>();
		Names = new ConcurrentHashMap<SelectionKey,String>();
		json_op=new JSON_Op(users); 
		
	}
	
	
	
	//___________   ***  RIPRISTINA USERS  ***  __________//
	
	
	
	//all'avvio del server ripristina le informazioni da persistere degli utenti registrati
	public void checkFile () {
		
		users = json_op.checkServer(users);
		
	}
	
	//______   *** GET KEY  ***   ______//
	
	
	
	//ritorna la selection key dell'utente relativo a username
	public SelectionKey getSelectionKey(String username) {
		return Keys.get(username);
	}
	
	//ritorna il nome del giocatore corrispondente alla chiave per settarlo offline se chiude improvvisamente
	public String getName(SelectionKey key) {
		return Names.get(key);
	}
	
	
	//_____________  *** Register RMI ***   ____________//
	
	
	//permette all'utente di registrarsi. crea registry e lo stub per il servizio remoto.
	 public void RegWQ_Service() throws RemoteException,IllegalArgumentException{
	
			try {
				
				//creazione istanza dell'oggetto di tipo Register su cui fare registra_utente
				Register RMIreg = new Register(users);
				
				//creazione stub di tipo RegisterService che rappresenta l'oggetto remoto
				RegisterService stub = (RegisterService) UnicastRemoteObject.exportObject(RMIreg, 0);
				//Creazione di un registry sulla porta di default
				LocateRegistry.createRegistry(5555);
				
				//Pubblicazione dello stub nel registry
				Registry r = LocateRegistry.getRegistry(5555);
				
				// Pubblicazione dello stub nel registry
			    r.rebind("REGISTER-SERVER",stub);
			    
			    System.out.println("____  ***  CREAZIONE REGISTRY,STUB E REBIND  *** _____");
			    
			}
			catch(RemoteException e){System.err.println("Server RMI procedure");}
			
	}
	 
	 
	
	 //____________   *** LOGIN ***   ______________//
	
	/* 0: successo, setto online,inserisco la chiave in Keys
	 * 
	 * -3: utente già online 
	 * -1: utente non registrato
	 * -2: password errata
	 */
	 public int Login(SelectionKey key,String nickUtente,String password)throws IllegalArgumentException {
		
		if (users==null) {throw new IllegalArgumentException("NickUtente o Password nulli");}
		//utente non registrato
		else if(users.containsKey(nickUtente)==false) {
			return -1;
		}
		//utente già online(se apro due terminali diversi e accedo con stesso nome)
		else if (users.get(nickUtente).get_state().equals(state.online) || users.get(nickUtente).get_state().equals(state.busy_inmatch)) {
				
				return -3;
		}
		//utente registrato 
		else if( users.containsKey(nickUtente) && password.equals(users.get(nickUtente).getPassw()) ) {
						//imposto stato:online
						users.get(nickUtente).set_Online();
						Keys.put(nickUtente, key);
						Names.put(key, nickUtente);
						System.out.println(Names.get(key));
						return 0;
		}
		
		
		
		//password sbagliata 
		else { 
			return -2;
		}
	
	}
	
	
	//____________   *** LOGOUT ***   ______________//
	
	
	//ritorna un codice che viene interpetato dal mainserver:
	 /*
	  * -4: utente fa logout, ma non online. errore del server
	  * -1: utente non registrato. impossibile
	  * 1: successo -> offline e cancellazione key e chiusura del channel
	  */
	public int Logout(String nickUtente)throws IllegalArgumentException, IOException {
			
		try {
			if(users == null || nickUtente == null) {throw new IllegalArgumentException("NickUtente o Password nulli");}
			
			//return 1 : se è registato e loggato setta a offline lo stato
			else if(users.containsKey(nickUtente) && users.get(nickUtente).get_state().equals(state.online) )
				try {
					 	users.get(nickUtente).set_Offline();
					 	
						Names.remove(Keys.get(nickUtente));
						Keys.remove(nickUtente);
						
						System.out.println("Logout effettuato con successo. STATO: "+users.get(nickUtente).get_state());
						return 1;
					
				} catch (Exception e) {e.printStackTrace();}
			else if(users.containsKey(nickUtente) && !users.get(nickUtente).get_state().equals(state.online) ){ 
				return -4;
			}
			
			
		}
		catch(IllegalArgumentException e){e.printStackTrace();}
		//return -1: utente non registrato
		return -1;
	}
	
	
	
	//____________   *** AGGIUNGI_AMICO ***   ______________//
	
	
	//ritorna un codice che viene interpretato dal mainserver: 
	/* -5: aggiunge se stesso
	 * -6: nickAmico non è registrato
	 * -7: già amici
	 * 
	 * 2: aggiunto nickAmico agli amici e aggiornato file JSON
	 */
	public int Aggiungi_Amico(String nickUtente, String nickAmico) {
		
		JSON_Op json_op = new JSON_Op(users); 
		
		//parametri nulli
		if(users == null ) {throw new IllegalArgumentException("NickUtente o Password nulli");}
		
		//un utente non può aggiungere se stesso
		else if (nickUtente.equals(nickAmico))return -5;
		
		//nickAmico non esiste
		else if(users.containsKey(nickAmico) == false) return -6;
		
		//amicizia già esistente
		else if(users.containsKey(nickAmico) && users.get(nickUtente).getListaAmici().contains(nickAmico)) return -7;
		
		//amicizia aggiunta con successo ad entrambe le liste_amici
		else {
			users.get(nickUtente).getListaAmici().add(nickAmico);
			users.get(nickAmico).getListaAmici().add(nickUtente);
			json_op.Aggiorna_JSON();
			return 2;
		}
	}	
		
	//_____________ ***  LISTA AMICI  ***     __________//
	
	
	//ritorna la stringa che rappresenta la lista degli amici in formato json oppure "VUOTA"
	public String Lista_amici(String nickUtente)throws IllegalArgumentException {
		
		//parametri nulli
		if(users == null || nickUtente == null ) {throw new IllegalArgumentException("NickUtente o Password nulli");}
				
		String StrAmici = null;
		
		try {
			//ottengo la lista 
			LinkedList<String> amici= users.get(nickUtente).getListaAmici();
			
			if(amici != null) {
				//non ha ancora amici
				if(amici.isEmpty())
					return "VUOTA";
				else {
					//la traformo in stringa
					Gson gson = new Gson();
					//deserializzo
					StrAmici = new String(gson.toJson(amici));
					System.out.println("lista gson : "+ StrAmici);
					
				}	
			}
			
		}
		catch(IllegalArgumentException e){e.printStackTrace();}
		return StrAmici;
		
	}

	
	//_________    ***   MOSTRA CLASSIFICA   ***    ____________//
	
	
	//ritorna la stringa che rappresentala classifica informato json o la stringa "VUOTA" nel caso in cui nickUtente non abbia ancora amici
	public String Mostra_Classifica(String nickUtente)throws IllegalArgumentException {
		
		//parametri nulli
		if(users == null || nickUtente == null) {throw new IllegalArgumentException("NickUtente o Password nulli");}
				
		String classifica = null;
		
		
		try {
			//ottengo la lista 
			LinkedList<String> amici= users.get(nickUtente).getListaAmici();
			HashMap<String,Long> rank = new HashMap<String,Long>();
			LinkedHashMap<String, Long> sorted_rank = new LinkedHashMap<String, Long>();
			
			if(amici != null) {
				//non ha ancora amici
				if(amici.isEmpty())
					return "VUOTA";
				else {
					//riempo la classifica
					for(String amico : amici) {
						long punteggio = users.get(amico).getPunteggioTot();
						rank.put(amico , punteggio );
						
						
						
					}
					long p=users.get(nickUtente).getPunteggioTot();
					rank.put(nickUtente , p);
					sorted_rank=SortedHashMap(rank);
					//trasformo in gson
					Gson gson = new Gson();
					classifica = new String(gson.toJson(sorted_rank));
					System.out.println("classifica gson : "+ classifica);
				}
			}
			
			
			
		}
		catch(IllegalArgumentException e){e.printStackTrace();}
		
		return classifica;
	}
	
	
	//Ordina HashMap in base al punteggio in ordine decrescente per la classifica. ritrona la classifica ordinata.
	public LinkedHashMap<String, Long> SortedHashMap(HashMap<String, Long> classifica) throws IllegalArgumentException{
		
		//parametri nulli
		if(users == null ) {throw new IllegalArgumentException("NickUtente o Password nulli");}
			
		//due arraylist di username e punteggi 
		ArrayList<String> chiavi = new ArrayList<String>(classifica.keySet()); 
		ArrayList<Long> punteggi = new ArrayList<Long>(classifica.values()); 
		
		//Le due liste ordiante in ordine decrescente
		Collections.sort(chiavi, Collections.reverseOrder()); 
		Collections.sort(punteggi, Collections.reverseOrder()); 
		
		LinkedHashMap<String, Long> sorted_rank = new LinkedHashMap<String, Long>(); 
		
		//iteratore dei punteggi
		Iterator<Long> IteratorPunteggi = punteggi.iterator(); 
		
		while(IteratorPunteggi.hasNext()) { 
			//per ogni punteggio
			Long punteggio = IteratorPunteggi.next();
			
			//iteratore delle chiavi
			Iterator<String> IteratorChiavi = chiavi.iterator(); 
			
			while(IteratorChiavi.hasNext()) { 
				//per ogni chiave
				String chiave = IteratorChiavi.next(); 
				
				//
				Long punt1 = classifica.get(chiave);
				Long punt2 = punteggio; 
				
				if(punt1 == punt2) { 
					
					 //se sono uguali rimuovo la chiave dall'iteratore
					IteratorChiavi.remove();
					//inserisco chiave e punteggio nella classifica ordinata
					sorted_rank.put(chiave, punteggio); 
					break; 
				}
			}
		}
		return sorted_rank;
	}
	

	//___________ *** MOSTRA PUNTEGGIO  ***  __________//
	
	
	//ritorna il punteggio del nickUtente
	public long Mostra_Punteggio(String nickUtente) {
		
		//parametri nulli
		if(users == null ) {throw new IllegalArgumentException("NickUtente o Password nulli");}
		long punti = users.get(nickUtente).getPunteggioTot();
		return punti;
	}
 
	
	// _____________   ***   SFIDA   ***  _____________//
	
	
	//ritornaun codice che interpreterà il mainserver:
	/*
	 * -14: problema non identificato
	 * -13: scaduto timeout per accettare o rifiutare la sfida da parte di nickUtente
	 * -12: sfida rifiutata da nickAmico
	 * -11: nickAmico già impegnato in un'altra sfida
	 * -10: nickAmico offline o registrato, ma non ancora online
	 * -9: nickAmico non è ancora amcio di nickUtente
	 * 
	 * 6: sfida accettata
	 * 
	 */
	public int Sfida( String nickUtente,String nickAmico) {
		
		//parametri nulli
		if(users == null || nickUtente==null || nickAmico==null ) {throw new IllegalArgumentException("NickUtente o Password nulli");}
		
		//sfida a se stesso
		if((nickUtente).equals(nickAmico)) return -8;
		
		//non amici
		else if(users.get(nickUtente).getListaAmici().contains(nickAmico)==false) return -9;
		
		//sono amici, ma amico non è online (offline)
		else if(users.get(nickAmico).get_state().equals(state.offline) || users.get(nickAmico).get_state().equals(state.registered)) return -10;
		
		//sono amici, ma amico non è online (busy_inmatch)
		else if(users.get(nickAmico).get_state().equals(state.busy_inmatch) ) return -11;
		
		//richiesta di sfida corretta
		else if (users.get(nickUtente).getListaAmici().contains(nickAmico) && users.get(nickAmico).get_state().equals(state.online)){
			
			
			System.out.println("SFIDA INVIATA A "+nickAmico+" DA PARTE DI "+nickUtente);
			
			return UDP_msg(nickUtente,nickAmico);
			
			
		}
		else {//problema non identificato
			return -14;
		}
		
	}
	
	//_____________   *** UDP_MSG  ***   _________________//
	
	//invia tramite UDP la richiesta di sfida, valida per 5 secondi
	//ritorna 6 se sfida accettata e -12 se sfida rifiutata, -13 se scade timeout, altrimenti -14
	public int UDP_msg(String nickUtente,String nickAmico){
		
		//recupero la selection key dell'avversario
		SelectionKey key_avvers = Keys.get(nickAmico);
		
		String data = null;
		//errore generico -14
		int resp = -14;
		
		//connessione UDP con nickAmico a cui mando richiesta di sfida da parte di nickUtente
		try{
			//recupero informazioni dell'avversario
			SocketChannel sc = (SocketChannel) key_avvers.channel();
			
		
	        //buffer come allegato
	        ByteBuffer[] buf = (ByteBuffer[]) key_avvers.attachment();
	        //puntatore all'inizio
	       buf[0].clear();
	       buf[1].clear();
	        
	        //datagram socket tramite cui invio il datagram packet, una porta qualsiasi disponibile su pc
	        DatagramSocket ds = new DatagramSocket();
	       
	       
	        SocketAddress sa = sc.getRemoteAddress();
	      
	        //preparo il datatgram packet da inviare
	        byte[] msg = ("SFIDADA:"+nickUtente+":").getBytes();
	       
	        DatagramPacket dp= new DatagramPacket(msg,msg.length,sa);
	       
	        //timeout di accetazione 3sec
	        ds.setSoTimeout(3000);
	        
	        //invio e ricezione tramite datagram socket
	        ds.send(dp);
	        System.out.println("______   ***   Invio del datagram packet   ***   _____");
	       
	        ds.receive(dp);
	        System.out.println("_______   ***   Ricezione del datagram packet   ***   ______");
	          
	        //contiene "ACCETTATA" o "RIFIUTATA"
	        data = new String(dp.getData());
	        
	        //se avversario invia ACCETTATA
	        if(data.contains("ACCETTATA")) { 
	        	resp=6; 
	        	
	        	//cambiano stato e sono in sfida, cosi non riceveranno altre richieste di sfide
	        	users.get(nickAmico).set_InMatch();
	        	users.get(nickUtente).set_InMatch();
	        }
	        //altrimenti "RIFIUTATA"
	        else 
	        	resp=-12; 
	        
	        //chiudo datagram socket  
	        ds.close();
	    } 
		//TEMPO SCADUTO->ECCEZIONE
	    catch(SocketTimeoutException e) {System.out.println("____  Timeout scaduto  ____");resp=-13;  }
	    catch (IOException ex)  { key_avvers.cancel(); }
	    
	    
		
		return resp;
   }
	
	
	//__________  *** CREA SFIDA  ***  __________//
	
	/*
	 * Dedica un thread del threadpool per la sfida tra due giocatori, specificando le keys per le comunicazioni TCP delle parole
	 * 
	 */
	
	public void CreaSfida(String u1,String u2,Selector selector){
    
		System.out.println("\n_________ CREA SFIDA  ________\n");
      
        SelectionKey k1=Keys.get(u1);
        SelectionKey k2=Keys.get(u2);
        System.out.println("k1 e k2: "+k1 +" "+k2);
        
        //non imposto inMatch perchè li ho settati non appena l'avversario accetta, cosi non ricevono piu richieste da quel momento.
        //potrebbe succedere che nell'intervallo da quando accetta a quando creo il thread arrivi una richiesta.
		//users.get(u1).set_InMatch();
		//users.get(u2).set_InMatch();
		
        //creo un'istanza del thread
        ThreadSfida thsfida = new ThreadSfida(this,u1,u2,k1,k2,selector);
        
        //sottometto il thread e avvio la sfida tra i due giocatori
        ThreadpoolSfide.execute(thsfida);
        
        System.out.println("\n_________ FINE CREAZIONE THREAD SFIDA  ________\n");
    }
		
	
}
