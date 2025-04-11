import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class cliente {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Cliente");
        JTextArea area = new JTextArea(10, 30);
        JButton enviar = new JButton("Enviar");

        enviar.addActionListener(e -> {
            try {
                String texto = area.getText();
                URL url = new URL("http://localhost:8000/processar");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(texto.getBytes());
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String resposta = br.readLine();
                JOptionPane.showMessageDialog(frame, "Resposta: " + resposta);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(area), BorderLayout.CENTER);
        frame.add(enviar, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}