import java.rmi.RemoteException;
import java.rmi.Remote;


/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */

//interfaccia implementata da Register per permettere meccanismo RMI
public interface RegisterService extends Remote{
	
	public int registra_utente(String nickUtente,String password) throws RemoteException, IllegalArgumentException;

}
