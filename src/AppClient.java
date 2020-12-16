package src;

import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
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

        String msg = "";
        while (msg != null)
        {
            System.out.print("Inserisci messaggio (max. 256, invio con messaggio vuoto per smettere di inviare): ");
            msg = input.nextLine();
            
            if (msg.length() == 0)
            {
                msg = null;
            }
            else
            {
                try
                {
                    LocalDate data = LocalDate.now();
                    LocalTime time = LocalTime.now();
                    OutputStreamWriter write = new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1");
                    
                    write.write(data + " " + time + "|" + nomeUtente + "|" + msg);
                    write.flush();
                    write.close();
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