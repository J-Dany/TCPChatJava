package src;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.net.Socket;
import java.io.OutputStreamWriter;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class AppClient {
    /**
     * Codici di ritorno dell'applicazione
     */
    public static final int CONNESSIONE_RIFIUTATA = 1;
    public static final int IO_EXCEPTION = 2;
    public static final int OUTPUT_STREAM_NON_ISTANZIATO = 3;

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
                    new Random().nextInt(180),
                    new Random().nextInt(180),
                    new Random().nextInt(180)));
            }

            Scanner input = new Scanner(System.in);
            System.out.print("Nome utente: ");
            String nome = input.nextLine();
            socket.getOutputStream().write((nome + "%%!").getBytes());
            input.close();

            ChatUI chat = new ChatUI(socket, new String(nome));
            chat.prepareApp();
            chat.show();

            Thread read = new Thread(new Runnable() {
                @Override
                public void run() 
                {
                    while (!Thread.currentThread().isInterrupted()) 
                    {
                        try 
                        {
                            byte[] buffer = new byte[1024];
                            int l = socket.getInputStream().read(buffer);
                            String msg = new String(buffer, 0, l, "ISO-8859-1");

                            if (msg.equals("UTENTE_NON_RICONOSCIUTO")) 
                            {
                                break;
                            }

                            String nome = msg.split(":")[0];
                            if (!chat.getUtenteColore().containsKey(nome))
                            {
                                chat.aggiungiUtenteColore(nome, colors.get(new Random().nextInt(colors.size())));
                            }

                            chat.aggiungiMessaggio(msg);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ChatUI 
    {
        private HashMap<String, Color> utenteColore;
        private OutputStreamWriter writer;
        private Socket socket;
        private JFrame app;
        private JTextField input;
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
            if (socket != null) {
                this.socket = socket;
                this.writer = new OutputStreamWriter(this.socket.getOutputStream(), "ISO-8859-1");
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
            quit.setFont(font);
            file.add(quit);
            JMenu info = new JMenu("Info");
            info.setFont(font);
            JMenuItem about = new JMenuItem("About");
            about.setFont(font);
            menuBar.add(file);
            menuBar.add(info);
            info.add(about);
            this.app.setJMenuBar(menuBar);

            JPanel panel = new JPanel();
            BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
            panel.setLayout(layout);
            this.app.setContentPane(panel);

            this.textArea = new JTextPane();
            this.textArea.setFont(fontTextArea);
            this.textArea.setPreferredSize(new Dimension(WIDTH, 400));
            this.textArea.setEditable(false);
            panel.add(this.textArea);

            JPanel panelInput = new JPanel();
            panelInput.setBackground(Color.WHITE);
            BoxLayout layoutInput = new BoxLayout(panelInput, BoxLayout.LINE_AXIS);
            panelInput.setLayout(layoutInput);

            input = new JTextField();
            input.setFont(fontInviaMessaggio);
            panelInput.add(input);

            JButton buttonInviaMessaggio = new JButton("Invia");
            buttonInviaMessaggio.setPreferredSize(new Dimension(100, 40));
            buttonInviaMessaggio.setFont(fontInviaMessaggio);
            buttonInviaMessaggio.addActionListener(new Action()
            {
				@Override
                public void actionPerformed(ActionEvent arg0) 
                {
                    try
                    {
                        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); 
                        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
                        
                        String msg = input.getText();
                        aggiungiMessaggio("Tu: " + msg);
                        writer.write(data + " " + time + "|" + nome + "|" + msg);
                        writer.flush();
                        input.setText("");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
				}

				@Override
                public void addPropertyChangeListener(PropertyChangeListener arg0) 
                {					
				}

				@Override
                public Object getValue(String arg0) 
                {
					return null;
				}

				@Override
                public boolean isEnabled() 
                {
					return true;
				}

				@Override
                public void putValue(String arg0, Object arg1) 
                {					
				}

				@Override
                public void removePropertyChangeListener(PropertyChangeListener arg0) 
                {					
				}

				@Override
                public void setEnabled(boolean arg0) 
                {					
                }                
            });
            buttonInviaMessaggio.setBorder(null);

            panelInput.add(buttonInviaMessaggio);

            this.doc = this.textArea.getStyledDocument();

            panel.add(panelInput);
        }

        public void show() 
        {
            this.app.setResizable(false);
            this.app.setVisible(true);
        }

        public void aggiungiMessaggio(String msg) {
            try 
            {
                String nome = msg.split(":")[0];
                Style style = textArea.addStyle(msg, null);
                Color c = nome.equals("Tu")
                    ? Color.BLACK
                    : utenteColore.get(nome);
                StyleConstants.setForeground(style, c);

                doc.insertString(doc.getLength(), msg + "\n", style);
            } 
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}