package srcclient;

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
     * Questo metodo ritorna il json contenente la
     * richiesta per autenticarsi nella chat
     * 
     * @param nomeUtente nome del client
     * @param passwd la password del client
     * @return String, contenente il json
     */
    public static String autenticazione(String nomeUtente, String passwd)
    {
        JSONObject json = new JSONObject();

        json.put("Tipo-Richiesta", TipoRichiesta.AUTENTICAZIONE);
        json.put("Nome", nomeUtente);
        json.put("Chiave", Crypt.getCodPubKey());
        json.put("Password", passwd);

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

        json.put("Tipo-Richiesta", TipoRichiesta.INVIO_MESSAGGIO);
        json.put("Tipo-Messaggio", TipoMessaggio.PLAIN_TEXT);
        json.put("Data", data);
        json.put("Time", time);
        json.put("Messaggio", msg);
        json.put("Nome", nome);

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
    public static String nuovoMessaggioIndirizzato(String msg, String destinatario)
    {
        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
        
        JSONObject json = new JSONObject();

        json.put("Tipo-Richiesta", TipoRichiesta.INVIO_MESSAGGIO);
        json.put("Tipo-Messaggio", TipoMessaggio.INDIRIZZATO);
        json.put("Data", data);
        json.put("Time", time);
        json.put("Messaggio", msg);
        json.put("Destinatario", destinatario);

        return json.toString();
    }
}