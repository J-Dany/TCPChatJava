package src;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
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
    private Log logger = new Log("log");

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
        this.logger.add_msg("[ OK  ] - Server partito");
        try {
            // Esegui finché non viene interrotto il server
            while (!Thread.currentThread().isInterrupted()) {
                // Sta in ascolto per le connessioni in entrata
                this.logger.add_msg("[ OK  ] - " + this.getName() + " sta in ascolto per i client");
                Socket sclient = null;
                try {
                    sclient = this.socket.accept();
                    this.logger.add_msg("[ OK  ] - Connessione accettata per " + sclient.getInetAddress());
                } catch (SocketException e) {
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
                if (!this.connected_clients.containsKey(c)) {
                    this.connected_clients.put(c, sclient);
                }

                this.logger.add_msg("[ OK  ] - Scalo le richieste che puo' fare al minuto");
                this.logger.add_msg("[ OK  ] - Creo l'array di client per scalare le richieste per il client giusto");

                // Scalo le richieste che puo' fare al minuto
                Socket client_socket = null;
                Client[] arr = (Client[]) this.connected_clients.keySet().toArray();
                for (int i = 0; i < arr.length; ++i) {
                    if (arr[i].equals(c)) {
                        client_socket = this.connected_clients.get(arr[i]);
                        c = arr[i];
                        break;
                    }
                }
                c.clientConnected();

                this.logger.add_msg("[ OK  ] - Creo il buffer di ricezione e leggo il messaggio che il client ha mandato.");

                // Buffer per il messaggio ricevuto
                byte[] buffer = new byte[512];
                int l = client_socket.getInputStream().read(buffer);
                String msg = new String(buffer, 0, l, "ISO-8859-1");

                this.logger.add_msg("[ OK  ] - Creato il buffer e letto il messaggio.");
                this.logger.add_msg("[ OK  ] - Inoltro i messaggi a tutti gli altri client.");

                // Mando il messaggio ricevuto a ogni client connesso
                Socket send;
                for (int i = 0; i < this.connected_clients.size(); ++i) {
                    send = this.connected_clients.get(arr[i]);
                    if (send.isConnected() && !arr[i].equals(c)) {
                        OutputStreamWriter out = new OutputStreamWriter(send.getOutputStream(), "ISO-8859-1");
                        out.write(msg);
                        out.flush();
                        out.close();
                    }
                }

                this.logger.add_msg("[ OK  ] - Messaggio inoltrato.");
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

        this.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " chiuso.");
    }

    public static Server getServer()
    {
        return server;
    }

    public static void main(String[] args)
    {
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
            
            // Setto la priorita' del server
            s.setPriority(Thread.MAX_PRIORITY);

            // Setto la priorita' del logger
            s.logger.setPriority(Thread.NORM_PRIORITY);

            // Faccio partire server e logger
            s.start();
            s.logger.start();

            String command = "";

            // Finché il comando non e' null processa il comando letto
            while (command != null)
            {
                System.out.print("? ");
                command = new Scanner(System.in).nextLine();

                s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " ricevuto comando: " + command);

                switch (command)
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

            s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " sta per chiudere il server");

            // Chiudo il socket
            s.socket.close();

            s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " ha chiuso il socket");
            s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " aspetta la fine dell'esecuzione del server");
            
            // Aspetto che il server si chiuda correttamente
            s.join();
        }
        catch (IOException | InterruptedException e)
        {
            s.logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " exception: " + e);
        }

        s.logger.add_msg("[ OK  ] - Libero la memoria creata per contenere la history");
        // Elimino history
        history.clear();

        // Interrompo il logger
        s.logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " fine programma");
        try
        {
            s.logger.interrupt();
            s.join();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Chiudo correttamente il logger
        s.logger.shutdown();
    }
}