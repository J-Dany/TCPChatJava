package src.richiesta;

import java.net.Socket;
import org.json.JSONObject;
import src.Client;
import src.eccezioni.*;

public interface Richiesta 
{
    public void rispondi(JSONObject json, Client c, Socket s) throws AutenticazioneFallita, ChiudiConnessione;
}