package src.eccezioni;

public class AutenticazioneFallita extends Exception
{
    private static final long serialVersionUID = -6121601655174447853L;
 
    public AutenticazioneFallita(String msg)
    {
        super(msg);
    }
}