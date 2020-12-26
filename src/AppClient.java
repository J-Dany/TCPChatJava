package src;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.json.JSONObject;
import java.net.Socket;
import java.io.OutputStreamWriter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;

public class AppClient 
{
    /**
     * Codici di ritorno dell'applicazione
     */
    private static final int CONNESSIONE_RIFIUTATA = 1;
    private static final int IO_EXCEPTION = 2;
    private static final int OUTPUT_STREAM_NON_ISTANZIATO = 3;
    private static final int UTENTE_NON_RICONOSCIUTO = 4;

    /**
     * Nome del client
     */
    private static String nome;

    /**
     * Grandezza buffer di ricezione.
     * Possiamo ricevere un'immagine all'incirca
     * di 67 MB
     */
    private static final int GRANDEZZA_BUFFER = 1 << 26;

    public static void main(String[] args) 
    {
        try 
        {
            Socket socket = new Socket();
            InetSocketAddress server_address = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
            socket.connect(server_address);

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

                    JSONObject auth = new JSONObject();
                    auth.put("Tipo-Richiesta", "Autenticazione");
                    auth.put("Nome", n);
                    auth.put("Password", p);
                    
                    nome = n;

                    try
                    {
                        socket.getOutputStream().write(auth.toString().getBytes());
                        socket.getOutputStream().flush();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    dialog.dispose();
                }
            });
            buttonLogin.setFont(font);
            dialog.add(Box.createHorizontalGlue());
            dialog.add(buttonLogin);
            dialog.pack();
            dialog.setVisible(true);

            ChatUI chat = null;

            while (true)
            {
                byte[] buffer = new byte[GRANDEZZA_BUFFER];
                String msg = null;

                synchronized (socket)
                {
                    try
                    {
                        int l = socket.getInputStream().read(buffer);
                        msg = new String(buffer, 0, l, "UTF8");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                JSONObject risposta = new JSONObject(msg);

                switch (risposta.getString("Tipo-Richiesta"))
                {
                    case "Autenticazione":
                        if (!risposta.getBoolean("Risultato"))
                        {
                            System.out.println("Utente non riconosciuto.");
                            System.exit(UTENTE_NON_RICONOSCIUTO);
                        }

                        try
                        {
                            OutputStreamWriter write = new OutputStreamWriter(socket.getOutputStream(), "UTF8");
                            
                            String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); 
                            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
                            
                            JSONObject nuovoMessaggio = new JSONObject();
                            nuovoMessaggio.put("Tipo-Richiesta", "Invio-Messaggio");
                            nuovoMessaggio.put("Messaggio", "si è connesso!");
                            nuovoMessaggio.put("Nome", nome);
                            nuovoMessaggio.put("Data", data);
                            nuovoMessaggio.put("Time", time);

                            write.write(nuovoMessaggio.toString());
                            write.flush();

                            chat = new ChatUI(socket, nome);
                            chat.prepareApp();
                            chat.show();
                            chat.setNumeroUtentiConnessi(risposta.getInt("Utenti-Connessi"));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    break;
                    case "Nuovo-Messaggio":
                        switch (risposta.getString("Tipo-Messaggio"))
                        {
                            case "Plain-Text":
                                String nomeClient = risposta.getString("Nome");
                                chat.aggiungiMessaggio(nomeClient, risposta.getString("Messaggio"));
                            break;
                            case "Immagine":

                            break;
                        }
                    break;
                    case "Numero-Utenti":
                        chat.setNumeroUtentiConnessi(risposta.getInt("Numero"));
                    break;
                    case "Non-Puoi-Inviare-Messaggi":
                        JOptionPane.showMessageDialog(chat.app, risposta.getString("Motivo"), "Non puoi inviare messaggi", JOptionPane.ERROR_MESSAGE);                                   
                    break;
                    case "Chiudi-Connessione":
                        JOptionPane.showMessageDialog(chat.app, "Il server ha mandato una richiesta di disconnessione perché si sta chiudendo. Chiudo l'applicazione", "Server chiuso", JOptionPane.ERROR_MESSAGE);
                        socket.close();
                        System.exit(0);    
                    break;
                }
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    static class ChatUI 
    {
        /**
         * Grandezza dell'applicazione
         */
        public final int HEIGHT = 480;
        public final int WIDTH = 853;

        private HashMap<String, Color> utenteColore;
        private OutputStreamWriter writer;
        private Socket socket;
        public JFrame app;
        private JTextField input, utentiConnessi;
        private Font font = new FontUIResource("Noto Sans", Font.PLAIN, 14);
        private Font fontTextArea = new FontUIResource("Caladea", Font.PLAIN, 18);
        private Font fontInviaMessaggio = new FontUIResource("Noto Sans", Font.PLAIN, 18);
        private JTextPane textArea;
        private StyledDocument doc;
        private String nome;
        private JScrollPane scrollPaneTextArea;
        private JScrollBar s;
        private ArrayList<ColorUIResource> colors = new ArrayList<>();

        public ChatUI(Socket socket, String nome) throws IOException 
        {
            this.utenteColore = new HashMap<>();
            this.nome = nome;
            this.app = new JFrame("Chat");
            if (socket != null) 
            {
                synchronized (socket)
                {
                    this.socket = socket;
                    this.writer = new OutputStreamWriter(this.socket.getOutputStream(), "UTF8");
                }
            }

            for (int i = 0; i < 256; ++i) 
            {
                this.colors.add(new ColorUIResource(
                    new Random().nextInt(180) + 70, 
                    new Random().nextInt(100),
                    new Random().nextInt(180) + 70));
            }
        }

        public void aggiungiUtenteColore(String nome, Color color) 
        {
            this.utenteColore.put(nome, color);
        }

        public HashMap<String, Color> getUtenteColore() 
        {
            return this.utenteColore;
        }

        public void prepareApp() 
        {
            this.app.setSize(WIDTH, HEIGHT);
            this.app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JMenuBar menuBar = new JMenuBar();
            JMenu file = new JMenu("File");
            file.setFont(font);
            JMenuItem quit = new JMenuItem("Quit");
            quit.addActionListener(new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent arg0) 
                {
                    try 
                    {
                        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); 
                        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));

                        JSONObject inviaAvvenutaDisconnessione = new JSONObject();
                        inviaAvvenutaDisconnessione.put("Tipo-Richiesta", "Invio-Messaggio");
                        inviaAvvenutaDisconnessione.put("Tipo-Messaggio", "Plain-Text");
                        inviaAvvenutaDisconnessione.put("Nome", nome);
                        inviaAvvenutaDisconnessione.put("Messaggio", "si è disconnesso!");
                        inviaAvvenutaDisconnessione.put("Data", data);
                        inviaAvvenutaDisconnessione.put("Time", time);

                        synchronized (writer)
                        {
                            writer.write(inviaAvvenutaDisconnessione.toString());
                            writer.flush();
                        }
                        
                        JSONObject closeRequest = new JSONObject();
                        closeRequest.put("Tipo-Richiesta", "Chiudi-Connessione");

                        synchronized (writer)
                        {
                            writer.write(closeRequest.toString());
                            writer.flush();
                            writer.close();
                        }

                        System.exit(0);
                    } 
                    catch (Exception e) 
                    {
                        e.printStackTrace();
                    }
                }
            });
            quit.setFont(font);
            file.add(quit);
            JMenu caricaImmagine = new JMenu("Immagine");
            caricaImmagine.setFont(font);
            JMenuItem buttonCaricaImmagine = new JMenuItem("Carica...");
            buttonCaricaImmagine.setFont(font);
            buttonCaricaImmagine.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0) 
                {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.showOpenDialog(app);

                    File f = fileChooser.getSelectedFile();

                    try
                    {
                        FileInputStream fileInputStream = new FileInputStream(f);

                        if (f.length() > GRANDEZZA_BUFFER)
                        {
                            JOptionPane.showMessageDialog(app, "L'immagine è troppo grande!", "Errore immagine", JOptionPane.ERROR_MESSAGE);
                        }
                        else
                        {
                            String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); 
                            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));

                            JSONObject mandaImmagine = new JSONObject();
                            mandaImmagine.put("Tipo-Richiesta", "Invio-Messaggio");
                            mandaImmagine.put("Tipo-Messaggio", "Immagine");
                            mandaImmagine.put("Data", data);
                            mandaImmagine.put("Time", time);
                            mandaImmagine.put("Nome", nome);
                            
                            byte[] imageData = new byte[(int) f.length()];
                            fileInputStream.read(imageData);
                            String msg = Base64.getEncoder().encodeToString(imageData);

                            mandaImmagine.put("Messaggio", msg);

                            aggiungiImmagine(nome, msg);

                            synchronized (writer)
                            {
                                writer.write(mandaImmagine.toString());
                                writer.flush();
                            }
                        }

                        fileInputStream.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            caricaImmagine.add(buttonCaricaImmagine);
            JMenu info = new JMenu("Info");
            info.setFont(font);
            JMenuItem about = new JMenuItem("About");
            about.setFont(font);
            about.addActionListener(new ActionListener()
            {
				@Override
                public void actionPerformed(ActionEvent arg0) 
                {
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
            JMenu n = new JMenu("Sei loggato come: " + nome);
            n.setFont(font);
            menuBar.add(file);
            menuBar.add(caricaImmagine);
            menuBar.add(info);
            menuBar.add(n);
            info.add(about);
            this.app.setJMenuBar(menuBar);

            this.app.addWindowListener(new WindowListener() 
            {
                @Override
                public void windowActivated(WindowEvent arg0) 
                {
                }

                @Override
                public void windowClosed(WindowEvent arg0) 
                {
                }

                @Override
                public void windowClosing(WindowEvent arg0) 
                {
                    try 
                    {
                        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); 
                        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));

                        JSONObject inviaAvvenutaDisconnessione = new JSONObject();
                        inviaAvvenutaDisconnessione.put("Tipo-Richiesta", "Invio-Messaggio");
                        inviaAvvenutaDisconnessione.put("Tipo-Messaggio", "Plain-Text");
                        inviaAvvenutaDisconnessione.put("Nome", nome);
                        inviaAvvenutaDisconnessione.put("Messaggio", "si è disconnesso!");
                        inviaAvvenutaDisconnessione.put("Data", data);
                        inviaAvvenutaDisconnessione.put("Time", time);

                        synchronized (writer)
                        {
                            writer.write(inviaAvvenutaDisconnessione.toString());
                            writer.flush();
                        }
                        
                        JSONObject closeRequest = new JSONObject();
                        closeRequest.put("Tipo-Richiesta", "Chiudi-Connessione");

                        synchronized (writer)
                        {
                            writer.write(closeRequest.toString());
                            writer.flush();
                            writer.close();
                        }
                    }
                    catch (IOException e)
                    {
                        System.exit(IO_EXCEPTION);
                    }
                    catch (Exception e) 
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void windowDeactivated(WindowEvent arg0) 
                {
                }

                @Override
                public void windowDeiconified(WindowEvent arg0) 
                {
                }

                @Override
                public void windowIconified(WindowEvent arg0) 
                {
                }

                @Override
                public void windowOpened(WindowEvent arg0) 
                {
                }

            });

            JPanel panel = new JPanel();
            LayoutManager layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
            panel.setLayout(layout);
            this.app.setContentPane(panel);

            utentiConnessi = new JTextField();
            utentiConnessi.setFont(font);
            utentiConnessi.setEditable(false);
            panel.add(utentiConnessi);

            this.textArea = new JTextPane();
            this.textArea.setFont(fontTextArea);
            DimensionUIResource dimension = new DimensionUIResource(WIDTH, 400);
            this.textArea.setPreferredSize(dimension);
            this.textArea.setAutoscrolls(true);
            this.textArea.setEditable(false);
            panel.add(this.textArea);

            scrollPaneTextArea = new JScrollPane(this.textArea);
            this.app.getContentPane().add(scrollPaneTextArea);

            JPanel panelInput = new JPanel();
            panelInput.setBackground(Color.WHITE);
            BoxLayout layoutInput = new BoxLayout(panelInput, BoxLayout.LINE_AXIS);
            panelInput.setLayout(layoutInput);

            this.input = new JTextField();
            this.input.setFont(fontInviaMessaggio);
            panelInput.add(input);

            JButton buttonInviaMessaggio = new JButton("Invia");
            buttonInviaMessaggio.setPreferredSize(new Dimension(75, 40));
            buttonInviaMessaggio.setFont(fontInviaMessaggio);
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
                            JOptionPane.showMessageDialog(app, "Il messaggio non deve superare i 256 caratteri", "Messaggio troppo lungo", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        else if (msg.length() == 0)
                        {
                            return;
                        }

                        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); 
                        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
                         
                        aggiungiMessaggio("Tu", msg);

                        JSONObject json = new JSONObject();
                        json.put("Tipo-Richiesta", "Invio-Messaggio");
                        json.put("Tipo-Messaggio", "Plain-Text");
                        json.put("Data", data);
                        json.put("Time", time);
                        json.put("Nome", nome);
                        json.put("Messaggio", msg);

                        synchronized (writer)
                        {
                            writer.write(json.toString()); 
                            writer.flush();
                        }

                        input.setText("");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            buttonInviaMessaggio.setBorder(null);

            this.input.addKeyListener(new KeyListener() {

                @Override
                public void keyPressed(KeyEvent arg0) 
                {
                    if (arg0.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                        buttonInviaMessaggio.doClick();
                    }
                }

                @Override
                public void keyReleased(KeyEvent arg0) 
                {
                }

                @Override
                public void keyTyped(KeyEvent arg0) 
                {
                }
            });

            panelInput.add(buttonInviaMessaggio);

            this.doc = this.textArea.getStyledDocument();
            this.s = scrollPaneTextArea.getVerticalScrollBar();

            panel.add(panelInput);
        }

        public void show() 
        {
            this.app.setResizable(false);
            this.app.setVisible(true);
        }

        public void aggiungiMessaggio(String nome, String msg) 
        {
            try 
            {
                if (!this.utenteColore.containsKey(nome))
                {
                    this.aggiungiUtenteColore(nome, colors.get(new Random().nextInt(colors.size())));
                }

                Style style = textArea.addStyle(msg, null);
                Color c = nome.equals("Tu")
                    ? Color.BLACK
                    : utenteColore.get(nome);
                
                StyleConstants.setForeground(style, c);

                doc.insertString(doc.getLength(), nome + ": " + msg + "\n", style);
                this.s.setValue(this.s.getMaximum());
            } 
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        public void aggiungiImmagine(String nome, String img)
        {
            try
            {
                ImageIcon i = new ImageIcon(img);
                aggiungiMessaggio(nome, "");
                textArea.insertIcon(i);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        public void setNumeroUtentiConnessi(int numero)
        {
            try
            {
                if (numero == 1)
                {
                    this.utentiConnessi.setText("Solo te sei connesso!");
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
}