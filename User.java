import java.util.LinkedList;

/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */


public class User {
	private final String nickUtente;
	private final String passw;
	private LinkedList<String> lista_amici;
	private state s;
	private Long punteggioTot;
	
	//costruttore
	public User(String nickUtente, String passw) {
		this.nickUtente = nickUtente;
		this.passw = passw;
		punteggioTot=(long) 0;
		lista_amici= new LinkedList<String>();
	}
	//restituisce il nome utente
	public String getName() {
		return this.nickUtente;
	}
	//restituisce la password
	public String getPassw() {
		return this.passw;
	}
	
	//restituisce il punteggio totale
	public long getPunteggioTot() {
		
		//sincronizzazione sulla variabile punteggio per accesso esclusiovo 
		synchronized (punteggioTot) {
			  return punteggioTot;
        }
      
    }
	
	//restituisce la lista di amici
	public LinkedList<String> getListaAmici() {
		return lista_amici;     
    }
	//setta il punteggio totale dell'utente, sincornizzato per i thread-sfida
	public  void setPunteggio(long n) {
		synchronized (punteggioTot) {
			punteggioTot=punteggioTot+n;
		}
	}
	//setta lo stato online dopo login
	public void set_Online() {
		this.s=state.online;
	}
	//setta stato offline dopo logout
	public void set_Offline() {
		this.s=state.offline;
	}
	//setta lo stato in match dopo aver accettato una sfida
	public void set_InMatch() {
		this.s=state.busy_inmatch;
	}
	public void set_Registered() {
		this.s=state.registered;
	}
	//restituisce lo stato
	public state get_state() {
		return s;
	}
    
}
