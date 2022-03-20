import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */

//classe che implementai metodi per inviare e ricevere messaggi TCP dal server
public class MsgTCP {
	
	
	SocketChannel client=null;
	
	//costruttore dell'istanza dell'oggetto che rappresentsa la comunicazione TCP
	
	public MsgTCP(SocketChannel client) {
		
		this.client=client;
	}
	
	
	
	//___________ ***   Invio lunghezza e richiesta destinati al server   ***  ____________//
	
	
	//invia lunghezza e messaggio al server
	public void send_msg(String msg) throws IOException {
		
	
			
			//buf per inviare la richiesta sul socket
			ByteBuffer buf = ByteBuffer.allocate(1024);
			buf.put(new byte[1024]);
			
			
			
			//calcolo la lunghezza delle stringhe "operazione+parametri"
			Integer len = msg.length();
			//trasformo la lunghezza della stringa sottoforma di byte
			ByteBuffer length = ByteBuffer.allocate(Integer.BYTES);
		    length.putInt(len);
		    //sposto puntatore all'inizio della stringa per modalità scrittura
		    length.flip();
		    
		    //scrivo la lunghezza del messaggio sul socketChannel
		    client.write(length);
		    length.clear();
		   
		    //invio la stringa vera e propria scrivendo in byte buffer con wrap che prende un array di byte
		    buf = ByteBuffer.wrap(msg.getBytes());
		    
		    //scrivo sul socket channel 
		    client.write(buf); 
		    
		  
		    
		    buf.clear();
		   
		    
		
	}
	
	
	//___________ ***   Ricezione messaggio inviato dal server   ***  ____________//
	
	//ritorna la stringa inviata dal server
	public String receive_msg()  {
		
		ByteBuffer reply = ByteBuffer.allocate(1024);
		String resp = null;
	
		try {
			reply.clear();
			

			client.read(reply);
			resp = new String (reply.array()).trim();
		}
		catch (IOException e) {e.printStackTrace();}
		
		return resp;
	}
}
