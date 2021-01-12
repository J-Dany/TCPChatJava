package srcclient;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.LineBorder;
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

        LineBorder bord = new LineBorder(Color.GREEN, 8, true);

        this.labelNumeroMessaggi.setBorder(bord);

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
                System.out.println("Casella di " + user.getNome() + " cliccata, setto utente corrente");
                AppClient.setUtenteCorrente(user);
                AppClient.setNomeUtenteCorrente(user.getNome());
                System.out.println("Carico la text pane di " + user.getNome());
                gui.aggiungiTextPaneChatCorrente(user);
                System.out.println("Text pane caricata");
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
        return Integer.parseInt(
            (this.labelNumeroMessaggi.getText().equals(""))
            ? "0"
            : this.labelNumeroMessaggi.getText()
        );
    }
}