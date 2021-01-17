package srcclient;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class CasellaUtente extends JPanel 
{
    private static final long serialVersionUID = 8745213302075828373L;

    private String nome;
    private JLabel labelNome;
    private JLabel labelNumeroMessaggi;
    private Font fontNumeroMessaggi = new FontUIResource("monospace", Font.BOLD, 14);
    private Font fontNome = new FontUIResource("Arial", Font.PLAIN, 16);
    private ChatController controller;

    public CasellaUtente(String nome, ChatController controller) 
    {
        this.nome = nome;
        this.controller = controller;
        this.labelNome = new JLabel(nome);
        this.labelNumeroMessaggi = new JLabel();

        Color bg = new ColorUIResource(0.1f, 0.1f, 0.1f);
        Color fg = new ColorUIResource(0.9f, 0.9f, 0.9f);

        this.labelNome.setBackground(bg);
        this.labelNome.setForeground(fg);
        this.labelNumeroMessaggi.setBackground(bg);
        this.labelNumeroMessaggi.setForeground(fg);
        this.setBackground(bg);
        this.setForeground(fg);

        this.labelNumeroMessaggi.setFont(fontNumeroMessaggi);
        this.labelNome.setFont(fontNome);

        this.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        labelNome.setBorder(null);
        labelNumeroMessaggi.setBorder(null);

        this.labelNumeroMessaggi.setPreferredSize(new Dimension(24, 32));
        this.labelNumeroMessaggi.setMinimumSize(new Dimension(24, 32));
        this.labelNumeroMessaggi.setMaximumSize(new Dimension(24, 32));

        this.setPreferredSize(new Dimension(240, 64));
        this.setMinimumSize(new Dimension(240, 64));
        this.setMaximumSize(new Dimension(240, 64));
        this.setLayout(new BorderLayout());

        this.addMouseListener(new MouseInputListener() 
        {
            @Override
            public void mouseClicked(MouseEvent arg0) 
            {
                controller.setUtenteCorrente(nome);
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

        JPanel wrapperNumeroMessaggi = new JPanel();
        wrapperNumeroMessaggi.setLayout(new BoxLayout(wrapperNumeroMessaggi, BoxLayout.PAGE_AXIS));
        wrapperNumeroMessaggi.setBackground(bg);
        wrapperNumeroMessaggi.add(Box.createRigidArea(new Dimension(0, 16)));
        wrapperNumeroMessaggi.add(this.labelNumeroMessaggi);

        JPanel wrapperNome = new JPanel();
        wrapperNome.setBackground(bg);
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