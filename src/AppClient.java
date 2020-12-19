package src;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;

import java.net.Socket;
import java.io.OutputStreamWriter;
import java.awt.LayoutManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ConnectException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class AppClient 
{
    /**
     * Codici di ritorno dell'applicazione
     */
    public static final int CONNESSIONE_RIFIUTATA = 1;
    public static final int IO_EXCEPTION = 2;
    public static final int OUTPUT_STREAM_NON_ISTANZIATO = 3;

    /**
     * Grandezza dell'applicazione
     */
    public static final int HEIGHT = 600;
    public static final int WIDTH  = 900;

    public static void main(String[] args) 
    {
        /*if (args.length == 0) 
        {
            System.out.println("Non sono stati forniti argomenti all'applicazione");
            System.exit(1);
        }

        Socket socket = new Socket();
        InetSocketAddress server_address = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
        try 
        {
            socket.connect(server_address);
        } 
        catch (ConnectException e)
        {
            e.printStackTrace();
            System.exit(CONNESSIONE_RIFIUTATA);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(IO_EXCEPTION);
        }*/

        try
        {
            /*Socket socket = new Socket();
            InetSocketAddress server_address = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
            socket.connect(server_address);*/

            ChatUI chat = new ChatUI(null);
            chat.prepareApp();
            chat.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        /*String nomeUtente;

        Scanner input = new Scanner(System.in);

        System.out.print("Inserisci nome utente: ");
        nomeUtente = input.nextLine();

        OutputStreamWriter write = null;
        try 
        {
            write = new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1");

            write.write(nomeUtente + "%%!");
            write.flush();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }

        if (write == null) 
        {
            System.exit(OUTPUT_STREAM_NON_ISTANZIATO);
        }

        System.out.println("Il messaggio deve essere lungo max. 256 e, per smettere di inviare i messaggi, inserisci '!close'");
        System.out.println("*********************************");

        Thread read = new Thread(new Runnable()
        {
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

                        System.out.println("\n" + msg);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
        read.start();

        String msg = "";
        while (msg != null) 
        {
            if (socket.isClosed())
            {
                break;
            }

            System.out.print("Tu: ");
            msg = input.nextLine();

            if (msg.equals("!close")) 
            {
                msg = "close";
                
                try 
                {
                    write.write(msg);
                    write.flush();
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }

                break;
            }
            else
            {
                try
                {
                    String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
                    
                    // Struttura messaggio: Data Tempo|Utente|Messaggio
                    write.write(data + " " + time + "|" + nomeUtente + "|" + msg);
                    write.flush();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        read.interrupt();

        try
        {
            socket.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        input.close();*/
    }

    static class ChatUI
    {
        private OutputStreamWriter writer;
        private Socket socket;
        private JFrame app;
        private Font font = new FontUIResource("Noto Sans", Font.PLAIN, 14);
        private Font fontTextArea = new FontUIResource("Caladea", Font.PLAIN, 16);
        private Font fontNomeUtente = new FontUIResource("Noto Sans", Font.PLAIN, 18);
        private JTextPane textArea;

        public ChatUI(Socket socket) throws IOException
        {
            this.app = new JFrame("Chat");
            if (socket != null)
            {
                this.socket = socket;
                this.writer = new OutputStreamWriter(this.socket.getOutputStream(), "ISO-8859-1");
            }
        }

        public void prepareApp()
        {
            this.app.setSize(WIDTH, HEIGHT);
            this.app.setLayout(new GridLayout(2, 1));
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

            JPanel panel1 = new JPanel();
            panel1.setLayout(new GridLayout(1, 2));

            this.textArea = new JTextPane();
            this.textArea.setFont(fontTextArea);
            this.textArea.setEditable(false);
            panel1.add(this.textArea);

            JPanel containerNomeUtente = new JPanel();
            JTextField nomeUtente = new JTextField();
            nomeUtente.setPreferredSize(new Dimension(WIDTH / 2, 64));
            nomeUtente.setMargin(new InsetsUIResource(20, 20, 20, 20));
            nomeUtente.setFont(fontNomeUtente);
            JButton buttonInviaNomeUtente = new JButton("Login");
            buttonInviaNomeUtente.setFont(font);
            containerNomeUtente.add(nomeUtente);
            containerNomeUtente.add(buttonInviaNomeUtente);
            panel1.add(containerNomeUtente);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(1, 2));
            panel.setBackground(Color.BLACK);

            JTextField input = new JTextField();
            input.setFont(font);
            panel.add(input);

            JButton buttonInvia = new JButton("Invia");
            buttonInvia.setFont(font);
            panel.add(buttonInvia);

            this.app.setJMenuBar(menuBar);
            this.app.add(panel1);
            this.app.add(panel);
        }

        public void show()
        {
            this.app.setResizable(false);
            this.app.setVisible(true);
        }

        private void aggiungiMessaggio(String msg)
        {

        }
    }
}