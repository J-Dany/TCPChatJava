package src.eccezioni;

public class ChiudiConnessione extends Exception
{
    private static final long serialVersionUID = -5745333999030289957L;
    
    public ChiudiConnessione(String msg)
    {
        super(msg);
    }
}