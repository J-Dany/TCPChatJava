package src;

import org.json.JSONObject;
import src.Log.LogType;
import src.richiesta.Richiesta;
import src.richiesta.RichiestaFactory;
import java.net.Socket;
import src.eccezioni.*;

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
            int l = 0;
            String msg;    
            while (true)
            {
                byte[] buffer = new byte[GRANDEZZA_BUFFER];
                l = this.socket.getInputStream().read(buffer);
                msg = new String(buffer, 0, l, "UTF-8");

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
                        richiesta.rispondi(json, this.client, this.socket);
                    }
                    else
                    {
                        throw new Exception("Non è stato possibile rispondere alla richiesta.");
                    }
                }
            }   
        }
        catch (AutenticazioneFallita e)
        {
            Server.getServer().logger.addMsg(LogType.OK, Thread.currentThread().getName() + " autenticazione fallita per " + this.client.getNome() + " da " + this.client.getAddress());
            return;
        }
        catch (ChiudiConnessione e)
        {
            Server.getServer().logger.addMsg(LogType.OK, Thread.currentThread().getName() + " " + this.client.getNome() + " si è disconnesso");
        }
        catch (Exception e)
        {
            Server.getServer().logger.addMsg(LogType.ERR, Thread.currentThread().getName() + " " + e);
        }

        Server.getServer().rimuoviClient(this.client);
        Server.getServer().messaggioBroadcast(Messaggio.numeroUtenti(Messaggio.TipoNumeroUtenti.DISCONNESSIONE, this.client.getNome(), Server.getServer().getNumeroUtentiConnessi()));
    }
}