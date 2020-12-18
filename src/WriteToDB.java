package src;

import java.sql.*;
import java.util.Queue;
import java.util.LinkedList;

public class WriteToDB extends Thread
{
    /**
     * Rappresenta la coda dei messaggi arrivati
     */
    private Queue<String> msgs;

    public WriteToDB(String name) throws SQLException
    {
        super(name);
        this.msgs = new LinkedList<>();
    }

    private Connection getConnection() throws SQLException
    {
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
        }
        return DriverManager.getConnection(Config.URL, Config.USER, Config.PASSWD);
    }

    public synchronized void addMsg(String msg)
    {
        Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " arrivato nuovo messaggio");
        this.msgs.add(msg);
    }

    @Override
    public void run()
    {
        try
        {
            while (!Thread.currentThread().isInterrupted())
            {
                while (this.msgs.size() == 0 && !Thread.currentThread().isInterrupted()) { }
                
                String[] obj = this.msgs.poll().split("\\|");

                String user = obj[1];
                String datetime = obj[0];
                String msg = obj[2];

                Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " aggiungo un nuovo messaggio al db");

                if (this.insertMessage(user, datetime, msg))
                {
                    Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " messaggio aggiunto");
                }
                else
                {
                    Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " non e' stato possibile aggiungere il messaggio al db da parte di " + user + " con testo: " + msg);
                }
            }
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
        }
    }

    public boolean insertMessage(String user, String datetime, String msg)
    {
        Connection c = null;
        try
        {
            String[] objs = datetime.split(" ");
            String data = objs[0];
            String time = objs[1];

            Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " Stabilisco la connessione con il db");
                c = this.getConnection();
            Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " Connessione al db stabilita");

            c.setAutoCommit(false);

            String query = "INSERT INTO messaggi(message, user, `date`, `time`) " + 
                "VALUES("
                +    "?," 
                +    "?,"
                +    "?,"
                +    "?"  
                + ")";

            Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " query: " + query);

            PreparedStatement preparedStatement = c.prepareStatement(query);

            preparedStatement.setString(1, msg);
            preparedStatement.setString(2, user);
            preparedStatement.setString(3, data);
            preparedStatement.setString(4, time);
        
            if (preparedStatement.execute())
            {
                Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " errore nell'eseguire la query di inserimento");
            }
            else
            {
                Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " query eseguita correttamente");
            }

            c.commit();

            return true;
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
        }

        try { c.rollback(); } catch (SQLException e) { Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e); }
        Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " Errore nell'inserimento del messaggio nel db");
        return false;
    }
}