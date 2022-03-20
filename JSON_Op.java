
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;


/*
 * Progetto WORD QUIZZLE 2019/2020
 * Laboratorio Reti di calcolatori 
 * Cristiana Angiuoni 546144
 *   
 */

//classe che contiene tutte le operazioni relative alle operazioni di serializzazione e deserializzazione, tramite gson e json

public class JSON_Op {
	
	//pathname del file per salvare i dati
	private static String JSONpathfile = "./JSONUsers.json";
	private static Path path = Paths.get(JSONpathfile); 
	//tabella hash degli utenti contenente le info da serializzare
	ConcurrentHashMap<String,User> users;
	
	//costruttore
	public JSON_Op (ConcurrentHashMap<String,User> users) {
		this.users=users;
	}
	public JSON_Op () {
		
	}
	
	
	//controlla se il file per salvare i dati degli utenti esiste già
	public ConcurrentHashMap<String, User> checkServer(ConcurrentHashMap<String,User> users){
		
			
			//Se il file per salvare i dati degli utenti non esiste ok
			if (!Files.exists(path)) {
				
				System.out.println("FileJSON non ancora esistente.\n");	
			}
			//altrimenti ripristina
			else { 
				System.out.println("FileJSON già esistente\n");
				users = Ripristina();   
				
			}
			return users;
		
	}
	
		//recupero dati e riscrivo la tabella hash degli utenti
		public ConcurrentHashMap<String, User> Ripristina () {
	
			Gson gson = new Gson();
			
			//stringa di decodifica
			String UsersString = null;
			try {
				UsersString = FromFileToString(JSONpathfile);
				System.out.println("_______  ***  CONTENUTO DEL FILE JSON  ***  ________\n"+UsersString);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//ricopio i dati nella tabella hash e riprendo da dove il server si era interrotto. Type classe
			Type type = new TypeToken<ConcurrentHashMap<String, User>>(){}.getType();
			
			//deserializzazione
		    users = gson.fromJson(UsersString, type);
		    printMap(users);
		    
		    return users;
		}
		
		
		//Recupero dati: leggo dal file tramite un canale in lettura e scrivo in un buffer, poi lo decodifico in stringa 
		public static String FromFileToString (String JSONpathfile) throws IOException {
			
		
			 //stringa per la decodifica
	        String StringDecode = "";
			try {
				//creo un channel(più veloce di inputStream) per la lettura del file non bloccante
				FileChannel inChannel = FileChannel.open(path, StandardOpenOption.READ);
				//buffer per read
				ByteBuffer buffer = ByteBuffer.allocate(1024);
		        boolean end = false;
		       
		        while (end==false) {
		        	//leggo dal canale e scrivo in buffer
		        	int bytesRead = inChannel.read(buffer);
		        	//fine lettura
		        	if (bytesRead==-1) {
		        		end=true;
		        	}
		        	//cambio modalità. sposto il puntatore in cima per lettura
		        	buffer.flip();
		        	//leggo il buffer e decodifico
		            while (buffer.hasRemaining()) {
		            	//concateno la deocdifica
		            	StringDecode = StringDecode + StandardCharsets.UTF_8.decode(buffer).toString();
		            	
		            }
		            //flip per scrivere
		            buffer.flip();
		        }
		        //chiudo il canale
		        inChannel.close();
			}
		    catch (IOException e) {
					e.printStackTrace();
		    }
			System.out.println("Fine ripristino\n");
	        return StringDecode;
		}
	

		//Aggiornamento del file JSON degli utenti, in modo che siano persistenti
		public void Aggiorna_JSON() {
			
			System.out.print("\n______ *** AGGIORNAMENTO FILE JSON _____\n");
			
			GsonBuilder gson = new GsonBuilder();
			
			
			//trasformo users da java object a json string (DESERIALIZZAZIONE)
			String InfoUsersString = gson.create().toJson(users);
			
			System.out.print("\nDopo toJson :"+InfoUsersString);
			
			try {
				//inserisco nel bytebuffer
				ByteBuffer buf = ByteBuffer.wrap(InfoUsersString.getBytes("UTF-8"));
				try {
					//cancello file se esiste 
					Files.deleteIfExists(path); 
					//creo file 
					Files.createFile(path); 
					
					System.out.println("\nFile creato");
				} catch(Exception e) {
					e.printStackTrace();
				}
				//creo channel per scrivere sul file JSON (NIO)
				FileChannel outChannel = FileChannel.open(path, StandardOpenOption.WRITE);
				//leggo dal buffer e scrivo sul canale
				while(buf.hasRemaining()) {
					outChannel.write(buf);
				}
				//chiudo canale
				outChannel.close();
				System.out.println("Scrittura completata\n");
			} 
		
			catch (IOException e) {
				System.out.println("Aggiornamento non riuscito\n");
				e.printStackTrace();
			}
		}

		public static void printMap(ConcurrentHashMap<String,User> mp) {
			  // Costruisce l'iteratore con il metodo dedicato
			  Iterator<Entry<String, User>> it = mp.entrySet().iterator();
			 System.out.println("\n_______  ***  HASHMAP DEGLI UTENTI REGISTRATI  ***  ______\n");
			  // Verifica con il metodo hasNext() che nella hashmap ci siano altri elementi su cui ciclare
			  while (it.hasNext()) {
			    // Utilizza il nuovo elemento (coppia chiave-valore). non parametrizzo gli elem generici
			    ConcurrentHashMap.Entry entry = ( ConcurrentHashMap.Entry)it.next();
			    // Stampa a schermo la coppia chiave-valore;
			    System.out.println("Key ---> " + entry.getKey());
			    User user=(User) entry.getValue();
			    user.set_Offline();
			   
			    }
		}
		
		
		//________   *** JSON Traduzione ***  _________//
		
		//estrae la traduzione tramite il parser
		public String JSONTrad(String tr) throws ParseException {
	    
			  String trad=null;
	        
	      //Parsing del JSON ricevuto
            JsonElement json = new JsonParser().parse(tr);
            JsonArray translationsArray = json.getAsJsonObject().get("matches").getAsJsonArray();
            
            for (JsonElement match : translationsArray) {
                trad = match.getAsJsonObject().get("translation").getAsString().toLowerCase().replaceAll("//","").replaceAll("\\.", "").replaceAll("!", "").replaceAll("-", "").replace(" ", "");
                return trad;
            }
          
	      return null;
		
		}
}
