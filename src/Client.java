package src;

import java.net.InetAddress;

public class Client
{
    /**
     * L'utente può fare solo 16 richieste al minuto
     */
    private final int MAX_REQUEST = 64;

    private InetAddress address;
    private int counter;
    private long time;
    private int max_request_before_ban;
    
    public Client (InetAddress address)
    {
        this.address = address;
        this.counter = this.MAX_REQUEST; 
        this.max_request_before_ban = this.MAX_REQUEST;
        this.time = System.currentTimeMillis() / 1000;
    }

    public int getCounter()
    {
        return this.counter;
    }

    public void clientConnected()
    {
        if (this.counter > 0 && (System.currentTimeMillis() / 1000) - this.time < 60)
        {
            this.counter = this.counter - 1;
            this.time = System.currentTimeMillis() / 1000;
        }
        else if (this.max_request_before_ban > 0)
        {
            this.max_request_before_ban = this.max_request_before_ban - 1;
        }
        // Troppe richieste al minuto, l'utente viene momentaneamente bannato
        else if ((System.currentTimeMillis() / 1000) - this.time >= 60)
        {
            this.counter = this.MAX_REQUEST;
            this.max_request_before_ban = this.MAX_REQUEST;
            this.time = System.currentTimeMillis() / 1000;
        }
        else
        {
            Server.getServer().ban(this.address);
        }
    }

    public InetAddress getAddress()
    {
        return this.address;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }  

        if (o == null)
        {
            return false;
        }
        
        if (getClass() != o.getClass())
        {
            return false;
        }

        Client c = (Client) o;
        if (this.address.equals(c.address))
        {
            return true;
        }

        return false;
    }

    @Override
    public String toString()
    {
        return "Indirizzo: " + this.address;
    }
}