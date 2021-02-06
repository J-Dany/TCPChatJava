package srcclient;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.json.JSONObject;

import srcclient.Messaggio.TipoMessaggio;
import srcclient.Messaggio.TipoRichiesta;

import java.net.Socket;
import java.security.PublicKey;
import java.io.OutputStreamWriter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.net.InetSocketAddress;

public class AppClient 
{
    /**
     * Codici di ritorno dell'applicazione
     */
    private static final int UTENTE_NON_RICONOSCIUTO = 4;
    private static final int ERRORE_NEL_FORM = 5;
    private static final int OUTPUT_STREAM_NON_INIZIALIZZATO = 3;

    /**
     * Grandezza buffer di ricezione.
     */
    private static final int GRANDEZZA_BUFFER = 8192;

    private static String ip;
    private static int port;

    private static PublicKey serverKey;

    /**
     * Socket di connessione del client
     */
    private static Socket s;
    
    /**
     * Output stream per mandare i dati
     */
    private static OutputStreamWriter outputStream;

    private static ChatModel model = null;
    private static ChatView chatUI = null;
    private static ChatController controller = null;

    private static String nome;
    
    public static void main(String[] args) 
    {
        Crypt.initialize();

        Thread.currentThread().setName("Thread-Listener-Messaggi");
        try 
        {
            Class.forName("com.google.common.hash.Hashing");

            // Mostra il form per inserire l'IP e la porta a cui collegarsi
            formIpPort();

            s = new Socket();
            InetSocketAddress server_address = new InetSocketAddress(ip, port);
            s.connect(server_address);

            // Mostra il form per autenticarsi
            formAutenticazione();

            try
            {
                outputStream = new OutputStreamWriter(s.getOutputStream());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(OUTPUT_STREAM_NON_INIZIALIZZATO);
            }

            byte[] buffer = new byte[GRANDEZZA_BUFFER];
            String msg = null;

            synchronized (s)
            {
                try
                {
                    int l = s.getInputStream().read(buffer);
                    msg = new String(buffer, 0, l, "UTF-8");
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    System.exit(0);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            JSONObject risposta = new JSONObject(msg);

            if (!risposta.getBoolean("Risultato"))
            {
                System.out.println("Utente non riconosciuto.");
                System.exit(UTENTE_NON_RICONOSCIUTO);
            }

            serverKey = Crypt.decodePublicKey(risposta.getString("Chiave"));

            try
            {
                model = new ChatModel(nome);
                chatUI = new ChatView(model);
                chatUI.buildApp();
                chatUI.show();
                chatUI.setNumeroUtentiConnessi(risposta.getInt("Utenti-Connessi"));

                controller = new ChatController(model);
                chatUI.setController(controller);

                Utente glob = new Utente("Globale");
                model.updateUtenti(glob);
                model.setUtenteCorrente(glob);

                for (Object n : risposta.getJSONArray("Lista-Utenti").toList())
                {
                    String nome = (String) n;
                    if (!nome.equals(AppClient.nome))
                    {
                        Utente u = new Utente(nome);
                        model.updateUtenti(u);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            while (true)
            {
                synchronized (s)
                {
                    try
                    {
                        int l = s.getInputStream().read(buffer);
                        msg = new String(buffer, 0, l, "UTF-8");
                    }
                    catch (StringIndexOutOfBoundsException e)
                    {
                        System.exit(0);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                risposta = new JSONObject(Crypt.decrypt(msg, Crypt.getPrivateKey()));
                TipoRichiesta tr = TipoRichiesta.valueOf(risposta.getString("Tipo-Richiesta"));

                switch (tr)
                {
                    case NUOVO_MESSAGGIO:
                        TipoMessaggio tm = TipoMessaggio.valueOf(risposta.getString("Tipo-Messaggio"));
                        switch (tm)
                        {
                            case INDIRIZZATO:
                                model.incrementaNumeroMessaggiDa(risposta.getString("Mittente"));
                                model.updateMessaggi(risposta.getString("Mittente"), new CasellaMessaggio(
                                    risposta.getString("Mittente"), 
                                    risposta.getString("Messaggio"), 
                                    risposta.getString("Data"), 
                                    risposta.getString("Time")
                                    )
                                );
                            break;
                            case PLAIN_TEXT:
                                model.incrementaNumeroMessaggiDa("Globale");
                                model.updateMessaggi("Globale", new CasellaMessaggio(
                                    risposta.getString("Nome"), 
                                    risposta.getString("Messaggio"), 
                                    risposta.getString("Data"), 
                                    risposta.getString("Time")
                                    )
                                );
                            break;
                        }
                    break;
                    case NUMERO_UTENTI:
                        Messaggio.TipoNumeroUtenti tnu = Messaggio.TipoNumeroUtenti.valueOf(risposta.getString("Tipo-Set-Numero"));
                        switch (tnu)
                        {
                            case CONNESSIONE:
                                chatUI.setNumeroUtentiConnessi(risposta.getInt("Numero"));

                                if (risposta.has("Nome-Utente"))
                                {
                                    Utente u = new Utente(risposta.getString("Nome-Utente"));
                                    model.updateUtenti(u);
                                }
                            break;
                            case DISCONNESSIONE:
                                model.removeUtente(risposta.getString("Nome"));
                                chatUI.setNumeroUtentiConnessi(risposta.getInt("Numero"));
                                chatUI.eliminaCasellaUtente(model.getCasella(risposta.getString("Nome")));
                            break;
                        }
                    break;
                    case NON_PUOI_INVIARE_MESSAGGI:
                        JOptionPane.showMessageDialog(chatUI.app, risposta.getString("Motivo"), "Non puoi inviare messaggi", JOptionPane.ERROR_MESSAGE);                                   
                    break;
                    case CHIUDI_CONNESSIONE:
                        JOptionPane.showMessageDialog(chatUI.app, "Il server ha mandato una richiesta di disconnessione perché si sta chiudendo. Chiudo l'applicazione", "Server chiuso", JOptionPane.ERROR_MESSAGE);
                        dispose(); 
                    break;
                }
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    public static synchronized void manda(String data)
    {
        try
        {
            outputStream.write(Crypt.encrypt(data, serverKey));
            outputStream.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void formIpPort()
    {
        FontUIResource font = new FontUIResource("Arial", FontUIResource.PLAIN, 16);
        JDialog server = new JDialog();
        server.setModal(true);
        server.setPreferredSize(new Dimension(500, 200));
        server.setLayout(new GridLayout(8, 1));
        server.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        server.add(Box.createHorizontalGlue());
        server.setTitle("Autenticazione");
        JLabel label = new JLabel("Inserisci IP (o dominio) e porta del server a cui collegarsi:");
        label.setFont(font);
        server.add(label);
        server.add(Box.createHorizontalGlue());
        JTextField ipTextField = new JTextField();
        ipTextField.setFont(font);
        JTextField inputPort = new JTextField();
        server.add(ipTextField);
        server.add(inputPort);
        JButton collegati = new JButton("Collegati");
        collegati.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent arg0) 
            {
                ip = ipTextField.getText();
                port = Integer.parseInt(inputPort.getText());
                server.dispose();
            }
        });
        collegati.setFont(font);
        inputPort.addKeyListener(new KeyListener()
        {
            @Override
            public void keyPressed(KeyEvent arg0) 
            {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) 
                {
                    collegati.doClick();
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) { }

            @Override
            public void keyTyped(KeyEvent arg0) { }
        });
        server.add(Box.createHorizontalGlue());
        server.add(collegati);
        server.pack();
        server.setVisible(true);
    }

    private static void formAutenticazione()
    {
        FontUIResource font = new FontUIResource("Arial", FontUIResource.PLAIN, 16);
        JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setPreferredSize(new Dimension(500, 200));
        dialog.setLayout(new GridLayout(8, 1));
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.add(Box.createHorizontalGlue());
        dialog.setTitle("Autenticazione");
        JLabel labelInfo = new JLabel("Per autenticarti ho bisogno di username e password:");
        labelInfo.setFont(font);
        dialog.add(labelInfo);
        dialog.add(Box.createHorizontalGlue());
        JTextField nomeUtente = new JTextField();
        nomeUtente.setFont(font);
        JPasswordField password = new JPasswordField();
        dialog.add(nomeUtente);
        dialog.add(password);
        JButton buttonLogin = new JButton("Autenticati");
        buttonLogin.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent arg0) 
            {
                String n = nomeUtente.getText();
                String p = new String(password.getPassword());
                
                try
                {
                    HashCode hash = Hashing.sha256().hashString(p, Charsets.UTF_8);
                    p = hash.toString();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    System.out.println("Errore nel prendere i valori dal form");
                    System.exit(ERRORE_NEL_FORM);
                }

                nome = n;

                try
                {
                    s.getOutputStream().write(Messaggio.autenticazione(nome, p).getBytes());
                    s.getOutputStream().flush();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                dialog.dispose();
            }
        });
        buttonLogin.setFont(font);
        password.addKeyListener(new KeyListener() 
        {
            @Override
            public void keyPressed(KeyEvent arg0) 
            { 
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) 
                {
                    buttonLogin.doClick();
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) { }

            @Override
            public void keyTyped(KeyEvent arg0) { }
            
        });
        dialog.add(Box.createHorizontalGlue());
        dialog.add(buttonLogin);
        dialog.pack();
        dialog.setVisible(true);
    }

    /**
     * Chiude tutte le risorse allocate per
     * l'applicazione, inclusi Controller/View/Model
     */
    public static void dispose()
    {
        try
        {
            s.shutdownOutput();
            s.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

        chatUI.dispose();
        model.dispose();
    }
}