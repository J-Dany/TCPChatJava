package srcclient;

import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.event.MouseInputListener;

public class CasellaUtente extends JPanel 
{
    private static final long serialVersionUID = 8745213302075828373L;

    private Utente utenteCorrente;
    private JLabel labelNome;
    private JLabel labelNumeroMessaggi;
    private boolean open;
    private final ChatUI gui;

    public CasellaUtente(String nomeUtente, ChatUI gui) 
    {
        this.open = false;
        this.gui = gui;
        this.utenteCorrente = new Utente(nomeUtente);
        this.labelNome = new JLabel(nomeUtente);
        this.labelNumeroMessaggi = new JLabel();

        if (!gui.getUtenti().containsKey(nomeUtente))
        {
            gui.getUtenti().put(nomeUtente, this);
        }

        this.setPreferredSize(new Dimension(240, 64));
        this.setMinimumSize(new Dimension(240, 64));
        this.setMaximumSize(new Dimension(240, 64));
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setBackground(Color.WHITE);

        this.addMouseListener(new MouseInputListener() 
        {
            @Override
            public void mouseClicked(MouseEvent arg0) 
            {
                gui.aggiungiTextPaneChatCorrente(utenteCorrente);
                gui.getUtenti().get(utenteCorrente.getNome()).setOpen(true);
            }

            @Override
            public void mouseEntered(MouseEvent arg0) 
            { 
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent arg0) { }

            @Override
            public void mousePressed(MouseEvent arg0) { }

            @Override
            public void mouseReleased(MouseEvent arg0) { }

            @Override
            public void mouseDragged(MouseEvent arg0) { }

            @Override
            public void mouseMoved(MouseEvent arg0) { }
            
        });

        this.add(this.labelNome, BorderLayout.LINE_START);
        this.add(this.labelNumeroMessaggi, BorderLayout.LINE_END);
    }

    public boolean isOpen()
    {
        return this.open;
    }

    public void setOpen(boolean value)
    {
        this.open = value;
    }

    public Utente getUtente()
    {
        return this.utenteCorrente;
    }

    public void setNumeroMessaggi(String numero)
    {
        this.labelNumeroMessaggi.setText(numero);
    }

    public int getNumeroMessaggi()
    {
        return Integer.parseInt(this.labelNumeroMessaggi.getText());
    }
}