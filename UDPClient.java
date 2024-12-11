import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {
    private static final int SERVER_PORT = 12345;
    private static final String SERVER_HOST = "localhost";
    private JTextField inputField;
    private JTextArea textArea;

    public UDPClient() {
        JFrame frame = new JFrame("UDP Client");
        textArea = new JTextArea(20, 40);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        inputField = new JTextField(30);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(this::sendMessage);

        JPanel inputPanel = new JPanel();
        inputPanel.add(inputField);
        inputPanel.add(sendButton);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void sendMessage(ActionEvent event) {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            try (DatagramSocket socket = new DatagramSocket()) {
                byte[] buffer = message.getBytes();
                InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
                socket.send(packet);
                textArea.append("Sent: " + message + "\n");
                inputField.setText("");
            } catch (Exception e) {
                textArea.append("Error: " + e.getMessage() + "\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UDPClient::new);
    }
}
