package srcclient;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class CasellaMessaggio extends JPanel 
{
    private static final long serialVersionUID = -7569772015523879553L;

    private JTextArea areaMessaggio;
    private JTextField nomeUtente;
    private JTextField dataTempoMessaggio;

    private Font fontDataTempo = new FontUIResource("Noto Sans", Font.PLAIN, 12);
    private Font fontMessaggio = new FontUIResource("Noto Sans", Font.PLAIN, 16);
    private Font fontNome = new FontUIResource("Noto Sans", Font.BOLD, 18);

    public CasellaMessaggio(String nome, String msg, String data, String tempo) 
    {
        areaMessaggio = new JTextArea();
        nomeUtente = new JTextField();
        dataTempoMessaggio = new JTextField();

        areaMessaggio.setAutoscrolls(true);

        nomeUtente.setBorder(null);
        dataTempoMessaggio.setBorder(null);
        this.setBorder(null);

        areaMessaggio.setFont(fontMessaggio);
        nomeUtente.setFont(fontNome);
        dataTempoMessaggio.setFont(fontDataTempo);

        areaMessaggio.setEditable(false);
        nomeUtente.setEditable(false);
        dataTempoMessaggio.setEditable(false);

        areaMessaggio.setText(msg);
        nomeUtente.setText(nome);
        dataTempoMessaggio.setText(data + " " + tempo);

        this.addMouseListener(new MouseInputListener() 
        {
            @Override
            public void mouseClicked(MouseEvent arg0) { }

            @Override
            public void mouseEntered(MouseEvent arg0) 
            {
                setCursor(new Cursor(Cursor.TEXT_CURSOR));
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

        this.setPreferredSize(new Dimension(350, 128));
        this.setMinimumSize(new Dimension(350, 128));
        this.setMaximumSize(new Dimension(350, 128));
        this.setLayout(new BorderLayout());

        Color bg = new ColorUIResource(37, 211, 102);

        this.areaMessaggio.setBackground(bg);
        this.areaMessaggio.setForeground(Color.BLACK);

        LineBorder line = new LineBorder(bg, 6, true);
        this.setBorder(line);
        nomeUtente.setBorder(line);
        dataTempoMessaggio.setBorder(line);

        this.setBackground(bg);
        nomeUtente.setBackground(bg);
        dataTempoMessaggio.setBackground(bg);
        
        this.add(nomeUtente, BorderLayout.PAGE_START);
        this.add(areaMessaggio, BorderLayout.CENTER);
        this.add(dataTempoMessaggio, BorderLayout.PAGE_END);
    }
}