
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import static java.lang.Thread.sleep;

/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */

//implementa il task del thread che si occupa della sfida tra due utenti
public class ThreadSfida implements Runnable {
	
	
	
	//_________*** DICHIARAZIONI VARIABILI***_________//
	
	//WQ_Server che crea il thread
	WQ_Server WQ;
	
	//punteggi
	long p1;
	long p2;
	
	//per selector
	SelectionKey k1;
	SelectionKey k2;
	String u1;
	String u2;
	Selector old_selector;
	
	//arrayList di parole scelte
	ArrayList<String> scelte;
	//hashmap di 5 posizioni con associazione <parola italiana, traduzione inglese>
  	HashMap <String,String> ItaEn;
  	//dizionario di 400 parole italiane
  	ArrayList<String> dizionario;
  
	//risultato della lettura sul socket
	int res;
	//numero di parole da tradurre: 5
	int K;
	//inviata al client se traduzione errata
	String trad_corretta;
	
	//contatore di parole
	int count1;
	int count2;
	
	int corrette1;
	int corrette2;
	
	int nonrisposte1;
	int nonrisposte2;
	
	//risultati finali
	String risult;
	//risultati a chi abbandona
	String abbandonato;
	//per sapere se entrambi hanno finito
	int fine;
	
	//per capire chi finisce prima :end è il primo che finisce
	boolean end2;
	
			
	//=2 se ho inviato a entrambi i risultati e termino,=1 se ho inviato a uno solo
	int stopresult;

	//stringacon i risultati dopo FINE PARTITA
	String risulta;
	
	String first;
	
	//per ricordare la chiave del giocatore che finisce per primo, per inviargli i risultati
	SelectionKey firstfinishkey=null;
	SelectionKey secondfinishkey=null;
	//per ricordare la chiave del giocatore che abbandona la partita
	SelectionKey firstabbkey=null;
	SelectionKey secondabbkey=null;
	//per ricordare la chiave del giocatore che finisce con timeout
	SelectionKey firsttimekey=null;
	SelectionKey secondtimekey=null;
	

	//per ricordare il socket di chi  finisce per primo
	SocketChannel firstfinishsock=null;
	//del secondo che finisce
	SocketChannel secondfinishsock=null;	
	//per ricordare il socket di chi abbandona
	SocketChannel firstabbsock=null;
	//del secondo che vince automatiamente
	SocketChannel secondabbsock=null;
	//per ricordare il socket di chi  finisce per primo per timeout
	SocketChannel firsttimesock=null;
	//del secondo che finisce con timeout
	SocketChannel secondtimesock=null;	
	
	
	//per ricordare i nomi di chi abbandona o finisce per primo
	String firstfinishname=null;
	String firstabbname=null;
	String firsttimename=null;
	String secondfinishname=null;
	String secondtimename=null;
	String secondabbname=null;
	
	//per la terminazione
	boolean firstfinish=false;
	boolean firsttime=false;
	boolean secondfinish=false;
	boolean secondtime=false;
	boolean firstabb=false;
	boolean secondabb=false;
	
	//contiene il vincitore per abbandono
	String win=null;
	
	//per il selector in cui scrive la stringa di buf[1]
	String msg=null;
	
	//per aspettare che l'avversario finisca
	boolean aspetta=false;
	
	//chiusura anomala
	boolean end=false;
	
	
	//_______***  costruttore  ***________//
	
	public ThreadSfida(WQ_Server WQ,String u1,String u2,SelectionKey k1, SelectionKey k2,Selector selector){
			
		this.WQ=WQ;
		
		this.ItaEn= new HashMap <String,String>();
		this.scelte=new ArrayList<String>();
		this.dizionario=new ArrayList<String>();
		
		
		//per svegliarlo una volta finita la sfida in modo che riveda le chiavi dei due giocatori
		this.old_selector=selector;
		//attributi dei due client:nome,chiave,punteggio
		this.k1=k1;
		this.k2=k2;
		this.u1=u1;
		this.u2=u2;
		this.p1=0;
		this.p2=0;
		
		this.K=5;
		//stringa che contiene la traduzione corretta da far visualizzare al client 
		this.trad_corretta=null;
		//per contare le parole tradotte dal client
		this.count1=0;
		this.count1=0;
		//per contare le parole tradotte correttamente
		this.corrette1=0;
		this.corrette2=0;
		//per contare le parole rimaste da tradurre
		this.nonrisposte1=0;
		this.nonrisposte2=0;
		//stringa che contiene la stringa dei risultati finali
		this.risult=null;
		//se =1 allora primo giocatore ha finito, se =2 hanno finito entrambi
		this.fine=0;
		
		//end2==true se hanno finito entrambi e finisce il thread
		this.end2=false;
		
		//per terminare. setta end2=true
		this.stopresult=0;
		this.first=null;
		
		
	
	}
	
	//_______  ***   task del thread  ***  _____//
	/*
	 * Crea un dizionario di parole italiane, ne sceglie 5 per la sfida in corso e le salva in una hashtable con le rispettive traduzioni
	 * - invia e riceve le parole tramite un selector per entrambi i giocatori nella partita. 
	 * - invia gli esiti di ogni parola per far sapere al client se la traduzione era esatta o meno(nel caso di risposta errata invia anche la traduzione corretta)
	 * - invia i risultati finali con il punteggio del vincitore e dello sconfitto.
	 */
	@Override
	public void run() {
		
			
		
			System.out.println("\n______________ ***   Inizia la sfida tra "+u1+" e "+u2+"   ***   _____________\n");
			
			//scelgo le parole da inviare ai giocatori con le rispettive traduzioni
			CreaDizionario(dizionario);
			
			ParoleRandom(dizionario,5);
			
			RiempiHashTrad(ItaEn,scelte,5);
	
			try {
				
				Selector new_selector=Selector.open();
				
				//ottengo i due canali dei giocatori tramite le chiavi
				SocketChannel channel1=(SocketChannel)k1.channel();
				SocketChannel channel2=(SocketChannel)k2.channel();
			
				//creo attachment con lunghezza e messaggio
				ByteBuffer[] attachments= new ByteBuffer[2];
				attachments[0]=ByteBuffer.allocate(Integer.BYTES);
				attachments[1]=ByteBuffer.allocate(1024);
				
				//in lettura
				channel1.register(new_selector, SelectionKey.OP_READ,attachments);
				channel2.register(new_selector, SelectionKey.OP_READ,attachments);
			
				//serve peril recupero, se uno dei due termina in modo anomalo 
				SelectionKey[] Chiavi=new SelectionKey[2];
				Chiavi[0]=null;
				Chiavi[1]=null;
				
				String[] Nomi= new String[2];
				Nomi[0]=null;
				Nomi[1]=null;
				//
			
				while(!end2) {
					
					//si blocca finchè non ci sono canali pronti
					if(new_selector.select()>0 ) {
						
						//insieme di chiavi: ognuna mantiene riferimento al proprio canale e lo stato del canale
						Set<SelectionKey> keys= new_selector.selectedKeys();
						
						
						
						Iterator<SelectionKey> iterator = keys.iterator();
						
						
						SocketChannel sock0=null;
						SocketChannel sock1=null;
							
							while(iterator.hasNext() ) { 
								
								
								//seleziona una key(sicuramente rappresenta un canale pronto a fare qualcosa)e poi: key.channel(), key.attachment(), key.isReadble()...
								SelectionKey key = iterator.next(); 
								
								
								//utile per il recupero in fase di disconnessione anomala di uno dei due giocatori
								//salvo in 0 il primo giocatore che inizia
								if(Chiavi[0]==null) {  Chiavi[0]=key;sock0=(SocketChannel)Chiavi[0].channel(); }
								else if(Chiavi[1]==null && key!= Chiavi[0]) {Chiavi[1]=key;sock1=(SocketChannel)Chiavi[1].channel(); }
								
								//rimuovo la chiave dal set dopo averla selezionata
								iterator.remove();
								
								
								
								//terminazione per abbandono: l'altro ha già abbandonato, quindi termino anch'io 
								if(firstabbname!=null && key!=firstabbkey) {
									
									System.out.println("SONO IL SECONDO CHE FINISCE PERCHè L'ALTRO HA ABBANDONATO");
									ByteBuffer length= ByteBuffer.allocate(Integer.BYTES); 
									ByteBuffer message= ByteBuffer.allocate(1024);
									ByteBuffer[] msg = {length, message};
									secondabbkey=key;
									
									secondabbsock=(SocketChannel)secondabbkey.channel();
									secondabbsock.register(new_selector, SelectionKey.OP_WRITE,msg);
									
									//capisco chi è l'utente che ha vinto automaticamente per aggiornare la tabella hash
									if(firstabbname.equals(u2))
										win=AssegnaPuntiAbbandona(u1);
										
									else
										win=AssegnaPuntiAbbandona(u2);
									
									//invio la stringa che ritorna AssegnaPuntiAbbandona()
									res=1;
								
								}
	
								
								
								if(key.isReadable() ) {
									
									try {
										
										
										
										//socket channel del giocatore
										SocketChannel sc= (SocketChannel) key.channel();
										//imposto modalià non bloccante, altrimenti sarebbe bloccante di default
										sc.configureBlocking(false);
										System.out.println("\nkey leggibile: "+key);
										
										//in caso di chiusura anomala
										if(end==true) {
											
											//msg="OKDISCONNECT";
											sc.register(new_selector, SelectionKey.OP_WRITE, msg);
											System.out.println("  ***    Terminazione della sfida per disconnessione improvvisa  ***  \n");
											res=9;
											end=false;
											break;
										}	
										
									    ByteBuffer[] buf = (ByteBuffer[]) key.attachment();
									    buf[0].clear();
									    buf[1].clear();
									    sc.read(buf);
									    
									    
									    //se ho letto tutto
									    if (!buf[0].hasRemaining()){ 
									    	
									    	//leggo la lunghezza
									    	buf[0].flip();
									    	int len= buf[0].getInt();
									    	
									    	if(buf[1].position()<len)len=buf[1].position();
									    	//controllo che nel buffer position=l (la stringa è stata letta tutta) altrimenti INVIADINUOVO
									    	if(buf[1].position()==len ){ 
									    		
									    		//leggo messaggio
									    		buf[1].flip(); 
									    		msg = new String(buf[1].array()).trim();
									    		
									    		System.out.println("\n***  Stringa ricevuta dal client in sfida:\n " + msg+" \nl(unghezza: "+msg.length() +")  *** \n\n");
									    		
									    		String[] tokens = msg.split(" ");
									    		
									    		switch(tokens[0]) {
									    		
									    		
									    		case "INIZIASFIDA":
									    			
									    			res=0;
									    			break;
									    		
									    		case "ABBANDONA":
									    			
									    			//clickkano consecutivamente abbandona
									    			if(secondabbkey!=null) {
									    				break;
									    			}
									    			
									    			//sono il primo a finire perchè abbandono -> faccio terminare anche il secondo
									    			if(firstfinishkey==null && firsttimekey==null) {
									    				
									    				System.out.println("\nSONO IL PRIMO CHE FINISCE (PER ABBANDONO): key ->"+key+"\n");
									    				
									    				firstabbkey=key;
									    				firstabbname=tokens[1];
									    				firstabbsock=(SocketChannel)firstabbkey.channel();
									    				abbandonato=Abbandona(firstabbname);
									    				
									    				
									    			}
									    			
									    			else {
									    				
									    				System.out.println("\nSONO IL SECONDO CHE FINICE (PER ABBANDONO): key ->"+key+"\n");
									 	
									    				secondabbkey=key;
									    				secondabbname=tokens[1];
									    				secondabbsock=(SocketChannel)secondabbkey.channel();
										    			//l'altro ha finito di giocare con fine partita
										    			if(firstfinishkey!=null) {
										    				
										    				System.out.println("\n IL PRIMO AVEVA FINITO CON FINE PARTITA : key ->"+key+"\n");
										    				abbandonato=Abbandona(secondabbname);
										    				//per mandare i risultati anche all'avversario
										    				firstfinishsock=(SocketChannel)firstfinishkey.channel();
										    				firstfinishsock.register(new_selector, SelectionKey.OP_WRITE,msg);
										    				
										    			}
										    			
										    			//l'altro ha finito di giocare per timeout
										    			else if(firsttimekey!=null) {
										    				
										    				System.out.println("\n IL PRIMO AVEVA FINITO CON TIMEOUT : key ->"+key+"\n");
										    				abbandonato=Abbandona(secondabbname);
										    				//per mandare i risultati anche all'avversario
										    				firsttimesock=(SocketChannel)firsttimekey.channel();
										    				firsttimesock.register(new_selector, SelectionKey.OP_WRITE,msg);
										    				
										    			}
										    			
										    			else if(firstabbkey!=null) {
										    				
										    				//il primo ha abbandonato esattamente nel mio stesso momento
										    				secondabb=true;
										    				
										    			}
										    			
										    			
									    			}
									    			
									    			res=1;
									    	
									    			break;
									    			
									    		case "PROSSIMAPAROLA":
									    			//ad ogni click su "prossima parola"
									    			if(u1.equals(tokens[1])) {  res=2;}
													else { res=3;}
									    			//invia prossima parola
									    			break;
									    		
									    		case "TRADUZIONE":
									    			//al click su "verifica traduzione"
									    			
									    			//aggiornamento contatori delle parole
									    			
									    			//se è il primo giocatore (sfidante)
									    			if(tokens[1].contains(u1)) { 
									    				count1++; 
									    				System.out.println("Numero della parola tradotta da u1:\n "+u1+" --> "+count1);
									    			}
									    			//se è il secondo giocatore (sfidato) 
									    			else {
									    				
									    				count2++; 
									    				System.out.println("Numero della parola tradotta da u2:\n "+u2+" --> "+count2);
									    			}
									    			
									    			//verifico se la traduzione inviata è corretta
									    			
									    			trad_corretta = ControllaTrad(tokens[2],tokens[3]);
									    			
									    			//se è corretta
									    			if(trad_corretta.equals("ok")) { 
									    				
									    				if(tokens[1].contains(u1)) {corrette1++; }
														else {corrette2++;}
									    				
									    				res=4;
									    			}
									    			
									    			//se è sbagliata
									    			else res=5;
									    			
									    			break;
									    		
									    		case "OKWAIT1":
									    			
									    			//per timeout o per fine normale
									    			aspetta=true;
									    			
									    			break;
									  
									    	
									    		case "FINEPARTITA":
									    			
									    			//sono il primo a finire(fine normalecon FINE PARTITA)
									    			if(firstfinishkey==null && firsttimekey==null) {
									    				
									    				System.out.println("\nSONO IL PRIMO CHE FINICE (PER FINEPARTITA): key ->"+key+"\n");
									    				
									    				firstfinishkey=key;
									    				firstfinishname=tokens[1];
									    				
									    				risulta="WAIT";
									    				res=6;
									    			
									    			}
									    			
									    			//l'altro ha già finito
									    			else {
	
									    				secondfinishkey=key;
									    				secondfinishname=tokens[1];
									    				System.out.println("\nSONO IL SECONDO CHE FINICE (PER TIMEOUT): key ->"+key+"\n");
									    				
									    				//il primo ha finito normalmente, anch'io finisco normalemnte
									    				if(firstfinishkey!=null){
									    					
									    					System.out.println("\nIL PRIMO HA FINITO NORMALMENTE\n");
	
										    				
										    				//per mandare i risultati anche all'avversario
										    				firstfinishsock=(SocketChannel)firstfinishkey.channel();
										    				firstfinishsock.register(new_selector, SelectionKey.OP_WRITE,msg);
										    	
										    				
										    			}
									    				//il primo ha finito con TIMEOUT
									    				else if(firsttimekey!=null) {
	
									    					System.out.println(" \nIL SECONDO HA FINITO PER TIMEOUT\n\n");
									    					
									    					
										    				//per mandare i risultati anche all'avversario
										    				firsttimesock=(SocketChannel)firsttimekey.channel();
										    				firsttimesock.register(new_selector, SelectionKey.OP_WRITE,msg);
										    			
									    				
									    				}
	
								    					//calcolo punteggi
									    				risulta=Esito();
									    				res=7;
									    				
									    			}
									    			break;
									    			
									    		
									    			
										    		case "TIMEOUT":
										    			
										    			
										    			//sono il primo a finire(fine con TIMEOUT)
										    			if(firstfinishkey==null && firstabbkey==null && firsttimekey==null) {
										    				
										    				System.out.println("\nSONO IL PRIMO CHE FINICE (PER TIMEOUT) "+key+"\n");
										    				
										    				firsttimekey=key;
										    				firsttimename=tokens[1];
										    				
										    				risulta="WAIT";
										    				res=6;
										    				
										    				//torno scrivibile normalmente
										    				
										    				
										    				
										    			}
										    			//l'altro ha già finito
										    			else {
										    				
										    				System.out.println("\nSONO IL SECONDO CHE FINICE (PER TIMEOUT)"+key+"\n");
	
										    				secondtimekey=key;
										    				secondtimename=tokens[1];
										    				
										    				
										    				//il primo ha finito normalmente, io finisco con timeout
										    				if(firstfinishkey!=null){
										    					
										    					System.out.println("\nIL PRIMO HA FINITO NORMALMENTE\n");
	
											    				
											    				//per mandare i risultati anche all'avversario
											    				firstfinishsock=(SocketChannel)firstfinishkey.channel();
											    				firstfinishsock.register(new_selector, SelectionKey.OP_WRITE,msg);
											    	
											    				
											    			}
										    				//il primo ha finito con TIMEOUT, anch'io finisco così
										    				else if(firsttimekey!=null) {
	
										    					System.out.println(" \nIL PRIMO HA FINITO PER TIMEOUT\n\n");
										    					
										    					
											    				//per mandare i risultati anche all'avversario
											    				firsttimesock=(SocketChannel)firsttimekey.channel();
											    				firsttimesock.register(new_selector, SelectionKey.OP_WRITE,msg);
											    			
										    				
										    				}
	
									    					//calcolo punteggi
										    				risulta=Esito();
										    				res=8;
										    			}
									    				
									    			break;
									    			
									    			
									    			
									    			default:
									    				break;
									    		}
									    		
									    		
									    		if(aspetta==true && (firstfinishkey==key || firsttimekey==key )) {
									    			
									    			
									    			//ho finito per timeout o per fine partita, ora aspetto che l'altro giocatore finisca e mi sblocchi
										    			sc.register(new_selector, 0);
										    		
									    		}
										    	//se sono chi ancora deve giocare
										    	else {
										    		sc.register(new_selector, SelectionKey.OP_WRITE, msg);
										   		}
									    		
									    		
									    	}
									    	
									    }else if(end!=true){
									    	System.out.println("Errore. Disconnessione client non aspettata.");
										    
									    	end=true; 
									    	

											//chiudo chi si è disconnesso
										    key.cancel();
										    key.channel().close();
										    break;
									    }
									}  
									
									catch (IOException e){
										System.out.println("Error reading from client ");
										key.cancel();key.channel().close();
									}
								}
							
								
								//soketchannel scrivibile
								else if (key.isWritable()){ 
										SocketChannel client= (SocketChannel) key.channel();
										System.out.println("\nkey scrivibile: "+key);
										
										//per convertire la stringa e inviarla
										ByteBuffer resp= ByteBuffer.allocate(256);
										String s=null;
										resp.clear();
										
										switch (res) {
											case 0:
													//prima parola inviata
												s="INIZIATA:"+scelte.get(0);
												break;
											case 1:
												
												
												//sono chi ha abbandonato. O prima che l'altro finisca (first)o dopo(second).
												if(key==firstabbkey) {
													
													System.out.println("\nCase firstabb\n");
													firstabb=true;
													//prende 0 punti
													s=abbandonato;
													//mi fa terminare il secondo
													firstabbsock.register(new_selector,0);
													
												}
												
												
												//chi ha vinto perchè il primo ha abbandonato, oppure chiha vinto perchè aveva finito prima che il secondo abbandonasse
												else if((key==secondabbkey && firstabbkey!=null) || firsttimekey==key || firstfinishkey==key) {
													
													System.out.println("\nCase secondabb\n");
													secondabb=true;
													if(key==secondabbkey && firstabbkey!=null) {
														
														s=win;
														System.out.println("stringa1:"+s);
														
													}
													else if(firsttimekey==key) {
														firsttime=true;
														s=AssegnaPuntiAbbandona(firsttimename);
														System.out.println("stringa2:"+s);
													}
													else if(firstfinishkey==key) {
														firstfinish=true;
														s=AssegnaPuntiAbbandona(firstfinishname);
														System.out.println("stringa3:"+s);
													}
												}
												
												
												else if(key==secondabbkey && firstabbkey==null) {
													secondabb=true;
													s=abbandonato;
												}
											
												
											
												break;
												
											case 2: 
												//prossima parola per u1
												System.out.println("Parola inviata a u1   --->  "+scelte.get(count1));
												s="PAROLA:"+scelte.get(count1);
												break;
											
											case 3: 
												//prossima parola per u2
												System.out.println("Parola inviata a u2   --->  "+scelte.get(count2));
												s="PAROLA:"+scelte.get(count2);
												break;
											
											case 4:
												//esito positivo della traduzione
												s="CORRETTA";
												break;
											case 5:
												//esito negativo con traduzione corretta
												s="ERRATA:"+trad_corretta;
												break;
											case 6:
												
												s="WAIT";
												break;
												
											case 7:
												System.out.println("\nCase 7\n");
												s=risult;
												
												
												if(firstfinishkey==key ) {
													System.out.println("\nCase firstfinish\n");
													firstfinish=true;
													//risultati finali
													//firstfinishsock.register(new_selector,0);
													
												}
												
												else if(firsttimekey==key ) {
													System.out.println("\nCase firsttime\n");
													firsttime=true;
													//firsttimesock=(SocketChannel)firsttimekey.channel();
													//firsttimesock.register(new_selector,0);
													
												}
												else if(secondfinishkey==key){
													System.out.println("\nCase secondfinish\n");
													secondfinish=true;
													secondfinishsock=(SocketChannel)secondfinishkey.channel();
													//secondfinishsock.register(new_selector,0);
												
												
											}
											
												
												break;
											case 8: 
												
												s=risult;
												
												
												if(firsttimekey==key ) {
													System.out.println("\nCase firsttime\n");
													firsttime=true;
													//firsttimesock=(SocketChannel)firsttimekey.channel();
													//firsttimesock.register(new_selector,0);
													
												}
												
												else if( firstfinishkey==key) {
													System.out.println("\nCase firstfinish\n");
													firstfinish=true;
												}
												
												else if(secondtimekey==key){
													System.out.println("\nCase secondtime\n");
													secondtime=true;
													secondtimesock=(SocketChannel)secondtimekey.channel();
													//secondtimesock.register(new_selector,0);
												}
												
												
												
											break;
											case 9:
												 s="DISCONNECT";
												
											
												resp = ByteBuffer.wrap((s.getBytes()));
										    	
											    
											   client.write(resp);
											    
											    if (!resp.hasRemaining() ){
													// Non c'è più da scrivere
											    	
													ByteBuffer length= ByteBuffer.allocate(Integer.BYTES); 
													ByteBuffer message= ByteBuffer.allocate(1024);
													ByteBuffer[] bbf = {length, message};
												
													message.put(new byte[1024]);
													message.clear();
													client.register(new_selector, 0);
													client.register(old_selector, SelectionKey.OP_READ, bbf);
													
											    }
												end2=true;
												
												break;
											default:
												break;
										}
										
										
										System.out.println(" *** Risposta inviata al client nel thread sfida : "+s+" ***   \n");
							    		
										if(res!=9) {
										   resp = ByteBuffer.wrap((s.getBytes()));
										   
										    client.write(resp);
										   
										    
										    if (!resp.hasRemaining() ){
												// Non c'è più da scrivere
												resp.clear();
												ByteBuffer length= ByteBuffer.allocate(Integer.BYTES); 
												ByteBuffer message= ByteBuffer.allocate(1024);
												ByteBuffer[] bbf = {length, message};
												
												
												//CASI TERMINAZIONE
												if(firstabbkey==null && secondabbkey==null) {
													//hanno finito entrambi normalmente
													if(firstfinish==true && secondfinish==true ) {
														
														System.out.println("FIRST FINISH E SECOND FINISH");
														firstfinishsock.register(old_selector,SelectionKey.OP_READ,bbf);
														firstfinishsock.register(new_selector, 0,bbf);
														secondfinishsock.register(old_selector,SelectionKey.OP_READ,bbf);
														secondfinishsock.register(new_selector,0,bbf);
														end2=true;
													}
													
													//hanno finito entrambi con timeout
													else if(firsttime==true && secondtime==true ) {
														System.out.println("FIRST TIME E SECOND TIME");
														firsttimesock.register(old_selector,SelectionKey.OP_READ,bbf);
														firsttimesock.register(new_selector, 0,bbf);
														secondtimesock.register(old_selector,SelectionKey.OP_READ,bbf);
														secondtimesock.register(new_selector,0,bbf);
														end2=true;
													}
													
													
													//primo: timeout secondo:fine partita
													else if(firsttime==true && secondfinish==true) {
														System.out.println("FIRST TIME E SECOND FINISH");
														firsttimesock.register(old_selector,SelectionKey.OP_READ,bbf);
														firsttimesock.register(new_selector, 0,bbf);
														secondfinishsock.register(old_selector,SelectionKey.OP_READ,bbf);
														secondfinishsock.register(new_selector,0,bbf);
														end2=true;
														
													}
													
													else if(firstfinish==true && secondtime==true ) {
														System.out.println("FIRST FINISH E SECOND TIME");
														firstfinishsock.register(old_selector,SelectionKey.OP_READ,bbf);
														firstfinishsock.register(new_selector, 0,bbf);
														secondtimesock.register(old_selector,SelectionKey.OP_READ,bbf);
														secondtimesock.register(new_selector,0,bbf);
														end2=true;
													}
													
													//il secondo non ha ancora finito, allora aspetto che sblocchi entrambi e termini
													else if( secondtime!=true && secondfinish!=true && (firstfinish==true  || firsttime==true ) ){
														
														
														
														//sono il primo che ha finito normalmente, ma il secondo ancora deve passare di qui
														if(firstfinish==true ) {
															System.out.println("FIRST FINISH E SONO IL PRIMO A PASSARE DI QUI");
															firstfinishsock.register(new_selector, 0,bbf);
														}
														//sono il primo che ha finito con timeout, ma il secondo ancora deve passare di qui
														else if(firsttime==true) {
															System.out.println("FIRST TIME E SONO IL PRIMO A PASSARE DI QUI");
															firsttimesock.register(new_selector, 0,bbf);
														}
														//sono il primo che ha abbandonato, ma il secondo ancora deve passare di qui
														else {
															
														}
													}
													//il primo non ha ancora finito, allora aspetto e mi sbloccherà lui
													else if(firstfinish!=true && firsttime!=true && (secondtime==true || secondfinish==true)) {
														System.out.println("FIRST NON HA FINITO E SONO IL PRIMO A PASSARE DI QUI");
														
													
														if(secondfinish==true) {
															
														}
														
														else if(secondtime!=true) {
															
															
														}
														
														
													}
													
													//normalità
													else {
														
														client.register(new_selector, SelectionKey.OP_READ,bbf);
													}
												}
												
												
												//abbandono
												else {
													//il primo ha abbandonato , il secondo ha finito di conseguenza
													if(firstabb==true && secondabb==true) {
														
														
														System.out.println("FIRST ABB E SECOND ABB");
														firstabbsock.register(old_selector,SelectionKey.OP_READ,bbf);
														firstabbsock.register(new_selector, 0,bbf);
														secondabbsock.register(old_selector,SelectionKey.OP_READ,bbf);
														secondabbsock.register(new_selector,0,bbf);
														end2=true;
														
														
													}
													else if(secondabb==true && firsttimekey!=null && firsttime==true) {
	
														System.out.println("FIRST TIME E SECOND ABB");
														firsttimesock.register(old_selector,SelectionKey.OP_READ,bbf);
														firsttimesock.register(new_selector, 0,bbf);
														secondabbsock.register(old_selector,SelectionKey.OP_READ,bbf);
														secondabbsock.register(new_selector,0,bbf);
														end2=true;
													}
													else if(secondabb==true && firstfinishkey!=null && firstfinish==true) {
														System.out.println("FIRST FINISH E SECOND ABB");
														firstfinishsock.register(old_selector,SelectionKey.OP_READ,bbf);
														firstfinishsock.register(new_selector, 0,bbf);
														secondabbsock.register(old_selector,SelectionKey.OP_READ,bbf);
														secondabbsock.register(new_selector,0,bbf);
														end2=true;
													}
													
													
													
													
												}
												
											
												
										 }  		
										}	    
							
								}
									
										    
								  } 
							} 	
						
						
					}   
					sleep(1000);
					System.out.println("  ***    Terminazione della sfida   ***  \n");
					old_selector.wakeup();
					new_selector.close();
				      
				}
				catch (IOException e){k1.cancel();k2.cancel();end2=true; System.out.println("Error writing to client: "+ e.getMessage());} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			
				   
	}
	
	//_________   *** SCEGLIE K PAROLE RANDOM DAL DIZIONARIO ***  _______//

	public ArrayList<String> ParoleRandom(ArrayList<String> dizionario, int K){
		
		System.out.println("\n_______  ***  LE 5 PAROLE SCELTE RANDOM DAL DIZIONARIO  ***  ______\n");
		String p=null;
		int i=0;
		
		
		//permutazione casuale dell'array
       Collections.shuffle(dizionario);
       
       
        for(i=0; i<K; i++) {
        	//ottengo la parola dall'array di parole precedentemente scelte per la sfida
        	p =  dizionario.get(i);
        	System.out.println(p);
        	scelte.add(i, p);
        	
        }
		return scelte;
        
	}
	
	
	//_________  *** CREA DIZIONARIO (ArrayList<String> itwords)***  ___________//
	
	
	//crea un dizionario leggendo dal file e copiando le parole nell'array itwords
	public void CreaDizionario(ArrayList<String> itwords) {
		
		//FileReader per leggere le parole dal file e scriverle in ArrayList words
		FileReader fr=null;
		//parola estratta
		String word=null;
		
		
		//creo un oggetto File di parole italiane una per riga
		File italianwords = new File("italianwords.txt");
		try {
			fr = new FileReader(italianwords);
		}
		catch(FileNotFoundException e) {
			System.out.println("E' probabile che il file italianwords.txt non sia nella cartella corrente. Eventualmente spostarlo e riprovare.");
		}
		
		//buffer di appoggio alla lettura
		BufferedReader br = new BufferedReader(fr);
		
		try {
			//prelevo la prima parola (prima riga)
			word=br.readLine();
			itwords.add(word);
			while(word!=null) {
				//prelevo tutte le parole e le salvo in words
				itwords.add(word);
				word=br.readLine();
				
			}
			br.close();
		}
		catch(IOException e) {System.out.println("E' probabile che il file italianwords.txt non sia nella cartella corrente. Eventualmente spostarlo e riprovare.");}
		
		
	}
	
	
	//________   *** RIEMPI HASHMAP CON ASSOCIAZIONI <ITALIANO,INGLESE> DELLE PAROLE SCELTE  ***   _______//
	
	
	public void RiempiHashTrad (HashMap<String,String> traduzioni, ArrayList<String> scelte,int K) {
		
		
		for(int i=0; i<K; i++) {
			String parola = scelte.get(i);
			//ottengo la traduzione dal servizio esterno e converto da JSON a stringa
			String trad = getTrad(parola).toLowerCase().trim().replaceAll(",", "");
			//costruisco tabella hash da usare nella sfida: <parola,traduzione>
			traduzioni.put(parola,trad);
		}
	}
	
	
	
	//______________   ***  GET TRAD: ottieni traduzione diuna parola  ***  ______________//
	
	
	//parola: UTF-8 e max 500 bytes
	public String getTrad(String parola) {
		
	  //stringa da restituire
	  String english = null;
	  //per costruire la stringa
	  StringBuilder str = new StringBuilder();
	  
	  //connessione
	  HttpURLConnection conn=null;
	  
      try {  
       
		//servizio esterno di traduzione
		String https = ("https://api.mymemory.translated.net/get?q="+parola+"&langpair=it|en");
        URL url= new URL(https);
     
        //apro la connessione
        conn = (HttpURLConnection) url.openConnection();
       
        //richiesta GET
        conn.setRequestMethod("GET");
        //InputStream classe astratta che implementa lo stream per leggere l'inputStream di conn
        InputStream is=  conn.getInputStream();	 
        //come is,ma buffering per rendere più efficienti le operazioni IO, invece di leggere un byte alla volta
        InputStreamReader inr= new InputStreamReader(is);
        BufferedReader br = new BufferedReader(inr);
        
        //recupero tramite servizio esterno traduzioni in JSON 
        String line=null;
        //creo line che contiene la riga letta dal buffer reader inJSON, restituita da GET e costruisco una stringa str con tutto ciò che leggo
        //ritorna null se non ci sono caratteri da leggere
        //"responseData":{"translatedText":"what",...}
        while ((line = br.readLine()) != null) { str.append(line);}
        //chiude anche is automaticamente
        br.close();
        conn.disconnect();
      }
      catch(IOException e) {e.printStackTrace();}
      
     
      //trasformo stringa it (parola italiana)
      String it = str.toString();
      JSON_Op j = new JSON_Op();
     
      //dal file JSON estraggo il campi traduzione e converto in stringa la traduzione inglese ottenuta
      try { 
    	  english=j.JSONTrad(it);
    	  System.out.println("Traduzione   --->  "+english);
      } 
      catch (ParseException e) {e.printStackTrace();}
      
	return english;
	}
	
	//____________  ***   Controlla traduzione  ***   _______________//
	
	
	public String ControllaTrad(String word,String parola) {
		String corretta=ItaEn.get(parola);
		//risposta esatta
		if(word!=null && word.equals(corretta)){
			return "ok";
		}
		//parola mancante o errata
		else return corretta;
	}
	
	//__________   *** fine partita ***  _____________//
	
	//Ritorna la stringa dei risultati,se entrambi finiscono con FINE PARTITA 
	public String Esito() {
			
				//risposte date-rispostecorrette=risposte sbagliate
				int errori1 = count1-corrette1;
				int errori2 =count2-corrette2;
				
				//ogni risposta esatta vale 3
				int punti_corrette1= corrette1*3;
				int punti_corrette2=corrette2*3;
				
				//ogni errore vale 1
				p1=punti_corrette1-errori1;
				p2=punti_corrette2-errori2;
			
				
				//confronto i punteggi
				if(p1 > p2 ) {
					//vittoria di u1 bonus +5
					p1=p1+5;
					risult= "vincitore:"+u1+":punteggio:"+Long.toString(p1)+":corrette:"+Integer.toString(corrette1)+":errate:"+Integer.toString(errori1)+":perdente:"+u2+":punteggio:"+Long.toString(p2)+":corrette:"+Integer.toString(corrette2)+":errate:"+Integer.toString(errori2);//VINCE U1
					//aggiorno i punteggi in tabella hash
					WQ.users.get(u1).setPunteggio(p1);
					WQ.users.get(u2).setPunteggio(p2);
					
				}
				else if(p1==p2) {
					//pareggio
					p1=p1+5;
					
					risult= "pareggio:"+Long.toString(p1)+":corrette:"+Integer.toString(corrette1)+":errate:"+Integer.toString(errori1);
					//aggiorno i punteggi in tabella hash
					WQ.users.get(u1).setPunteggio(p1);
					WQ.users.get(u2).setPunteggio(p1);
					
				}
				else {
					//vittoria u2 bouns +5
					p2=p2+5;
					risult= "vincitore:"+u2+":punteggio:"+Long.toString(p2)+":corrette:"+Integer.toString(corrette2)+":errate:"+Integer.toString(errori2)+":perdente:"+u1+":punteggio:"+Long.toString(p1)+":corrette:"+Integer.toString(corrette1)+":errate:"+Integer.toString(errori1); //VINCE U2
					//aggiorno i punteggi in tabella hash
					WQ.users.get(u1).setPunteggio(p1);
					WQ.users.get(u2).setPunteggio(p2);
					
				}
			
			return risult;
	}
	
	
	
	//nel caso in cui un giocatore abbandona, l'altro ancora sta giocando
	public String Abbandona(String name){
		
		//l'altro sta ancora giocando
		return "abbandonato:"+name;
	
	}
	
	//per entrambi i giocatori perchè assegno sempre 10 punti
	public String AssegnaPuntiAbbandona(String vincitore) {
	
			int p=10;
			
			risult= "vincitore:"+vincitore+":punteggio:"+Long.toString(p); 
		
			//aggiorno punteggi 
			if(vincitore.equals(u1)) {
				WQ.users.get(u1).setPunteggio(p);
			}
			else {
				WQ.users.get(u2).setPunteggio(p);
			}
			
			return risult;
	}
	
	
}
