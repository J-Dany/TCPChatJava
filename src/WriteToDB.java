package src;

import java.sql.*;
import java.util.Queue;
import java.util.LinkedList;

public class WriteToDB extends Thread
{
    /**
     * Rappresenta l'utente del database
     */
    private final String USER = "chat";

    /**
     * Rappresenta la password dell'utente {USER} del db
     */
    private final String PASSWD = "Abcd1234";

    /**
     * Rappresenta la porta in cui mysql sta in ascolto
     */
    private final int PORT = 3306;

    private final String URL = "jdbc:sqlserver://localhost:" + PORT + ";"
        + "database=AdventureWorks;"
        + "user=" + USER + ";"
        + "password=" + PASSWD + ";"
        + "encrypt=true;"
        + "trustServerCertificate=false;"
        + "loginTimeout=30;";

    private Queue<String> msgs;

    public WriteToDB(String name) throws SQLException
    {
        super(name);
        this.msgs = new LinkedList<>();
    }

    private Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(URL);
    }

    public synchronized void addMsg(String msg)
    {
        this.msgs.add(msg);
    }

    @Override
    public void run()
    {
        try
        {
            synchronized (this)
            {
                while (!Thread.currentThread().isInterrupted())
                {
                    while (this.msgs.size() == 0)
                    {
                        wait();
                    }
    
                    String[] obj = this.msgs.poll().split("|");

                    String user = obj[1];
                    String datetime = obj[0];
                    String msg = obj[2];
    
                    this.insertMessage(user, datetime, msg);
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
            String data = datetime.split(" ")[0];
            String time = datetime.split(" ")[1];

            Server.getServer().logger.add_msg("[ OK  ] - " + this.getName() + " Stabilisco la connessione con il db");
                c = this.getConnection();
            Server.getServer().logger.add_msg("[ OK  ] - " + this.getName() + " Connessione al db stabilita");

            c.setAutoCommit(false);

            String query = "INSERT INTO messaggi(message, user, `date`, `time`) " + 
                "VALUES(" +
                    "'" + msg + "', '" +
                    user + "', '" +
                    data + "', '" +
                    time +
                "')";

            Server.getServer().logger.add_msg("[ OK  ] - " + this.getName() + " query: " + query);

            PreparedStatement preparedStatement = c.prepareStatement(query);
        
            if (!preparedStatement.execute())
            {
                Server.getServer().logger.add_msg("[ ERR ] - " + this.getName() + " errore nell'eseguire la query di inserimento");
            }

            Server.getServer().logger.add_msg("[ OK  ] - " + this.getName() + " query eseguita correttamente");

            c.commit();
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg("[ ERR ] - " + this.getName() + " " + e);
        }

        try { c.rollback(); } catch (SQLException e) { Server.getServer().logger.add_msg("[ ERR ] - " + this.getName() + " " + e); }
        Server.getServer().logger.add_msg("[ ERR ] - " + this.getName() + " Errore nell'inserimento del messaggio nel db");
        return false;
    }
}