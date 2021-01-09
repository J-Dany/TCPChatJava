package srcclient;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.MouseInputListener;

public class CasellaUtente extends JPanel 
{
    private static final long serialVersionUID = 8745213302075828373L;

    private Utente user;
    private JLabel labelNome;
    private JLabel labelNumeroMessaggi;
    private ChatUI gui;

    public CasellaUtente(Utente u, ChatUI gui) 
    {
        this.gui = gui;
        this.user = u;
        this.labelNome = new JLabel(u.getNome());
        this.labelNumeroMessaggi = new JLabel();

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
                AppClient.setUtenteCorrente(user);
                AppClient.setNomeUtenteCorrente(user.getNome());
                gui.aggiungiTextPaneChatCorrente(user);
                labelNumeroMessaggi.setText("");
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

    public void setNumeroMessaggi(String numero)
    {
        this.labelNumeroMessaggi.setText(numero);
    }

    public int getNumeroMessaggi()
    {
        return Integer.parseInt(this.labelNumeroMessaggi.getText());
    }
}