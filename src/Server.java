package src;

import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Server extends Thread 
{
    /**
     * Rappresenta la coda dei comandi dati al server
     */
    private static Stack<String> history = new Stack<>();

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
     * Una lista di indirizzi bannati
     */
    public ArrayList<InetAddress> banned;

    /**
     * Rappresenta l'array di client connessi
     */
    private Object[] arr;

    public Server(String name, int port) throws IOException, SQLException
    {
        super(name);
        this.socket = new ServerSocket(port);
        this.banned = new ArrayList<>();
        this.connected_clients = new HashMap<>();
        this.writer = new WriteToDB("WriterDB");
    }

    public void ban(InetAddress address)
    {
        this.banned.add(address);
    }

    public void rimuoviClient(Client c)
    {
        this.connected_clients.remove(c);
    }

    public int getNumeroUtentiConnessi()
    {
        return this.connected_clients.size();
    }

    public void mandaMessaggio(String msg, Client c, Socket s)
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

    public void messaggioBroadcast(String msg)
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
        ExecutorService exec = Executors.newScheduledThreadPool(32);
        
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
                synchronized (this)
                {
                    if (!this.connected_clients.containsKey(c)) 
                    {
                        this.connected_clients.put(c, sclient);
                    }
                }
                
                this.logger.add_msg("[ OK  ] - Scalo le richieste che puo' fare al minuto");
                this.logger.add_msg("[ OK  ] - Creo l'array di client per scalare le richieste per il client giusto");

                // Scalo le richieste che puo' fare al minuto
                Socket client_socket = null;
                synchronized (this)
                {
                    arr = this.connected_clients.keySet().toArray();
                    for (int i = 0; i < arr.length; ++i) {
                        if (arr[i].equals(c)) {
                            client_socket = this.connected_clients.get(arr[i]);
                            c = (Client)arr[i];
                            break;
                        }
                    }
                    c.clientConnected();
                }

                this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + "Aggiorno numero utenti connessi per client");
                this.messaggioBroadcast("!!:" + this.getNumeroUtentiConnessi());

                this.logger.add_msg("[ OK  ] - Sto in ascolto per i messaggi di questo client.");

                // Buffer per il messaggio ricevuto
                exec.submit(new ConnectionClient(client_socket, c));
            }
        } 
        catch (Exception e) 
        {
            this.logger.add_msg("[ ERR ] - " + this.getName() + " exception: " + e);
        }

        this.logger.add_msg("[ OK  ] - Libero la memoria creata per contenere hashmap");
        // Elimino hashmap
        this.connected_clients.clear();

        this.logger.add_msg("[ OK  ] - Libero la memoria creata per contenere arraylist di client bannati");
        // Elimino i client bannati
        this.banned.clear();

        this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " spengo la ThreadPool");
        exec.shutdownNow();

        this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " chiuso.");
    }

    public static Server getServer()
    {
        return server;
    }

    public static void main(String[] args)
    {
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

            String command = "";

            Scanner input = new Scanner(System.in);

            // Finché il comando non e' null processa il comando letto
            while (command != null)
            {
                System.out.print("? ");
                command = input.nextLine();
                String[] arguments = command.split(" ");
                String com = arguments[0];

                s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " ricevuto comando: " + command);

                switch (com)
                {
                    case "history":
                        int i = 0;
                        for (String c : history)
                        {
                            System.out.println(i++ + ": " + c);
                        }
                    break;
                    case "s":
                    case "stop":
                    case "e":
                    case "exit":
                        command = null;
                    break;
                    case "ban":
                        String ip = arguments[1];
                        s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " banna: " + ip);
                        s.ban(InetAddress.getByName(ip));
                    break;
                    case "show-connected-client":
                    case "showcc":
                    case "scc":
                        try
                        {
                            for (Socket c : s.connected_clients.values())
                            {
                                System.out.println(c.getInetAddress());
                            }
                        }
                        catch (Exception e)
                        {
                            s.logger.add_msg("[ ERR ] - Main exception: " + e);
                        }
                    break;
                    default:
                        System.out.println("Comando non riconosciuto.");
                }

                history.push(command);
            }

            input.close();

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
        catch (IOException | InterruptedException | SQLException e)
        {
            if (s == null)
            {
                System.out.println("Errore nell'inizializzare il server");
            }
            s.logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " exception: " + e);
        }

        s.logger.add_msg("[ OK  ] - Libero la memoria creata per contenere la history");
        // Elimino history
        history.clear();

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