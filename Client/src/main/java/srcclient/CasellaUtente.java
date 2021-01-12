package srcclient;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.FontUIResource;

public class CasellaUtente extends JPanel 
{
    private static final long serialVersionUID = 8745213302075828373L;

    private JLabel labelNome;
    private JLabel labelNumeroMessaggi;
    private Font fontNumeroMessaggi = new FontUIResource("monospace", Font.BOLD, 14);
    private Font fontNome = new FontUIResource("Arial", Font.PLAIN, 16);

    public CasellaUtente(String nome) 
    {
        this.labelNome = new JLabel(nome);
        this.labelNumeroMessaggi = new JLabel();

        this.labelNumeroMessaggi.setFont(fontNumeroMessaggi);
        this.labelNome.setFont(fontNome);

        this.labelNumeroMessaggi.setPreferredSize(new Dimension(24, 32));
        this.labelNumeroMessaggi.setMinimumSize(new Dimension(24, 32));
        this.labelNumeroMessaggi.setMaximumSize(new Dimension(24, 32));

        this.setPreferredSize(new Dimension(240, 64));
        this.setMinimumSize(new Dimension(240, 64));
        this.setMaximumSize(new Dimension(240, 64));
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setBackground(Color.WHITE);

        this.addMouseListener(new MouseInputListener() 
        {
            @Override
            public void mouseClicked(MouseEvent arg0) { }

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

        JPanel wrapperNumeroMessaggi = new JPanel();
        wrapperNumeroMessaggi.setLayout(new BoxLayout(wrapperNumeroMessaggi, BoxLayout.PAGE_AXIS));
        wrapperNumeroMessaggi.setBackground(Color.WHITE);
        wrapperNumeroMessaggi.add(Box.createRigidArea(new Dimension(0, 16)));
        wrapperNumeroMessaggi.add(this.labelNumeroMessaggi);

        JPanel wrapperNome = new JPanel();
        wrapperNome.setBackground(Color.WHITE);
        wrapperNome.setLayout(new BorderLayout());
        wrapperNome.add(this.labelNome, BorderLayout.CENTER);

        this.add(wrapperNome, BorderLayout.LINE_START);
        this.add(wrapperNumeroMessaggi, BorderLayout.LINE_END);
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

    public void incrementaNumeroMessaggi()
    {
        setNumeroMessaggi("" + (getNumeroMessaggi() + 1));
    }
}