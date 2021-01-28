package src;

public class Config 
{
    /**
     * Rappresenta l'utente del database
     */
    public static final String USER = "chat";

    /**
     * Rappresenta la password dell'utente {USER} del db
     */
    public static final String PASSWD = "Abcd1234";

    /**
     * Rappresenta la porta in cui mysql sta in ascolto
     */
    private static final int PORT = 3306;

    /**
     * Rappresenta il nome del database
     */
    private static final String DB_NAME = "chat";

    //private static final String INDIRIZZO_DATABASE = System.getenv("IP_DB");

    /**
     * Url per la connessione al database
     */
    public static final String URL = "jdbc:mysql://localhost:" + PORT + "/" + DB_NAME;
}