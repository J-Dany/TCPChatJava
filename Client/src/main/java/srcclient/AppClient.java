package srcclient;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.json.JSONObject;
import java.net.Socket;
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

    private static String ip;
    private static int port;

    private static String nome;

    public static final int GRANDEZZA_BUFFER = 8192; // 1 << 13
    
    public static void main(String[] args) 
    {
        Thread.currentThread().setName("Thread-Listener-Messaggi");
        try 
        {
            // Mostra il form per inserire l'IP e la porta a cui collegarsi
            formIpPort();

            Socket s = new Socket();
            InetSocketAddress server_address = new InetSocketAddress(ip, port);
            s.connect(server_address);

            OutputStreamWriter outputStream = null;

            try
            {
                outputStream = new OutputStreamWriter(s.getOutputStream());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(OUTPUT_STREAM_NON_INIZIALIZZATO);
            }

            ChatController controller = new ChatController(s, outputStream);
        }
        catch (Exception e)
        {
            e.printStackTrace();
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

    public static void formAutenticazione()
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

                try
                {
                    /*s.getOutputStream().write(Messaggio.autenticazione(nome, p).getBytes());
                    s.getOutputStream().flush();*/
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
            /*s.shutdownOutput();
            s.close();*/
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

        /*chatUI.dispose();
        model.dispose();*/
    }
}