package src;

import org.json.JSONObject;
import src.Log.LogType;
import src.richiesta.Richiesta;
import src.richiesta.RichiestaFactory;
import java.net.Socket;
import java.sql.*;

public class ConnectionClient implements Runnable
{
    private Socket socket;
    private Client client;

    private final int GRANDEZZA_BUFFER = 8192;

    public ConnectionClient(Socket socket, Client client)
    {
        this.socket = socket;
        this.client = client;
    }

    @Override
    public void run() 
    {
        try
        {            
            while (true)
            {
                byte[] buffer = new byte[GRANDEZZA_BUFFER];
                int l = this.socket.getInputStream().read(buffer);
                String msg = new String(buffer, 0, l, "UTF-8");

                if (msg.isEmpty())
                {
                    throw new Exception("La richiesta è vuota.");
                }
                else
                {
                    JSONObject json = new JSONObject(msg);
                    Richiesta richiesta = RichiestaFactory.crea(json);
                    
                    if (richiesta != null)
                    {
                        richiesta.rispondi(json);
                    }
                    else
                    {
                        throw new Exception("Non è stato possibile rispondere alla richiesta.");
                    }
                }
            }   
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg(LogType.ERR, Thread.currentThread().getName() + " " + e);
        }
    }

    private boolean gestisciAutenticazione(JSONObject richiesta)
    {
        String nomeUtente = richiesta.getString("Nome");

        if (Server.getServer().isAlreadyAuth(nomeUtente))
        {
            Server.getServer().logger.add_msg(Log.LogType.ERR, Thread.currentThread().getName() + " " + this.client.getAddress() + " ha provato ad autenticarsi con " + nomeUtente);
            return false;
        }

        String password = richiesta.getString("Password");
        this.client.setNome(nomeUtente);
        Server.getServer().logger.add_msg(Log.LogType.OK, Thread.currentThread().getName() + " nuovo client: " + nomeUtente);

        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Server.getServer().logger.add_msg(Log.LogType.OK, Thread.currentThread().getName() + " mi connetto al database e istanzio un oggetto di tipo Statement");

            Connection c = DatabaseConnection.getConnection();
            Statement s = c.createStatement();

            Server.getServer().logger.add_msg(Log.LogType.OK, Thread.currentThread().getName() + " connesso al database e creato oggetto Statement, ora eseguo la query di ricerca utete");

            ResultSet utenti = s.executeQuery("SELECT COUNT(*) as num_rows FROM utenti WHERE username = '" + nomeUtente + "' AND password = '" + password + "'");

            Server.getServer().logger.add_msg(Log.LogType.OK, Thread.currentThread().getName() + " query eseguita correttamente");
            
            if (utenti.next() && utenti.getInt("num_rows") == 0)
            {
                Server.getServer().logger.add_msg(Log.LogType.ERR, Thread.currentThread().getName() + " utente non riconosciuto (" + nomeUtente + ")");
                throw new Exception("Utente non riconosciuto");
            }

            Server.getServer().logger.add_msg(Log.LogType.OK, Thread.currentThread().getName() + " si e' connesso " + nomeUtente);

            return true;
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg(Log.LogType.ERR, Thread.currentThread().getName() + " " + e);
        }

        return false;
    }
}