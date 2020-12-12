package src;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.Queue;

public class Log extends Thread
{
    
    private Queue<String> coda;
    private String filename;
    
    public Log(String filename)
    {
        this.filename = "../lop/" + filename;
        this.coda = new LinkedList<>();
    }

    public synchronized void add_msg(String msg)
    {
        if (msg != null)
        {
            this.coda.add(msg);
        }
    }

    @Override
    public void run()
    {
        try
        {
            File file = new File(this.filename);
            if (!file.exists())
            {
                file.createNewFile();
            }
            
            FileWriter f = new FileWriter(this.filename, true);
            Formatter write = new Formatter(f);
            
            while (!Thread.currentThread().isInterrupted())
            {
                LocalDateTime myDateObj = LocalDateTime.now();
                DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String formattedDate = myDateObj.format(myFormatObj);
                
                String msg = this.coda.poll();

                if (msg != null)
                {
                    write.format("[ %s ] -- %s\n", formattedDate, msg);
                    write.flush();
                }
            }

            write.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void shutdown ()
    {
        try
        {
            // Interrompo il logger
            Thread.currentThread().interrupt();

            // Aspetto la fine del thread
            Thread.currentThread().join(100);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}