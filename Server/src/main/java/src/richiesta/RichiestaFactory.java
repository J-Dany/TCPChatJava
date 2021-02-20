package src.richiesta;

import org.json.JSONObject;
import src.Messaggio.TipoRichiesta;

public class RichiestaFactory
{
    public static Richiesta crea(JSONObject json)
    {
        TipoRichiesta tipoRichiesta = TipoRichiesta.valueOf(json.getString("Tipo-Richiesta"));

        switch (tipoRichiesta)
        {
            case AUTENTICAZIONE:
                return new RichiestaAutenticazione();
            case CHIUDI_CONNESSIONE:
                return new RichiestaChiudConnessione();
            case INVIO_MESSAGGIO:
                return new RichiestaInvioMessaggio();
        }

        return null;
    }
}