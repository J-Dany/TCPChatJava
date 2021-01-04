package srcclient;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

public class Utente 
{
    private String nome;
    private JTextPane textArea;
    private Font fontTextArea = new FontUIResource("Caladea", Font.PLAIN, 18);
    
    public Utente(String nome)
    {
        this.nome = nome;
        this.textArea = new JTextPane();
        this.textArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.textArea.setFont(fontTextArea);
    }

    public String getNome()
    {
        return this.nome;
    }

    public void setTextArea(JTextPane pane)
    {
        this.textArea = pane;
    }

    public JTextPane getTextArea()
    {
        return this.textArea;
    }
}
