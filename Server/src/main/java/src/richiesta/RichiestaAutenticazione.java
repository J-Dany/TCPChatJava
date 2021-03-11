package src.richiesta;

import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.json.JSONObject;
import src.Server;
import src.Log.LogType;
import src.Log;
import src.Messaggio;
import src.Client;
import src.DatabaseConnection;
import src.eccezioni.*;

public class RichiestaAutenticazione implements Richiesta
{
    @Override
    public void rispondi(JSONObject json, Client c, Socket s) throws AutenticazioneFallita, ChiudiConnessione
    {
        String nome = json.getString("Nome");
        String passwd = json.getString("Password");

        if (!autenticazione(nome, passwd))
        {
            Server.getServer().logger.addMsg(Log.LogType.ERR, Thread.currentThread().getName() + " " + c.getAddress() + " ha provato ad autenticarsi con " + nome);

            Server.getServer().mandaMessaggio(
                Messaggio.autenticazioneFallita(), 
                c, 
                s
            );

            throw new AutenticazioneFallita(null);
        }

        c.setNome(nome);

        Thread.currentThread().setName(
            nome + "-Thread"
        );

        Server.getServer().aggiungiNuovoClient(c, s);

        Server.getServer().mandaMessaggio(
            Messaggio.autenticazioneCorretta(), 
            c, 
            s
        );
    }

    /**
     * Gestisce l'autenticazione da parte del client
     * 
     * @param c il client che si vuole autenticare
     * @param nome il nome del client
     * @param password la password del client
     * @return boolean, il risultato dell'autenticazione
     */
    private boolean autenticazione(String nome, String password)
    {
        if (Server.getServer().isAlreadyAuth(nome))
        {
            return false;
        }

        try
        {
            Connection connection = DatabaseConnection.getConnection();

            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery(
                "SELECT COUNT(*) as num_rows FROM utenti WHERE username = '" + nome + "' AND password = '" + password + "'"
            );

            if (result.next() && result.getInt("num_rows") == 0)
            {
                throw new Exception("Utente non trovato");
            }

            return true;
        }
        catch (Exception e)
        {
            Server.getServer().logger.addMsg(LogType.ERR, Thread.currentThread().getName() + " " + e);
        }

        return false;
    }
}