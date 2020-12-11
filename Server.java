import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Server extends Thread
{
    private static Server server;
    private Log logger = new Log("log");
    private ServerSocket socket;

    private HashMap<Client, Socket> connected_clients;
    private ArrayList<InetAddress> banned;

    public Server(String name, int port) throws IOException
    {
        super(name);
        this.socket = new ServerSocket(port);
        this.banned = new ArrayList<>();
        this.connected_clients = new HashMap<>();
    }

    public void ban(InetAddress address)
    {
        this.banned.add(address);
    }

    @Override
    public void run()
    {
        this.logger.add_msg("[ OK ] - Server partito");
        try
        {
            // Esegui finché non viene interrotto il server
            while (!Thread.currentThread().isInterrupted())
            {
                // Sta in ascolto per le connessioni in entrata
                this.logger.add_msg("[ OK ] - " + this.getName() + " sta in ascolto per i client"); 
                    Socket sclient = this.socket.accept(); 
                this.logger.add_msg("[ OK ] - " + this.getName() + " accettata la connessione per " + sclient.getInetAddress());

                // Creo un nuovo oggetto client, che rappresenta il client connesso
                this.logger.add_msg("[ OK ] - Creo un oggetto Client per rappresentare il client connesso..."); 
                    Client c = new Client(sclient.getInetAddress()); 
                this.logger.add_msg("[ OK ] - Oggetto creato");
                
                this.logger.add_msg("[ OK ] - Controllo se il client esiste gia'");

                // Controllo se è un client che gia' si era connesso in precedenza, altrimenti lo aggiungo
                if (!this.connected_clients.containsKey(c))
                {
                    this.connected_clients.put(c, sclient);
                }

                this.logger.add_msg("[ OK ] - Scalo le richieste che puo' fare al minuto");
                this.logger.add_msg("[ OK ] - Creo l'array di client per scalare le richieste per il client giusto");

                // Scalo le richieste che puo' fare al minuto
                Socket client_socket = null;
                Client[] arr = (Client[])this.connected_clients.keySet().toArray();
                for (int i = 0; i < arr.length; ++i)
                {
                    if (arr[i].equals(c))
                    {
                        client_socket = this.connected_clients.get(arr[i]);
                        c = arr[i];
                        break;
                    }
                }
                c.clientConnected();

                // Buffer per il messaggio ricevuto
                byte[] buffer = new byte[512];
                int l = client_socket.getInputStream().read(buffer);
                String msg = new String(buffer, 0, l, "ISO-8859-1");

                // Mando il messaggio ricevuto a ogni client connesso
                Socket send;
                for (int i = 0; i < this.connected_clients.size(); ++i)
                {
                    send = this.connected_clients.get(arr[i]);
                    if (send.isConnected() && !arr[i].equals(c))
                    {
                        OutputStreamWriter out = new OutputStreamWriter(send.getOutputStream(), "ISO-8859-1");
                        out.write(msg);
                        out.flush();
                    }
                }
            }
        }
        catch (Exception e)
        {
            this.logger.add_msg("[ ER ] - " + e);
        }

        this.logger.add_msg("[ OK ] - Sto chiudendo il server...");

        // Provo a chiudere il socket
        try { if (!socket.isClosed()) { socket.close(); } }
        catch (Exception e) { this.logger.add_msg("[ ER ] - " + e); }

        this.logger.add_msg("[ OK ] - Server chiuso.");
        this.logger.shutdown();
    }

    public static Server getServer()
    {
        return server;
    }

    public static void main(String[] args)
    {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        Server s = null;
        server = s;

        try
        {
            // Se non ci sono argomenti, di default sta in ascolto nella porta 60000
            if (args.length == 0)
            {
                s = new Server("SERVER", 60000);
            }
            // Altrimenti ascolta nella porta passata come parametro
            else
            {
                s = new Server("SERVER", Integer.parseInt(args[1]));
            }
            
            threadPool.submit(s);
            threadPool.submit(s.logger);

            // Aspetto per un messaggio qualunque per interrompere il server
            int t = System.in.read();

            s.interrupt();

            // Chiudo il socket
            s.socket.close();
        }
        catch (IOException e)
        {
            s.logger.add_msg("[ ER ] - " + e);
        }

        threadPool.shutdownNow();
    }
}