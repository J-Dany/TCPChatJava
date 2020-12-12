# TCP Server & Client
Piccolo progetto per una chat in java criptata.

## Dipendenze
- make
- mysql (command line)

## Aggiungere il database
Per aggiungere il database basta scrivere:
```bash
make db
```

## Come compilare ed avviare il Server
Date i seguenti comandi nella directory del progetto:
```bash
make compile
make run-server
```

## Console del programma Server
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

#### Thanks to: Daniele Castiglia, Francesco Borri, Sultan Zhunushov