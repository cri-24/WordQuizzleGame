import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static java.lang.Thread.sleep;


/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */

//Implementa un thread che è in ascolto di richieste di sfida UDP da parte del server che risponde con "ACCETTA" o "RIFIUTA" tramite UDP
public class ClientUDP implements Runnable{
		
		//datagram socket tramite cui trasmetto DatagramPacket
		DatagramSocket ds;
		//per terminare il task
		boolean stop=false;
		//se tutti i pacchetti sono stati ricevuti vale true
		boolean receivedAll;
		//array per contenere il messaggio da inglobare nel packet
		byte[] msg ;
		//istanza della classe che rappresenta la sfida 
		Game_GUI GameGUI;
		
		String myname=null;
		
		//per non ricevere richieste mentre si è impegnati a gestirne un'altra
		boolean busy;
		
		//costruttore usato in Login di Game_GUI
		public ClientUDP(Game_GUI GameGUI,DatagramSocket ds,String myname) {
			this.GameGUI=GameGUI;
			this.ds=ds;
			this.msg=new byte [1024];
			this.myname=myname;
		}
		
		
		//run
		public void run()  {
			
			WaitRequestUDP(); 
		}
		
		
		//attesa di richieste UDP
		public void WaitRequestUDP() { 
			
			 busy=false;
			String data;
			
			while(!stop) {
				//riceve pacchetti solo se l'utente non è già in un'altra sfida 
				try {
					
					if(!busy) {
						
						//
						DatagramPacket dp = new DatagramPacket(msg,1024);
						
							//ricevo pacchetto sul datagram socket associato al client inizializzato al login e scrivo in dp
							ds.receive(dp);
							
								//ottengo i dati scritti in dp dal server e li converto in stringa
								data = new String(dp.getData());
								
		                    
								//se il pacchetto UDP è valido -> contiene SFIDADA:<nome>
								if(data.contains("SFIDADA")) {
			                    	
			                    	//non riceve altri pacchetti da altri utenti
			                    	StopAscolti();
			                    	
			                    	//tokenizzo il pacchetto ricevuto
			                    	String[] tokens = data.split(":");
			                    	
			                    	//chi manda la sfida
			                    	String username = tokens[1]; 
			                    	
			                    	
			                    	//cambio interfaccia grafica allo sfidato con richiesta accetta o rifiuta
			                    	GameGUI.UDPRequest(username,dp);
			                    	
			                    	//sleep 3 secondi poi scompare avviso
			                    	sleep(3000);
			                    	
			                    	//scompare avviso
			                    	GameGUI.TimeoutAccept();
			                    	
			                    	//ricevo di nuovo gli scolti, finchè non viene accettata la sfida 
			                    	//riattivati alla fine quando client riceve OKFINESFIDA
			                    	Riattiva();
			                    	
								}
								else {
									System.out.println("Richiesta non valida");
									return;
								}
							
						
					 }
				}
				catch(Exception e) {System.out.println("Chiuso listener udp");stop=true; return;}
				
			}
		}
		
		//quando disconnetto il client
		public void TerminaUDP() {
			stop=true;
		}
		
		//termina quando accetta o rifiuta la sfida
		public void StopAscolti() {
			
			busy=true;
		}
		
		
		public void Riattiva() {
			
			busy=false;
		}
}
