package Server.src.main.java.src;

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

    /**
     * Restituisce tutti i valori della tabella
     * @return ResultSet, rappresentante tutti i valori della tabella
     */
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

    /**
     * Rimuove ogni record dalla tabella
     */
    public void delete()
    {
        Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " elimino tutti i record da " + this.tableName);
    
        try
        {
            Connection connection = DatabaseConnection.getConnection();

            String query = "DELETE FROM " + this.tableName;

            Statement stmt = connection.createStatement();

            stmt.executeUpdate(query);

            Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " query eseguita correttamente");
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
        }
    }

    /**
     * Rimuove soltanto il record con primary key uguale a {value}
     * @param value, valore della primary key del record da eliminare
     */
    public void deleteOnce(String value)
    {
        Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " eseguo query per eliminare un record da " + this.tableName);

        try
        {
            Connection connection = DatabaseConnection.getConnection();

            String query = "DELETE FROM " + this.tableName + " WHERE " + this.primaryKey + " = '" + value + "'";
            Statement stmt = connection.createStatement();
    
            stmt.executeUpdate(query);

            Server.getServer().logger.add_msg("[ OK  ] - " + Thread.currentThread().getName() + " query eseguita correttamente");
        }
        catch (Exception e)
        {
            Server.getServer().logger.add_msg("[ ERR ] - " + Thread.currentThread().getName() + " " + e);
        }
    }

    /**
     * Inserisce un solo record nel database
     * @param fields, i campi della tabella
     * @param values, i valori dei campi
     */
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