package srcclient.risposta;

import org.json.JSONObject;
import srcclient.Messaggio.TipoRisposta;

public class RispostaFactory 
{
    public static Risposta creaRisposta(JSONObject json)
    {
        TipoRisposta risposta = TipoRisposta.valueOf(json.getString("Tipo-Risposta"));

        switch (risposta)
        {
            case AUTENTICAZIONE:
                
            case CHIUDI_CONNESSIONE:
                return new RispostaChiudiConnessione();
            case NUOVO_MESSAGGIO:
                return new RispostaNuovoMessaggio();
            case NUMERO_UTENTI:
                return new RispostaNumeroUtenti();
            case NON_PUOI_INVIARE_MESSAGGI:
                return new RispostaNonPuoiInviareMessaggio();
        }

        return null;
    }
}