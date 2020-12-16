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

    /**
     * Rappresenta il nome del database
     */
    private final String DB_NAME = "chat";

    /*private final String URL = "jdbc:mysql://localhost:" + PORT + ";"
        + "database=" + DB_NAME + ";"
        + "user=" + USER + ";"
        + "password=" + PASSWD + ";"
        + "encrypt=true;"
        + "trustServerCertificate=false;";*/
    private final String URL = "jdbc:mysql://localhost:" + PORT + "/" + DB_NAME;

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
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWD);
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

                this.insertMessage(user, datetime, msg);

                Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " messaggio aggiunto");
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
            else
            {
                Server.getServer().logger.add_msg("[ OK  ] - " + this.getName() + " query eseguita correttamente");
            }

            c.commit();

            return true;
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