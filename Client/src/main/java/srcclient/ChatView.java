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
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import org.json.JSONObject;

public class ChatView 
{
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
    private ChatController controller;

    /**
     * Ctor
     * 
     * @param model il Model dell'applicazione
     * @throws IOException
     */
    public ChatView(ChatModel model) throws IOException {
        this.app = new JFrame("Chat");
        this.model = model;
        model.addListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                switch (evt.getPropertyName()) {
                    case "Imposta-Chat-Corrente":
                        aggiungiTextPaneChatCorrente((Utente) evt.getNewValue());
                        break;
                    case "Nuovo-Utente":
                        aggiungiNuovoUtente(
                            new CasellaUtente(
                                ((Utente) evt.getNewValue()).getNome(), controller)
                            );
                        break;
                }
            }
        });
    }

    public void setController(ChatController controller) {
        this.controller = controller;
    }

    /**
     * Prepara l'interfaccia grafica dell'App
     */
    public void buildApp() {
        this.app.setBackground(new ColorUIResource(0f, 0f, 0f));
        this.app.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.app.setLayout(new BorderLayout());

        this.app.addWindowListener(new WindowListener() {
            @Override
            public void windowActivated(WindowEvent arg0) {
            }

            @Override
            public void windowClosed(WindowEvent arg0) {
            }

            @Override
            public void windowClosing(WindowEvent arg0) {
                try {
                    JSONObject closeRequest = new JSONObject();
                    closeRequest.put("Tipo-Richiesta", "Chiudi-Connessione");

                    AppClient.manda(closeRequest.toString());
                    AppClient.dispose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void windowDeactivated(WindowEvent arg0) {
            }

            @Override
            public void windowDeiconified(WindowEvent arg0) {
            }

            @Override
            public void windowIconified(WindowEvent arg0) {
            }

            @Override
            public void windowOpened(WindowEvent arg0) {
            }
        });

        panelUtenti = new JPanel();
        panelUtenti.setBackground(new ColorUIResource(0.1f, 0.1f, 0.1f));
        panelUtenti.setBorder(null);
        panelUtenti.setLayout(new BoxLayout(panelUtenti, BoxLayout.PAGE_AXIS));

        JScrollPane scrollUtenti = new JScrollPane(panelUtenti);
        scrollUtenti.setBorder(null);
        this.app.add(scrollUtenti, BorderLayout.LINE_START);

        UIManager.put("TextField.inactiveBackground", new ColorUIResource(0.1f, 0.1f, 0.1f));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        utentiConnessi = new JTextField();
        utentiConnessi.setBorder(null);
        utentiConnessi.setFont(font);
        utentiConnessi.setForeground(new ColorUIResource(215, 255, 137));
        utentiConnessi.setPreferredSize(new Dimension(WIDTH, 20));
        utentiConnessi.setEditable(false);
        panel.add(utentiConnessi);
        panel.setBackground(new ColorUIResource(0.1f, 0.1f, 0.1f));
        this.app.add(panel, BorderLayout.PAGE_START);

        JPanel panelInput = new JPanel();
        panelInput.setBackground(new ColorUIResource(0.1f, 0.1f, 0.1f));
        BoxLayout layoutInput = new BoxLayout(panelInput, BoxLayout.LINE_AXIS);
        panelInput.setLayout(layoutInput);

        this.input = new JTextField();
        this.input.setFont(fontInviaMessaggio);
        this.input.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.input.setBackground(new ColorUIResource(0.1f, 0.1f, 0.1f));
        this.input.setForeground(new ColorUIResource(238, 238, 238));
        panelInput.add(input);

        JButton buttonInviaMessaggio = new JButton();
        Image img = new ImageIcon(getClass().getResource("/send.png")).getImage().getScaledInstance(32, 32,
                java.awt.Image.SCALE_SMOOTH);
        buttonInviaMessaggio.setIcon(new ImageIcon(img));
        buttonInviaMessaggio.setOpaque(true);
        buttonInviaMessaggio.setBackground(new Color(0.1f, 0.1f, 0.1f));
        buttonInviaMessaggio.setForeground(new ColorUIResource(238, 238, 238));
        buttonInviaMessaggio.setPreferredSize(new Dimension(75, 40));
        buttonInviaMessaggio.addActionListener(new ActionListener() 
        {
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

                    controller.inviaMessaggio(msg);

                    input.setText("");
                } 
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
            }
        });
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
     * @param c la casella da eliminare
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

    public void aggiungiNuovoUtente (CasellaUtente c)
    {
        panelUtenti.add(c);
        repaintListaUtenti();
    }

    /**
     * Aggiunge l'immagine passata alla JTextPane
     * (Ancora non implementata)
     * 
     * @param nome l'utente che l'ha mandata
     * @param img l'immagine da aggiungere
     */
    public void aggiungiImmagine(String nome, ImageIcon img) { }

    /**
     * Incrementa il numero dei messaggi mandati dal mittente ancora non letti
     * @param c la casella del mittente
     */
    public void incrementaNumeroMessaggiDa(CasellaUtente c)
    {
        int n = c.getNumeroMessaggi() + 1;

        c.setNumeroMessaggi("" + n);
    }

    /**
     * Setta la JTextField del numero degli utenti connessi al numero passato come parametro
     * 
     * @param numero numero degli utenti connessi dato dal server
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

    /**
     * Rilascia le risorse allocate
     * per l'interfaccia grafica
     */
    public void dispose()
    {
        this.app.dispose();
    }
}