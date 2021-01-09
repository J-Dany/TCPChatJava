package srcclient;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

public class CasellaMessaggio extends JPanel {
    private static final long serialVersionUID = -7569772015523879553L;

    private JTextArea areaMessaggio;
    private JTextField nomeUtente;
    private JTextField dataTempoMessaggio;

    private Font fontDataTempo = new FontUIResource("Noto Sans", Font.PLAIN, 12);
    private Font fontMessaggio = new FontUIResource("Caladea", Font.PLAIN, 18);
    private Font fontNome = new FontUIResource("Noto Sans", Font.BOLD, 16);

    public CasellaMessaggio(String nome, String msg, String data, String tempo) 
    {
        areaMessaggio = new JTextArea();
        nomeUtente = new JTextField();
        dataTempoMessaggio = new JTextField();

        areaMessaggio.setFont(fontMessaggio);
        nomeUtente.setFont(fontNome);
        dataTempoMessaggio.setFont(fontDataTempo);

        areaMessaggio.setEditable(false);
        nomeUtente.setEditable(false);
        dataTempoMessaggio.setEditable(false);

        areaMessaggio.setText(msg);
        nomeUtente.setText(nome);
        dataTempoMessaggio.setText(data + " " + tempo);

        this.setPreferredSize(new Dimension(500, 100));
        this.setMinimumSize(new Dimension(500, 100));
        this.setMaximumSize(new Dimension(500, 100));
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setLayout(new BorderLayout());
        
        this.add(nomeUtente, BorderLayout.PAGE_START);
        this.add(areaMessaggio, BorderLayout.CENTER);
        this.add(dataTempoMessaggio, BorderLayout.PAGE_END);
    }
}