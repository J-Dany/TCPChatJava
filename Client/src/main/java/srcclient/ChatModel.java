package srcclient;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import javax.swing.event.SwingPropertyChangeSupport;

public class ChatModel 
{
    private SwingPropertyChangeSupport propertyChangeSupport;

    /**
     * Utenti
     */
    private HashMap<String, Utente> utenti = new HashMap<>();

    /**
     * Lista di caselle
     */
    private HashMap<String, CasellaUtente> caselle = new HashMap<>();

    /**
     * Nome del client
     */
    private String nome;

    /**
     * Ctor
     * 
     * @param nome, nome dell'utente che ha avviato l'applicazione e si è autenticato
     */
    public ChatModel(String nome)
    {
        this.propertyChangeSupport = new SwingPropertyChangeSupport(this);
        this.nome = nome;
    }

    /**
     * Aggiunge il listener
     * @param prop
     */
    public void addListener(PropertyChangeListener prop)
    {
        propertyChangeSupport.addPropertyChangeListener(prop);
    }

    /**
     * Ritorna il nome dell'utente che ha avviato l'applicazione e si è autenticato
     * 
     * @return String
     */
    public String getNome()
    {
        return this.nome;
    }

    /**
     * Rimuove l'utente dalla lista, null se non esiste
     * 
     * @param nome, il nome dell'utente da eliminare
     * @return Utente
     */
    public Utente removeUtente(String nome)
    {
        if (utenti.containsKey(nome))
        {
            return utenti.remove(nome);
        }

        return null;
    }

    /**
     * Elimina la casella dell'utente selezionato e la ritorna, altrimenti ritorna null
     * 
     * @param nome, il nome dell'utente
     * @return CasellaUtente
     */
    public CasellaUtente removeCasella(String nome)
    {
        if (caselle.containsKey(nome))
        {
            return caselle.remove(nome);
        }

        return null;
    }

    /**
     * Ritorna un oggetto Utente se esiste, altrimenti null
     * 
     * @param nome, nome dell'utente
     * @return Utente
     */
    public Utente getUtente(String nome)
    {
        if (utenti.containsKey(nome))
        {
            return utenti.get(nome);
        }

        return null;
    }

    /**
     * Aggiorna la lista di utenti con il nuovo utente
     * 
     * @param u, il nuovo utente
     */
    public void updateUtenti(Utente u)
    {
        utenti.put(u.getNome(), u);
    }

    /**
     * Ritorna la casella dato il nome, null se non esiste
     * 
     * @param nome, il nome dell'utente
     * @return CasellaUtente
     */
    public CasellaUtente getCasella(String nome)
    {
        if (caselle.containsKey(nome))
        {
            return caselle.get(nome);
        }

        return null;
    }

    /**
     * Aggiunge la casella degli utenti alla lista
     * 
     * @param nome, il nome dell'utente che si è connesso
     * @param c, la casella utente
     */
    public void updateCaselle(String nome, CasellaUtente c)
    {
        caselle.put(nome, c);
    }

    public void incrementaNumeroMessaggiDa (String nome)
    {
        if (caselle.containsKey(nome))
        {
            caselle.get(nome).incrementaNumeroMessaggi();
        }
    }

    public void updateMessaggi (String nome, CasellaMessaggio c)
    {
        if (utenti.containsKey(nome))
        {
            utenti.get(nome).aggiungiMessaggio(c);
        }
    }

    /**
     * Rilascia le risorse allocate
     */
    public void dispose()
    {
        utenti.clear();
        caselle.clear();
        nome = null;
    }
}