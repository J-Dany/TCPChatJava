package srcclient;

import java.io.IOException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import org.json.JSONObject;

public class ChatView {
    /**
     * Grandezza dell'applicazione
     */
    public final int HEIGHT = 720;
    public final int WIDTH = 1280;

    public JFrame app;
    private JPanel panelUtenti;
    private JTextField input, utentiConnessi;
    private Font font = new FontUIResource("Noto Sans", Font.PLAIN, 14);
    private Font fontInviaMessaggio = new FontUIResource("Noto Sans", Font.PLAIN, 18);
    private JButton buttonInvia;
    private ChatModel model;

    /**
     * Ctor
     * 
     * @param model, il Model dell'applicazione
     * @throws IOException
     */
    public ChatView(ChatModel model) throws IOException {
        this.app = new JFrame("Chat");
        this.model = model;
        model.addListener(new PropertyChangeListener() 
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                System.out.println("Qualcosa è cambiato sul model");
                System.out.println(evt);
            }   
        });
    }

    /**
     * Prepara l'interfaccia grafica dell'App
     */
    public void buildApp() 
    {
        this.app.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.app.setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setFont(font);
        JMenuItem quit = new JMenuItem("Quit");
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) 
            {
                try 
                {
                    JSONObject closeRequest = new JSONObject();
                    closeRequest.put("Tipo-Richiesta", "Chiudi-Connessione");

                    AppClient.manda(closeRequest.toString());
                    AppClient.dispose();
                } 
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
            }
        });
        quit.setFont(font);
        file.add(quit);
        JMenu info = new JMenu("Info");
        info.setFont(font);
        JMenuItem about = new JMenuItem("About");
        about.setFont(font);
        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JDialog dialog = new JDialog(app, "Info");
                dialog.setLayout(new GridLayout(2, 1));

                JLabel labelCreatore = new JLabel("Creatore: Daniele Castiglia");
                labelCreatore.setFont(fontInviaMessaggio);
                dialog.add(labelCreatore);

                JLabel labelLinkGithub = new JLabel("https://github.com/J-Dany/TCPChatJava");
                labelLinkGithub.setFont(fontInviaMessaggio);
                dialog.add(labelLinkGithub);

                dialog.setSize(400, 200);
                dialog.setVisible(true);
            }
        });
        menuBar.add(file);
        menuBar.add(info);
        info.add(about);
        this.app.setJMenuBar(menuBar);

        this.app.addWindowListener(new WindowListener() {
            @Override
            public void windowActivated(WindowEvent arg0) { }

            @Override
            public void windowClosed(WindowEvent arg0) { }

            @Override
            public void windowClosing(WindowEvent arg0) {
                try 
                {
                    JSONObject closeRequest = new JSONObject();
                    closeRequest.put("Tipo-Richiesta", "Chiudi-Connessione");

                    AppClient.manda(closeRequest.toString());
                    AppClient.dispose();
                }
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void windowDeactivated(WindowEvent arg0) { }

            @Override
            public void windowDeiconified(WindowEvent arg0) { }

            @Override
            public void windowIconified(WindowEvent arg0) { }

            @Override
            public void windowOpened(WindowEvent arg0) { }
        });

        panelUtenti = new JPanel();
        panelUtenti.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panelUtenti.setLayout(new BoxLayout(panelUtenti, BoxLayout.PAGE_AXIS));

        JScrollPane scrollUtenti = new JScrollPane(panelUtenti);
        this.app.add(scrollUtenti, BorderLayout.LINE_START);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        utentiConnessi = new JTextField();
        utentiConnessi.setBorder(null);
        utentiConnessi.setFont(font);
        utentiConnessi.setEditable(false);
        utentiConnessi.setPreferredSize(new Dimension(WIDTH, 20));
        panel.add(utentiConnessi);
        this.app.add(panel, BorderLayout.PAGE_START);

        JPanel panelInput = new JPanel();
        BoxLayout layoutInput = new BoxLayout(panelInput, BoxLayout.LINE_AXIS);
        panelInput.setLayout(layoutInput);

        this.input = new JTextField();
        this.input.setFont(fontInviaMessaggio);
        panelInput.add(input);

        JButton buttonInviaMessaggio = new JButton("Invia");
        buttonInviaMessaggio.setPreferredSize(new Dimension(75, 40));
        buttonInviaMessaggio.setFont(fontInviaMessaggio);
        /*buttonInviaMessaggio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) 
            {
                try 
                {
                    String msg = input.getText();
                    if (msg.length() > 256) 
                    {
                        JOptionPane.showMessageDialog(app, "Il messaggio non deve superare i 256 caratteri",
                                "Messaggio troppo lungo", JOptionPane.ERROR_MESSAGE);
                        return;
                    } 
                    else if (msg.length() == 0) 
                    {
                        return;
                    }

                    String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));

                    System.out.println("Robe: " + nome + ", " + msg + ", " + data + ", " + time);

                    JSONObject json = new JSONObject();
                    json.put("Tipo-Richiesta", "Invio-Messaggio");
                    json.put("Data", data);
                    json.put("Time", time);
                    json.put("Nome", nome);
                    json.put("Messaggio", msg);

                    if (!AppClient.getNomeUtenteCorrente().equals("Globale"))
                    {
                        json.put("Tipo-Messaggio", "Per");
                        json.put("Per", AppClient.getNomeUtenteCorrente());
                    }
                    else
                    {
                        json.put("Tipo-Messaggio", "Plain-Text");
                    }

                    System.out.println(json);

                    AppClient.getUtenteCorrente().aggiungiMessaggio(new CasellaMessaggio("Tu", msg, data, time));
                    AppClient.manda(json.toString());

                    input.setText("");
                } 
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
            }
        });*/
        buttonInviaMessaggio.setBorder(null);

        this.buttonInvia = buttonInviaMessaggio;

        this.input.addKeyListener(new KeyListener() 
        {
            @Override
            public void keyPressed(KeyEvent arg0) 
            {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) 
                {
                    buttonInviaMessaggio.doClick();
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) { }

            @Override
            public void keyTyped(KeyEvent arg0) { }
        });

        panelInput.add(buttonInviaMessaggio);

        this.app.add(panelInput, BorderLayout.PAGE_END);
    }

    /**
     * Ritorna il messaggio scritto all'interno della JTextField
     * e poi lo elimina
     * 
     * @return String
     */
    public String getInput ()
    {
        String text = input.getText();
        input.setText("");
        return text;
    }

    public JButton getButtonInvia()
    {
        return buttonInvia;
    }

    /**
     * Visualizza l'applicazione
     */
    public void show() 
    {
        this.app.pack();
        this.app.setResizable(false);
        this.app.setVisible(true);
    }

    /**
     * Funzione richiamata soltanto quando si connette
     * un nuovo utente, ridisegna la lista degli utenti
     */
    private void repaintListaUtenti()
    {
        panelUtenti.revalidate();
        panelUtenti.repaint();
    }

    /**
     * Elimina la casella dell'utente selezionato
     * @param c, la casella da eliminare
     */
    public void eliminaCasellaUtente(CasellaUtente c)
    {
        panelUtenti.remove(c);
        this.repaintListaUtenti();
    }

    public void aggiungiTextPaneChatCorrente(Utente u)
    {
        this.app.add(u.getScrollPane(), BorderLayout.CENTER);
        this.app.revalidate();
        this.app.repaint();
    }

    /**
     * Aggiunge l'immagine passata alla JTextPane
     * (Ancora non implementata)
     * 
     * @param nome, l'utente che l'ha mandata
     * @param img, l'immagine da aggiungere
     */
    public void aggiungiImmagine(String nome, ImageIcon img) { }

    /**
     * Incrementa il numero dei messaggi mandati dal mittente ancora non letti
     * @param c, la casella del mittente
     */
    public void incrementaNumeroMessaggiDa(CasellaUtente c)
    {
        int n = c.getNumeroMessaggi() + 1;

        c.setNumeroMessaggi("" + n);
    }

    /**
     * Setta la JTextField del numero degli utenti connessi al numero passato come parametro
     * 
     * @param numero, numero degli utenti connessi dato dal server
     */
    public void setNumeroUtentiConnessi(int numero)
    {
        try
        {
            if (numero == 0)
            {
                this.utentiConnessi.setText("Solo te sei connesso!");
            }
            else if (numero == 1)
            {
                this.utentiConnessi.setText("1 utente connesso!");
            }
            else 
            {
                this.utentiConnessi.setText(numero + " utenti connessi!");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}