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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class AppClient 
{
    /**
     * Codici di ritorno dell'applicazione
     */
    public static final int CONNESSIONE_RIFIUTATA = 1;
    public static final int IO_EXCEPTION = 2;
    public static final int OUTPUT_STREAM_NON_ISTANZIATO = 3;
    public static final int UTENTE_NON_RICONOSCIUTO = 4;

    /**
     * Grandezza dell'applicazione
     */
    public static final int HEIGHT = 480;
    public static final int WIDTH = 853;

    public static ArrayList<ColorUIResource> colors = new ArrayList<>();

    public static void main(String[] args) 
    {
        try 
        {
            Socket socket = new Socket();
            InetSocketAddress server_address = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
            socket.connect(server_address);

            for (int i = 0; i < 256; ++i) 
            {
                colors.add(new ColorUIResource(
                    new Random().nextInt(180) + 70, 
                    new Random().nextInt(180) + 70,
                    new Random().nextInt(180) + 70));
            }

            Scanner input = new Scanner(System.in);
            System.out.print("Nome utente: ");
            String nome = input.nextLine();

            JSONObject jsonAutenticazione = new JSONObject();
            jsonAutenticazione.put("Tipo-Richiesta", "Autenticazione");
            jsonAutenticazione.put("Nome", nome);
            socket.getOutputStream().write(jsonAutenticazione.toString().getBytes());
            
            input.close();

            ChatUI chat = new ChatUI(socket, new String(nome));
            chat.prepareApp();
            chat.show();

            Thread read = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try 
                        {
                            byte[] buffer = new byte[1024];
                            int l = socket.getInputStream().read(buffer);
                            String msg = new String(buffer, 0, l, "UTF8");

                            JSONObject risposta = new JSONObject(msg);

                            switch (risposta.getString("Tipo-Richiesta"))
                            {
                                case "Autenticazione":
                                    if (!risposta.getBoolean("Risultato"))
                                    {
                                        System.exit(UTENTE_NON_RICONOSCIUTO);
                                    }
                                break;
                                case "Utente-Connesso":
                                    String n = risposta.getString("Nome");
                                    if (!chat.getUtenteColore().containsKey(n)) 
                                    {
                                        chat.aggiungiUtenteColore(n, colors.get(new Random().nextInt(colors.size())));
                                    }
                                break;
                                case "Nuovo-Messaggio":
                                    String nome = risposta.getString("Nome");
                                    if (!chat.getUtenteColore().containsKey(nome))
                                    {
                                        chat.aggiungiUtenteColore(nome, colors.get(new Random().nextInt(colors.size())));
                                    }
                                    chat.aggiungiMessaggio(nome ,risposta.getString("Messaggio"));
                                break;
                                case "Numero-Utenti":
                                    chat.setNumeroUtentiConnessi(risposta.getInt("Numero"));
                                break;
                            }
                        } 
                        catch (Exception e) 
                        {
                            e.printStackTrace();
                            break;
                        }
                    }
                }
            });
            read.start();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    static class ChatUI 
    {
        private HashMap<String, Color> utenteColore;
        private OutputStreamWriter writer;
        private Socket socket;
        private JFrame app;
        private JTextField input, utentiConnessi;
        private Font font = new FontUIResource("Noto Sans", Font.PLAIN, 14);
        private Font fontTextArea = new FontUIResource("Caladea", Font.PLAIN, 18);
        private Font fontInviaMessaggio = new FontUIResource("Noto Sans", Font.PLAIN, 18);
        private JTextPane textArea;
        private StyledDocument doc;
        private String nome;

        public ChatUI(Socket socket, String nome) throws IOException 
        {
            this.utenteColore = new HashMap<>();
            this.nome = nome;
            this.app = new JFrame("Chat");
            if (socket != null) 
            {
                this.socket = socket;
                this.writer = new OutputStreamWriter(this.socket.getOutputStream(), "UTF8");
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
                        JSONObject closeRequest = new JSONObject();
                        closeRequest.put("Tipo-Richiesta", "Chiudi-Connessione");

                        writer.write(closeRequest.toString());
                        writer.flush();
                        writer.close();

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
            menuBar.add(file);
            menuBar.add(info);
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
                        JSONObject closeRequest = new JSONObject();
                        closeRequest.put("Tipo-Richiesta", "Chiudi-Connessione");

                        writer.write(closeRequest.toString());
                        writer.flush();
                        writer.close();
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
            this.textArea.setEditable(false);
            panel.add(this.textArea);

            this.app.getContentPane().add(new JScrollPane(this.textArea));

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
                        json.put("Data", data);
                        json.put("Time", time);
                        json.put("Nome", nome);
                        json.put("Messaggio", msg);

                        writer.write(json.toString()); 
                        writer.flush();

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
                Style style = textArea.addStyle(msg, null);
                Color c = nome.equals("Tu")
                    ? Color.BLACK
                    : utenteColore.get(nome);
                
                StyleConstants.setForeground(style, c);

                doc.insertString(doc.getLength(), nome + ": " + msg + "\n", style);
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