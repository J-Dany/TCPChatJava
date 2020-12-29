package srcclient;

import java.awt.image.BufferedImage;
import org.json.JSONObject;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.OutputStreamWriter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AppClient {
    /**
     * Codici di ritorno dell'applicazione
     */
    private static final int UTENTE_NON_RICONOSCIUTO = 4;

    /**
     * Nome del client
     */
    private static String nome;

    /**
     * Grandezza buffer di ricezione.
     */
    private static final int GRANDEZZA_BUFFER = 8192;

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
            buttonLogin.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    String n = nomeUtente.getText();
                    String p = new String(password.getPassword());

                    MessageDigest md = null;
                    try 
                    {
                        md = MessageDigest.getInstance("MD5");
                    } 
                    catch (NoSuchAlgorithmException e1) 
                    {
                        e1.printStackTrace();
                    }
                    md.update(p.getBytes());
                    byte[] digest = md.digest();
                    String hash = null;

                    try
                    {
                        hash = new String(digest, 0, digest.length, "UTF8");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    JSONObject auth = new JSONObject();
                    auth.put("Tipo-Richiesta", "Autenticazione");
                    auth.put("Nome", n);
                    auth.put("Password", hash);
                    
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
                            nuovoMessaggio.put("Tipo-Messaggio", "Plain-Text");
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

                                BufferedImage inputImage = ImageIO.read(ImageIO.createImageInputStream(socket.getOutputStream()));
                                
                                chat.aggiungiImmagine(risposta.getString("Nome"), new ImageIcon(inputImage));
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
}