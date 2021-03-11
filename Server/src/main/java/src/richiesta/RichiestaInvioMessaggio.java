package src.richiesta;

import java.net.Socket;

import org.json.JSONObject;

import src.Client;
import src.Messaggio;
import src.Server;
import src.eccezioni.*;

public class RichiestaInvioMessaggio implements Richiesta
{
    @Override
    public void rispondi(JSONObject json, Client c, Socket s) throws AutenticazioneFallita, ChiudiConnessione
    {
        if (Server.getServer().isBanned(c.getAddress())) // il client è bannato
        {
            Server.getServer().mandaMessaggio(
                Messaggio.bannato(), 
                c, 
                s
            );

            return;
        }
        else if (c.getCounter() == 0) // il client è mutato
        {
            Server.getServer().mandaMessaggio(
                Messaggio.mutato(), 
                c, 
                s
            );

            return;
        }

        if (json.has("Destinatario"))
        {
            Server.getServer().messaggioIndirizzato(
                json.getString("Messaggio"), 
                json.getString("Destinatario"), 
                c.getNome()
            );
        }
        else
        {
            Server.getServer().mandaMessaggio(
                json.getString("Messaggio"), 
                null, 
                null
            );
        }
    }
}