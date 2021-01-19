package srcclient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

public class ChatController 
{
    private ChatView view;
    private ChatModel model;

    public ChatController(ChatView view, ChatModel model) 
    {
        this.view = view;
        this.model = model;
    }

    /**
     * Si occupa dell'invio del messaggio al server
     * @param msg il messaggio da mandare
     */
    public void inviaMessaggio(String msg)
    {
        JSONObject json = new JSONObject();

        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));

        json.put("Data", data);
        json.put("Time", time);
        json.put("Messaggio", msg);

        model.updateMessaggi(model.getUtenteCorrente().getNome(), new CasellaMessaggio("Tu", msg, data, time));

        if (model.getUtenteCorrente().getNome().equals("Globale"))
        {
            json.put("Tipo-Richiesta", "Invio-Messaggio");
            json.put("Tipo-Messaggio", "Plain-Text");
            json.put("Nome", model.getNome());
        }
        else
        {
            json.put("Tipo-Richiesta", "Invio-Messaggio");
            json.put("Tipo-Messaggio", "Per");

            String dest = model.getUtenteCorrente().getNome();

            json.put("Destinatario", dest);
        }

        AppClient.manda(json.toString());
    }

    /**
     * Setta l'utente corrente e carica anche la sua
     * TextPane
     * @param nome il nome dell'account corrente
     */
    public void setUtenteCorrente(String nome)
    {
        model.setUtenteCorrente(model.getUtente(nome));
    }

    /**
     * Chiude la connessione con il server.
     * Chiude anche l'applicazione.
     */
    public void chiudi ()
    {
        // Notifica il Server che ci vogliamo disconnettere
        JSONObject closeRequest = new JSONObject();
        closeRequest.put("Tipo-Richiesta", "Chiudi-Connessione");

        AppClient.manda(closeRequest.toString());

        // Chiude tutto
        AppClient.dispose();
    }
}