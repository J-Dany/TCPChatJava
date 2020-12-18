package src;

import java.net.Socket;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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

                if (msg.contains("%%!"))
                {
                    String nomeUtente = msg.split("%%!")[0];
                    Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " nuovo client: " + nomeUtente);

                    try
                    {
                        Class.forName("com.mysql.cj.jdbc.Driver");

                        Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " mi connetto al database e istanzio un oggetto di tipo Statement");

                        Connection c = DriverManager.getConnection(Config.URL, Config.USER, Config.PASSWD);
                        Statement s = c.createStatement();

                        Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " connesso al database e creato oggetto Statement, ora eseguo la query di ricerca utete");

                        ResultSet utenti = s.executeQuery("SELECT username FROM utenti WHERE username = '" + nomeUtente + "'");

                        Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " query eseguita correttamente");

                        if (utenti.getFetchSize() == 0)
                        {
                            Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " esiste di gia' l'utente richiesto (" + nomeUtente + ")");
                            Server.getServer().mandaMessaggio("UTENTE_NON_RICONOSCIUTO", this.client);
                            return;
                        }

                        Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " si e' connesso " + nomeUtente);

                        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));

                        msg = data + " " + time + "|" + nomeUtente + "|si e' connesso!";
                    }
                    catch (Exception e)
                    {
                        Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
                    }
                }
                else if (msg.equals("close"))
                {
                    Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " si e' disconnesso un utente");
                    break;
                }
                
                Server.getServer().mandaMessaggio(msg.split("\\|")[1] + ": " + msg.split("\\|")[2], this.client);

                Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " aggiungo il messaggio al db");
                Server.getServer().writer.addMsg(msg);
            }
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg("[ ERR ] - Errore in ConnectionClient: " + e);
        }

        try { this.socket.close(); } catch (Exception e) { Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e); }
        Server.getServer().rimuoviClient(this.client);
    }
}