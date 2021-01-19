package srcclient;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Utente 
{
    private String nome;
    private JScrollPane scrollPane;
    private JScrollBar scrollBar;
    private JTextPane textArea;
    private StyledDocument doc;
    private Font fontTextArea = new FontUIResource("Caladea", Font.PLAIN, 18);
    
    public Utente(String nome)
    {
        this.nome = nome;
        this.textArea = new JTextPane();
        this.textArea.setBorder(null);
        this.textArea.setBackground(new ColorUIResource(0f, 0f, 0f));
        this.scrollPane = new JScrollPane(this.textArea);
        this.scrollPane.setBorder(null);
        this.scrollPane.setBackground(new ColorUIResource(0.1f, 0.1f, 0.1f));
        this.scrollBar = this.scrollPane.getVerticalScrollBar();
        this.textArea.setEditable(false);
        this.doc = this.textArea.getStyledDocument();
        this.textArea.setBorder(new EmptyBorder(5, 5, 5, 5));
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

    public JScrollPane getScrollPane()
    {
        return this.scrollPane;
    }

    public void aggiungiMessaggio(CasellaMessaggio casella)
    {
        try
        {
            Style s = doc.addStyle("Style", null);
            StyleConstants.setComponent(s, casella);
            doc.insertString(doc.getLength(), "invisible\n", s);

            Style space = doc.addStyle("space", null);
            StyleConstants.setComponent(space, Box.createRigidArea(new Dimension(0, 4)));
            doc.insertString(doc.getLength(), "invisible\n", space);

            this.scrollBar.setValue(this.scrollBar.getMaximum());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }

        if (o.getClass() != getClass())
        {
            return false;
        }

        if (o == this)
        {
            return true;
        }

        if (o instanceof Utente)
        {
            Utente that = (Utente)o;

            return that.textArea == this.textArea 
                && that.nome.equals(this.nome);
        }

        return false;
    }
}
