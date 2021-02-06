package src;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.json.*;

public class Messaggio 
{
    enum TipoRichiesta
    {
        INVIO_MESSAGGIO,
        NUMERO_UTENTI,
        NUOVO_MESSAGGIO,
        CHIUDI_CONNESSIONE,
        NON_PUOI_INVIARE_MESSAGGI,
        AUTENTICAZIONE
    };

    enum TipoMessaggio
    {
        PLAIN_TEXT,
        INDIRIZZATO
    };

    enum TipoNumeroUtenti
    {
        CONNESSIONE,
        DISCONNESSIONE
    };
    
    /**
     * Questo metodo ritorna il JSON contenente tutte
     * le info da mandare al client se l'autenticazione
     * ha avuto successo
     * 
     * @return String, il json
     */
    public static String autenticazioneCorretta()
    {
        JSONObject json = new JSONObject();

        json.put("Tipo-Richiesta", TipoRichiesta.AUTENTICAZIONE);
        json.put("Chiave", Crypt.getCodPubKey());
        json.put("Utenti-Connessi", Server.getServer().getNumeroUtentiConnessi());
        json.put("Lista-Utenti", Server.getServer().getListaUtentiConnessi(""));
        json.put("Risultato", true);

        return json.toString();
    }

    public static String numeroUtenti(TipoNumeroUtenti tipo, String mittente, int numero)
    {
        JSONObject json = new JSONObject();

        json.put("Tipo-Richiesta", TipoRichiesta.NUMERO_UTENTI);
        json.put("Tipo-Set-Numero", tipo);
        json.put("Nome-Utente", mittente);
        json.put("Numero", numero);

        return json.toString();
    }

    /**
     * Questo metodo ritorna il JSON contenente il risultato
     * negativo dell'autenticazione
     * 
     * @return String, il json
     */
    public static String autenticazioneFallita()
    {
        JSONObject json = new JSONObject();

        json.put("Tipo-Richiesta", TipoRichiesta.AUTENTICAZIONE);
        json.put("Risultato", false);

        return json.toString();
    }

    /**
     * Questo metodo ritorna il json contenente la
     * richiesta di disconnessione
     * 
     * @return String, la richiesta di disconnessione in stringa
     */
    public static String disconnessione()
    {
        JSONObject json = new JSONObject();

        json.put("Tipo-Richiesta", TipoRichiesta.CHIUDI_CONNESSIONE);

        return json.toString();
    }

    /**
     * Questo metodo ritorna il json contenente la richiesta per
     * mandare un messaggio nella chat globale.
     * 
     * @param nome il nome del client
     * @param msg il messaggio
     * @return String, il json in formato di stringa
     */
    public static String nuovoMessaggio(String nome, String msg)
    {
        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
        
        JSONObject json = new JSONObject();

        json.put("Tipo-Richiesta", TipoRichiesta.NUOVO_MESSAGGIO);
        json.put("Tipo-Messaggio", TipoMessaggio.PLAIN_TEXT);
        json.put("Data", data);
        json.put("Time", time);
        json.put("Messaggio", msg);
        json.put("Nome", nome);

        return json.toString();
    }

    /**
     * Questo metodo ritorna il json contenente
     * un messaggio di informazione per il client,
     * in questo caso gli fa sapere che e' mutato
     * 
     * @return String, il messaggio di info
     */
    public static String mutato()
    {
        JSONObject json = new JSONObject();

        json.put("Tipo-Richiesta", TipoRichiesta.NON_PUOI_INVIARE_MESSAGGI);
        json.put("Motivo", "Sei stato mutato");

        return json.toString();
    }

    /**
     * Questo metodo ritorna il json contenente
     * un messaggio di informazione per il client,
     * in questo caso gli fa sapere che e' bannato
     * 
     * @return String, il messaggio di info
     */
    public static String bannato()
    {
        JSONObject json = new JSONObject();

        json.put("Tipo-Richiesta", TipoRichiesta.NON_PUOI_INVIARE_MESSAGGI);
        json.put("Motivo", "Sei stato bannato");

        return json.toString();
    }

    /**
     * Questo metodo ritorna il json contenente la richiesta
     * per mandare un messaggio ad un client specifico
     * 
     * @param msg il messaggio
     * @param destinatario il destinatario
     * @return String, il json contenente la richiesta
     */
    public static String nuovoMessaggioIndirizzato(String msg, String mittente)
    {
        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
        
        JSONObject json = new JSONObject();

        json.put("Tipo-Richiesta", TipoRichiesta.NUOVO_MESSAGGIO);
        json.put("Tipo-Messaggio", TipoMessaggio.INDIRIZZATO);
        json.put("Data", data);
        json.put("Time", time);
        json.put("Messaggio", msg);
        json.put("Mittente", mittente);

        return json.toString();
    }
}