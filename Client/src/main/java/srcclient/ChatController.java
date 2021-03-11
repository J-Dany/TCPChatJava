package srcclient;

import srcclient.Messaggio.TipoRisposta;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;
import srcclient.oggetti_grafici.*;
import srcclient.risposta.*;

public class ChatController extends Thread
{
    private ChatModel model;
    private ChatView view;

    /**
     * Socket di connessione del client
     */
    private Socket s;
    
    /**
     * Output stream per mandare i dati
     */
    private OutputStreamWriter outputStream;

    public ChatController(Socket s, OutputStreamWriter writer) 
    {
        this.s = s;
        this.outputStream = writer;
        this.start();
    }

    public void setModel(ChatModel model)
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
            manda(Messaggio.nuovoMessaggio(model.getNome(), msg));
        }
        else
        {
            manda(Messaggio.nuovoMessaggioIndirizzato(msg, model.getUtenteCorrente().getNome()));
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
        manda(Messaggio.disconnessione());

        // Chiude tutto
        AppClient.dispose();
    }

    /**
     * Manda i dati al server
     * 
     * @param data la stringa da mandare al server
     */
    public void manda(String data)
    {
        try
        {
            outputStream.write(data);
            outputStream.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void run()
    {
        AppClient.formAutenticazione();

        try
        {
            while (true)
            {
                byte[] buffer = new byte[AppClient.GRANDEZZA_BUFFER];
                int l = s.getInputStream().read(buffer);
                String msg = new String(buffer, 0, l, "UTF-8");

                JSONObject json = new JSONObject(msg);                
                TipoRisposta tr = TipoRisposta.valueOf(json.getString("Tipo-Risposta"));

                Risposta r = null;

                switch (tr)
                {
                    case AUTENTICAZIONE:
                        //r = new RispostaAutenticazione();
                    break;
                }

                // r.interpreta(json);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}