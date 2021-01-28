package srcclient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatController 
{
    private ChatModel model;

    public ChatController(ChatModel model) 
    {
        this.model = model;
    }

    /**
     * Si occupa dell'invio del messaggio al server
     * @param msg il messaggio da mandare
     */
    public void inviaMessaggio(String msg)
    {
        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));

        model.updateMessaggi(model.getUtenteCorrente().getNome(), new CasellaMessaggio("Tu", msg, data, time));

        if (model.getUtenteCorrente().getNome().equals("Globale"))
        {
            AppClient.manda(Messaggio.nuovoMessaggio(model.getNome(), msg));
        }
        else
        {
            AppClient.manda(Messaggio.nuovoMessaggioIndirizzato(msg, model.getUtenteCorrente().getNome()));
        }
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
        AppClient.manda(Messaggio.disconnessione());

        // Chiude tutto
        AppClient.dispose();
    }
}