package src;

import java.awt.image.BufferedImage;
import java.io.OutputStreamWriter;
import org.json.JSONObject;
import java.net.Socket;
import java.sql.*;
import javax.imageio.ImageIO;

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
            String msg = "";
            
            byte[] buffer = new byte[GRANDEZZA_BUFFER];
            int l = this.socket.getInputStream().read(buffer);
            msg = new String(buffer, 0, l, "UTF-8");

            JSONObject tmp = new JSONObject(msg);

            if (tmp.getString("Tipo-Richiesta").equals("Autenticazione"))
            {
                Server.getServer().logger.add_msg(Log.LogType.OK, Thread.currentThread().getName() + " tipo richiesta: Autenticazione");
                if (this.gestisciAutenticazione(tmp))
                {   
                    this.client.setKey(Crypt.decodePublicKey(tmp.getString("Chiave")));

                    Thread.currentThread().setName("Thread-" + client.getNome());
                    
                    OutputStreamWriter out = new OutputStreamWriter(this.socket.getOutputStream());
                    out.write(Messaggio.autenticazioneCorretta());
                    out.flush();

                    synchronized (Server.getServer().connected_clients)
                    {
                        if (!Server.getServer().connected_clients.containsKey(this.client)) 
                        {
                            Server.getServer().connected_clients.put(this.client, this.socket);
                        }
                    }
                
                    Thread.sleep(64);

                    JSONObject numeroUtenti = new JSONObject();
                    numeroUtenti.put("Tipo-Richiesta", "Numero-Utenti");
                    numeroUtenti.put("Tipo-Set-Numero", "Connessione");
                    numeroUtenti.put("Nome-Utente", this.client.getNome());
                    numeroUtenti.put("Numero", Server.getServer().getNumeroUtentiConnessi() - 1);

                    Server.getServer().mandaMessaggio(numeroUtenti.toString(), this.client, null);
                }
                else
                {                    
                    OutputStreamWriter out = new OutputStreamWriter(this.socket.getOutputStream());
                    out.write(Messaggio.autenticazioneFallita());
                    out.flush();

                    throw new Exception("Autenticazione fallita per " + this.client.getAddress());
                }
            }
            else
            {
                throw new Exception("Il JSON ricevuto da " + this.client.getAddress() + " non contiene la richiesta di autenticazione");
            }

            while (msg != null)
            {
                byte[] buff = new byte[GRANDEZZA_BUFFER];
                int len = this.socket.getInputStream().read(buff);
                msg = new String(buff, 0, len, "UTF-8");

                JSONObject richiesta = new JSONObject(Crypt.decrypt(msg, Crypt.getPrivateKey()));

                Server.getServer().logger.add_msg(Log.LogType.OK, Thread.currentThread().getName() + " gestisco tipo richiesta");
                switch (richiesta.getString("Tipo-Richiesta"))
                {
                    case "Invio-Messaggio":
                        Server.getServer().logger.add_msg(Log.LogType.OK, Thread.currentThread().getName() + " tipo richiesta: Invio-Messaggio");

                        Server.getServer().logger.add_msg(Log.LogType.OK, Thread.currentThread().getName() + " controllo se il client è mutato o bannato");
                        if (Server.getServer().banned.contains(this.client.getAddress()))
                        {
                            Server.getServer().mandaMessaggio(Messaggio.bannato(), null, this.socket);

                            break;
                        }
                        else if (this.client.getCounter() == 0)
                        {
                            Server.getServer().mandaMessaggio(Messaggio.mutato(), null, this.socket);

                            break;
                        }

                        switch (richiesta.getString("Tipo-Messaggio"))
                        {
                            case "Immagine":
                                /*BufferedImage inputImage = ImageIO.read(ImageIO.createImageInputStream(socket.getOutputStream()));

                                JSONObject invioImmagine = new JSONObject();
                                invioImmagine.put("Tipo-Richiesta", "Nuovo-Messaggio");
                                invioImmagine.put("Tipo-Messaggio", "Immagine");
                                invioImmagine.put("Nome", this.client.getNome());
                                
                                Server.getServer().mandaMessaggio(invioImmagine.toString(), this.client, null);

                                ImageIO.write(inputImage, "PNG", socket.getOutputStream());*/
                            break;
                            case "Per":
                                Server.getServer().messaggioIndirizzato(richiesta.getString("Messaggio"), richiesta.getString("Destinatario"), this.client.getNome());

                                Server.getServer().writer.addMsg(
                                    richiesta.getString("Data") + " " + richiesta.getString("Time")
                                    + "|" +
                                    richiesta.getString("Nome")
                                    + "|" +
                                    richiesta.getString("Messaggio")
                                );
                            break;
                            case "Plain-Text":

                                Server.getServer().mandaMessaggio(Messaggio.nuovoMessaggio(this.client.getNome(), richiesta.getString("Messaggio")), this.client, null);

                                Server.getServer().writer.addMsg(
                                    richiesta.getString("Data") + " " + richiesta.getString("Time")
                                    + "|" +
                                    richiesta.getString("Nome")
                                    + "|" +
                                    richiesta.getString("Messaggio")
                                );
                            break;
                        }
                    break;
                    case "Chiudi-Connessione":
                        msg = null;
                    break;
                }
            }
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg(Log.LogType.ERR, Thread.currentThread().getName() + " " + e);
        }

        try { this.socket.close(); } catch (Exception e) { Server.getServer().logger.add_msg(Log.LogType.ERR, Thread.currentThread().getName() + " " + e); }
        Server.getServer().rimuoviClient(this.client);

        Server.getServer().logger.add_msg(Log.LogType.OK, Thread.currentThread().getName() + " " + this.client.getNome() + " si e' disconnesso, aggiorno il numero degli utenti connessi al client");
        JSONObject numeroUtenti = new JSONObject();
        numeroUtenti.put("Tipo-Richiesta", "Numero-Utenti");
        numeroUtenti.put("Tipo-Set-Numero", "Disconnessione");
        numeroUtenti.put("Nome", this.client.getNome());
        numeroUtenti.put("Numero", Server.getServer().getNumeroUtentiConnessi() - 1);
        numeroUtenti.put("Lista-Utenti", Server.getServer().getListaUtentiConnessi(""));
        Server.getServer().messaggioBroadcast(numeroUtenti.toString());
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