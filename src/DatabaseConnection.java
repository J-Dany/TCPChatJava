package src;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection
{
    private static Connection connection;

    public static Connection getConnection() throws SQLException
    {
        if (connection == null || connection.isClosed())
        {
            connection = DriverManager.getConnection(Config.URL, Config.USER, Config.PASSWD);
        }

        return connection;
    }
}