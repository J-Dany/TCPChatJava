package src;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Server extends Thread 
{
    /**
     * Rappresenta il server.
     * Server per la classe Client per interagire
     * con il server
     */
    private static Server server;

    /**
     * Rappresenta il logger
     */
    public Log logger = new Log("log");

    /**
     * Thread che scrive nel DB
     */
    public WriteToDB writer;

    /**
     * Rappresenta il socket TCP
     */
    private ServerSocket socket;

    /**
     * Rappresenta il Socket aperto
     * per ogni client connesso
     */
    private HashMap<Client, Socket> connected_clients;

    /**
     * Rappresenta la Thread Pool per gestire
     * i client
     */
    private ExecutorService threadPoolClient;

    /**
     * Una lista di indirizzi bannati
     */
    public ArrayList<InetAddress> banned;

    /**
     * Rappresenta l'array di client connessi
     */
    private Object[] arr;

    /**
     * Costruttore del Server
     * 
     * @param name, rappresenta il nome del thread Server
     * @param port, la porta in cui il Socket del Server resterà in ascolto
     * @throws IOException, errore con il Socket
     */
    public Server(String name, int port) throws IOException
    {
        super(name);
        this.socket = new ServerSocket(port);
        this.banned = new ArrayList<>();
        this.connected_clients = new HashMap<>();
        this.writer = new WriteToDB("WriterDB");
    }

    /**
     * Banna l'IP passato come parametro
     */
    public void ban(InetAddress address)
    {
        this.banned.add(address);
    }

    /**
     * Quando un client si disconnette
     * viene chiamato questo metodo che rimuove
     * il client dall'HashMap dei client connessi
     * 
     * @param c, il puntatore al client da rimuovere
     */
    public synchronized void rimuoviClient(Client c)
    {
        this.connected_clients.remove(c);
    }

    /**
     * Ritorna il numero degli utenti connessi.
     * Viene richiamato solo quando un nuovo client
     * si connette o uno già connesso di disconnette
     * per aggiornare il numero dei client connessi
     * nell'app del client
     * @return int, numero utenti connessi
     */
    public synchronized int getNumeroUtentiConnessi()
    {
        return this.connected_clients.size();
    }

    /**
     * Manda messaggio a tutti tranne che
     * al client che ha mandato il messaggio. Se
     * il client è null vuol dire mandare il messaggio
     * viene mandato a lui tramite il socket passato, solo
     * e soltanto a lui
     * 
     * @param msg, messaggio da inviare
     * @param c, rappresenta il mittente (o destinatario)
     * @param s, rappresenta il socket del client
     */
    public synchronized void mandaMessaggio(String msg, Client c, Socket s)
    {
        Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " inoltro il messaggio arrivato...");
        if (c == null)
        {
            try
            {
                OutputStreamWriter out = new OutputStreamWriter(s.getOutputStream(), "UTF8");
                out.write(msg);
                out.flush();
            }
            catch (Exception e)
            {
                this.logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
            }

            this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " messaggio inoltrato");
            return;
        }

        Client mittente = c;
        Socket send;
        arr = this.connected_clients.keySet().toArray();

        for (int i = 0; i < this.connected_clients.size(); ++i) 
        {
            send = this.connected_clients.get(arr[i]);
            if (!arr[i].equals(mittente)) 
            {
                try
                {
                    OutputStreamWriter out = new OutputStreamWriter(send.getOutputStream(), "UTF8");
                    out.write(msg);
                    out.flush();
                }
                catch (Exception e)
                {
                    this.logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
                }
            }
        }

        this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " messaggio inoltrato");
    }

    /**
     * Manda un messaggio a tutti i client.
     * Principalmente usato per aggiornare il numero
     * dei client connessi nell'app del client.
     * 
     * @param msg, messaggio da inviare a tutti
     */
    public synchronized void messaggioBroadcast(String msg)
    {
        this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " mando messaggio in broadcast: " + msg);
        arr = this.connected_clients.keySet().toArray();
        for (int i = 0; i < this.connected_clients.size(); ++i)
        {
            Socket s = this.connected_clients.get(arr[i]);

            try
            {
                OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
                writer.write(msg);
                writer.flush();
            }
            catch (Exception e)
            {
                this.logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
            }
        }
        this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " mandato messaggio broadcast");
    }

    @Override
    public void run() 
    {
        this.threadPoolClient = Executors.newScheduledThreadPool(32);
        
        this.logger.add_msg("[ OK  ] - Server partito");
        try 
        {
            // Esegui finché non viene interrotto il server
            while (!Thread.currentThread().isInterrupted()) 
            {
                // Sta in ascolto per le connessioni in entrata
                this.logger.add_msg("[ OK  ] - " + this.getName() + " sta in ascolto per i client");
                Socket sclient = null;
                try 
                {
                    sclient = this.socket.accept();
                    this.logger.add_msg("[ OK  ] - Connessione accettata per " + sclient.getInetAddress());                    
                }
                catch (SocketException e) 
                {
                    this.logger.add_msg("[ ERR ] - " + this.getName() + " exception: " + e);
                    Thread.currentThread().interrupt();
                    break;
                }
                this.logger.add_msg("[ OK  ] - " + this.getName() + " accettata la connessione per " + sclient.getInetAddress());

                // Creo un nuovo oggetto client, che rappresenta il client connesso
                this.logger.add_msg("[ OK  ] - Creo un oggetto Client per rappresentare il client connesso...");
                    Client c = new Client(sclient.getInetAddress());
                this.logger.add_msg("[ OK  ] - Oggetto creato");

                this.logger.add_msg("[ OK  ] - Controllo se il client esiste gia'");

                // Controllo se è un client che gia' si era connesso in precedenza, altrimenti
                // lo aggiungo
                synchronized (this.connected_clients)
                {
                    if (!this.connected_clients.containsKey(c)) 
                    {
                        this.connected_clients.put(c, sclient);
                    }
                }

                this.logger.add_msg("[ OK  ] - Sto in ascolto per i messaggi di questo client.");

                this.threadPoolClient.submit(new ConnectionClient(sclient, c));
            }
        } 
        catch (Exception e) 
        {
            this.logger.add_msg("[ ERR ] - " + this.getName() + " exception: " + e);
        }

        // Libera le risorse allocate
        this.liberaRisorse();
    }

    /**
     * Libera le risorse allocate per il server
     */
    private void liberaRisorse()
    {
        this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " Chiudo tutti i socket dei client");
        for (Socket s : this.connected_clients.values())
        {
            try
            {
                JSONObject chiudiConnessione = new JSONObject();
                chiudiConnessione.put("Tipo-Richiesta", "Chiudi-Connessione");

                s.getOutputStream().write(chiudiConnessione.toString().getBytes());
                s.getOutputStream().flush();
                
                s.close();
            }
            catch (Exception e)
            {
                this.logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
            }
        }
        this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " Chiusi tutti i socket dei client");

        this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " Libero la memoria creata per contenere hashmap");
        
        // Elimino hashmap
        this.connected_clients.clear();

        this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " Libero la memoria creata per contenere arraylist di client bannati");
        // Elimino i client bannati
        this.banned.clear();

        this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " spengo la ThreadPool");
        this.threadPoolClient.shutdownNow();

        this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " chiuso.");
    }

    public synchronized HashMap<Client, Socket> getConnectedClients()
    {
        return connected_clients;
    }

    /**
     * Restituisce l'oggetto Server creato nel main
     * per richiamare i metodi del Server tipo "ban" o
     * "mandaMessaggio" o "messaggioBroadcast"
     */
    public static Server getServer()
    {
        return server;
    }

    public static void main(String[] args)
    {
        Thread.currentThread().setName("Console");
        Server s = null;

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
                s = new Server("SERVER", Integer.parseInt(args[0]));
            }
            
            server = s;
            
            // Setto la priorita' del server
            s.setPriority(Thread.MAX_PRIORITY);

            // Setto la priorita' del logger
            s.logger.setPriority(Thread.NORM_PRIORITY);

            // Faccio partire server e logger
            s.start();
            s.logger.start();

            // Setto la priorità del Thread write e lo faccio partire
            s.writer.setPriority(Thread.NORM_PRIORITY);
            s.writer.start();

            // Avvio la console del Server
            Console console = new Console();
            console.avvia();

            s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " sta per chiudere il server");

            // Chiudo il socket
            s.socket.close();

            s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " ha chiuso il socket");
            s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " aspetta la fine dell'esecuzione del server");
            
            // Aspetto che il server si chiuda correttamente
            s.join();

            s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " interrompe il thread WriterToDB");

            // Interrompo e aspetto il thread Writer
            s.writer.interrupt();
            s.writer.join();

            s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " thread WriterToDB interrotto");
        }
        catch (IOException | InterruptedException e)
        {
            if (s == null)
            {
                System.out.println("Errore nell'inizializzare il server");
            }
            s.logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " exception: " + e);
        }

        try
        {
            s.logger.interrupt();
            s.join();
        }
        catch (Exception e)
        {
            s.logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
        }

        // Interrompo il logger
        s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " fine programma");

        // Chiudo correttamente il logger
        s.logger.shutdown();
    }
}