package src;

import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class AppClient 
{    
    // Struttura messaggio: Data Tempo|Utente|Messaggio

    public static void main(String[] args)
    {
        if (args.length == 0)
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
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String nomeUtente;

        Scanner input = new Scanner(System.in);

        System.out.print("Inserisci nome utente: ");
        nomeUtente = input.nextLine();

        System.out.println("Il messaggio deve essere lungo max. 256 e, per smettere di inviare i messaggi, invio con messaggio vuoto.");
        System.out.println("*********************************");

        OutputStreamWriter write;
        try
        {
            write = new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String msg = "";
        while (msg != null)
        {
            System.out.print("Tu: ");
            msg = input.nextLine();
            
            if (msg.length() == 0)
            {
                msg = null;
            }
            else
            {
                try
                {
                    String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
                    
                    write.write(data + " " + time + "|" + nomeUtente + "|" + msg);
                    write.flush();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        try
        {
            socket.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        input.close();
    }
}