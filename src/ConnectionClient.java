package src;

import java.net.Socket;

public class ConnectionClient implements Runnable
{
    private Socket socket;
    private Client client;

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
            String msg = "";

            while (msg != null)
            {
                byte[] buffer = new byte[1024];
                int l = this.socket.getInputStream().read(buffer);
                msg = new String(buffer, 0, l, "ISO-8859-1");

                if (msg == "close")
                {
                    msg = null;
                    break;
                }

                Server.getServer().mandaMessaggio(msg);

                Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " aggiungo il messaggio al db");
                Server.getServer().writer.addMsg(msg);
            }
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg("[ ERR ] - Errore in Connection: " + e);
        }

        Server.getServer().rimuoviClient(this.client);
    }
}