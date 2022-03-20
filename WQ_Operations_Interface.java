
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;


/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */

//operazioni permesse al client,implementate in WQ_Server.
public interface WQ_Operations_Interface {
	
	 	public void RegWQ_Service() throws RemoteException,IllegalArgumentException;
	
	    public int Login(SelectionKey key,String nickUtente,String password);
	    
	    public int Logout(String nickUtente)throws IOException;
	    
	    public int Aggiungi_Amico(String Nickutente, String Nickamico);
	    
	    public String Lista_amici(String nickUtente) throws FileNotFoundException, IOException, ParseException ;
	    
	    public int Sfida(String nickUtente,String nickAmico);
	    
	    public long Mostra_Punteggio(String nickUtente);
	    
	    public String Mostra_Classifica(String nickUtente);   
	    
	    public int UDP_msg(String nickUtente,String nickAmico);
	    
	    public void CreaSfida(String u1,String u2,Selector selector);
	    
	    public LinkedHashMap<String, Long> SortedHashMap(HashMap<String, Long> classifica);
	    
	    public void checkFile () ;
}

