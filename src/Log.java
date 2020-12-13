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

    public void writeToFile(String msg)
    {
        try
        {
            FileWriter f = new FileWriter(this.filename, true);
            Formatter write = new Formatter(f);

            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String formattedDate = myDateObj.format(myFormatObj);

            if (msg != null)
            {
                write.format("[ %s ] -- %s\n", formattedDate, msg);
                write.flush();
            }

            write.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        try
        {
            File lop_dir = new File("../lop");
            if (!lop_dir.exists())
            {
                lop_dir.mkdirs();
            }
            
            while (!Thread.currentThread().isInterrupted())
            {
                this.writeToFile(this.coda.poll());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void shutdown ()
    {
        if (this.coda.size() > 0)
        {
            try
            {
                for (String msg : this.coda)
                {
                    this.writeToFile(msg);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}