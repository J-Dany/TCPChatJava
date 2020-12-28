package src;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseTable 
{
    private String tableName;
    private String primaryKey;

    public DatabaseTable (String tableName, String primaryKey)
    {
        this.primaryKey = primaryKey;
        this.tableName = tableName;
    }

    public ResultSet findAll()
    {
        try
        {
            Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " mi connetto al database");
                Connection connection = DatabaseConnection.getConnection();
            Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " mi connetto al database");

            Statement stmt = connection.createStatement();

            String query = "SELECT * FROM " + tableName;

            Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " eseguo la query per prendere tutti i dati dalla tabella");
                ResultSet result = stmt.executeQuery(query);
            Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " query eseguita correttamente");

            return result;
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
        }

        return null;
    }

    public void deleteOnce(String user)
    {
        Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " eseguo query per eliminare un record da " + this.tableName);

        try
        {
            Connection connection = DatabaseConnection.getConnection();

            String query = "DELETE FROM " + this.tableName + " WHERE " + this.primaryKey + " = '" + user + "'";
            Statement stmt = connection.createStatement();
    
            stmt.executeUpdate(query);

            Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " query eseguita correttamente");
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
        }
    }

    public void insertOnce(String[] fields, Object[] values)
    {
        Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " inserisco un nuovo record nella tabella " + this.tableName);
        
        String query = "INSERT INTO " + this.tableName + " (";

        for (String field : fields)
        {
            query += field + ", ";
        }

        query = query.substring(0, query.length() - 2) + ") VALUES(";

        for (Object value : values)
        {
            if (value instanceof String)
            {
                query += "'" + value + "', ";
            }
            else if (value instanceof Integer)
            {
                query += value + ", ";
            }
        }

        query = query.substring(0, query.length() - 2) + ")";

        try
        {
            Connection connection = DatabaseConnection.getConnection();

            Statement stmt = connection.createStatement();
    
            stmt.executeUpdate(query);

            Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " query eseguita correttamente");
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
        }
    }
}