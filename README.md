# TCP Server & Client
Piccolo progetto per una chat in java criptata.

## Dipendenze
- gradle
- mysql
- make

## Installazione (Linux)
```bash
make
sudo make install
```

## Console del programma Server
**N.B.**: Ogni client deve essere registrato prima che quest'ultimo possa usare la chat.
Per registrare un nuovo client usa il comando *aggiungi-utente*

Quando avviate un server, nella console apparirà:
```bash
?
```
Questo simbolo sta a significare che potete dare dei comandi al server che lui eseguirà.
I comandi per ora disponibili, con alias, sono:
- **exit** | **stop** (v. corta: *e*, *s*)
    - Interrompe il server
- **show-connected-client** (v. corta: *show-cc*, *scc*)
    - Stampa una lista dei client in questo momento connessi
- **history**
    - Stampa la lista dei comandi dati al server
- **ban** {ip}
    - Banna l'indirizzo IP passato
- **numero-errori** (v. corta: *n-err*)
    - Stampa a video il numero degli errori registrati
- **error** [indexErrore] (v. corta: *err*)
    - Stampa a video l'errore richiesto. Se non viene specificato l'index dell'errore verrà stampato l'ultimo
- **aggiungi-utente** {nome} {passwordInChiaro} (v. corta: *a-utente*, *au*)
    - Aggiunge nuovo utente al database
- **delete-user** {nomeUtente} (v. corta: *du*)
    - Elimina un utente dal database
- **manda-msg** | **send-msg** {msg}
    - Manda il messaggio a tutti i client connessi (il messaggio deve essere lungo max. 256 caratteri)
- **utenti-registrati** (v. corta: *ur*)
    - Stampa una lista di utenti registrati nella chat
- **svuota-db-messaggi** (v. corta: *svuota-db-msg*, *sdbm*)
    - Elimina tutti i messaggi fino ad ora salvati
- **n-message-by** {nomeClient} (v. corta: *n-msg-by*, *nmb*)
    - Stampa il numero dei messaggi che {nomeClient} ha mandato oggi

## Log del Server
Eh si, esistono i log del Server :).
Sotto *Windows* il file di log si trova in questo percorso:
```bash
C:/Chat-Log/
```
mentre su Linux il file di log si trova sotto:
```bash
/var/log/chat-log/
```

## Codice di errore Client
Se non riesci a connetterti con il client al server, puoi controllare
il codice di ritorno del client digitando (su Linux):
```bash
echo $?
```

Ecco una tabella dei codici di errore:
- 1 => **Connessione rifiutata**, il server potrebbe essere spento
- 2 => **IO Exception**, c'è stato un errore con il socket
- 3 => **Stream di output non inizializzato**, non è stato possibile inizializzare lo stream di output per mandare i messaggi
- 4 => **Utente non riconosciuto**, probabilmente nel Server non sei stato aggiunto nel database
- 5 => **Errore nel prendere i dati dal form**

#### Thanks to: Daniele Castiglia
