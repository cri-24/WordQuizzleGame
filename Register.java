
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.concurrent.ConcurrentHashMap;



/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */

//classe che implementa il metodo registra_utente(nickName,passw)
public class Register extends RemoteServer implements RegisterService{
	
	private static final long serialVersionUID = 1L;
	private ConcurrentHashMap<String,User> users;
	private JSON_Op json_op;
	private User user;
	
	//costruttore
	public Register(ConcurrentHashMap<String,User> users) {
		this.users = users;	
		this.json_op = new JSON_Op(users);
	}
	
	//metodo regista_utente(nick,passw) per registrare un nuovo utente 
	public int registra_utente(String nickUtente,String password) throws RemoteException, IllegalArgumentException {
		
		
			synchronized(users){
				
				//creazione di utente non ancora registrato
				if(users.containsKey(nickUtente) == false) {
					
					user = new User(nickUtente,password);
					
					users.put(nickUtente, user);
					users.get(nickUtente).set_Registered();
					
					//scrivo sul file le info dell'utente come stringa in formato JSON
					
					json_op.Aggiorna_JSON();
					
					return 0;
				}
				 if(users.containsKey(nickUtente) && users.get(nickUtente).getPassw().equals(password)){
					return 1;
				}
				else
					//utente già esistente
					return -1;
			
			}
	
		
		
	}
	
	
}
