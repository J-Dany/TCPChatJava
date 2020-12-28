package srcclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.Robot;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.json.JSONObject;

public class ChatUI {

    private static final int IO_EXCEPTION = 2;
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
            this.colors.add(new ColorUIResource(new Random().nextInt(180) + 70, new Random().nextInt(100),
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
        quit.addActionListener(new ActionListener() {
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
        buttonCaricaImmagine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.showOpenDialog(app);

                File f = fileChooser.getSelectedFile();

                try {
                    FileInputStream fileInputStream = new FileInputStream(f);

                    if (f.length() > 1 << 26) 
                    {
                        JOptionPane.showMessageDialog(app, "L'immagine è troppo grande!", "Errore immagine",
                                JOptionPane.ERROR_MESSAGE);
                    } 
                    else 
                    {
                        byte[] imageData = new byte[(int) f.length()];
                        fileInputStream.read(imageData);
                        //aggiungiImmagine(nome, new Image(imageData));
                        String msg = Base64.getEncoder().encodeToString(imageData);

                        int msgLength = msg.length();

                            String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));

                            JSONObject mandaImmagine = new JSONObject();
                            mandaImmagine.put("Tipo-Richiesta", "Invio-Messaggio");
                            mandaImmagine.put("Tipo-Messaggio", "Immagine");
                            mandaImmagine.put("Data", data);
                            mandaImmagine.put("Time", time);
                            mandaImmagine.put("Nome", nome);

                            synchronized (writer)
                            {
                                writer.write(mandaImmagine.toString());
                                writer.flush();
                            }

                            Robot robot = new Robot();
                            BufferedImage bimp = robot.createScreenCapture(new Rectangle(0, 0, 427, 240));
                            ImageIO.write(bimp, "PNG", socket.getOutputStream());

                            /*for (int i = 0; i < msgLength - 1;) 
                            {
                                int k = i;

                                int lenPerPacketImg = 1024;

                                byte[] fragmentImg = new byte[lenPerPacketImg];
                                for (int j = 0; j < lenPerPacketImg; ++j) 
                                {
                                    if (k == msgLength) 
                                    {
                                        break;
                                    }
                                    fragmentImg[j] = (byte) msg.charAt(k++);
                                }

                                if (k == msgLength) 
                                {
                                    break;
                                }

                                mandaImmagine.put("Messaggio", new String(fragmentImg));

                                writer.write(mandaImmagine.toString());
                                writer.flush();

                                i += lenPerPacketImg;
                                Thread.sleep(256);
                            }

                            JSONObject fineImmagine = new JSONObject();
                            fineImmagine.put("Tipo-Richiesta", "Invio-Messaggio");
                            fineImmagine.put("Fine", true);
                            fineImmagine.put("Tipo-Messaggio", "Immagine");
                            fineImmagine.put("Messaggio", "");
                            fineImmagine.put("Data", data);
                            fineImmagine.put("Time", time);
                            fineImmagine.put("Nome", nome);

                            try 
                            {
                                writer.write(fineImmagine.toString());
                                writer.flush();
                            } 
                            catch (Exception e) 
                            {
                                e.printStackTrace();
                            }*/
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
        JMenu n = new JMenu("Sei loggato come: " + nome);
        n.setFont(font);
        menuBar.add(file);
        menuBar.add(caricaImmagine);
        menuBar.add(info);
        menuBar.add(n);
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
            public void windowDeactivated(WindowEvent arg0) { }

            @Override
            public void windowDeiconified(WindowEvent arg0) { }

            @Override
            public void windowIconified(WindowEvent arg0) { }

            @Override
            public void windowOpened(WindowEvent arg0) { }
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
        buttonInviaMessaggio.addActionListener(new ActionListener() {
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

                    aggiungiMessaggio("Tu", msg);

                    JSONObject json = new JSONObject();
                    json.put("Tipo-Richiesta", "Invio-Messaggio");
                    json.put("Tipo-Messaggio", "Plain-Text");
                    json.put("Data", data);
                    json.put("Time", time);
                    json.put("Nome", nome);
                    json.put("Messaggio", msg);

                    synchronized (writer) {
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
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    buttonInviaMessaggio.doClick();
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) { }

            @Override
            public void keyTyped(KeyEvent arg0) { }
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

    public void aggiungiImmagine(String nome, Image img)
    {
        try
        {
            ImageIcon i = new ImageIcon(img);
            aggiungiMessaggio(nome, "");
            textArea.insertIcon(i);
            doc.insertString(doc.getLength(), "\n", null);
            this.s.setValue(this.s.getMaximum());
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