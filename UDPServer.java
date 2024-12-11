
// UDPServer.java
import javax.swing.*;
import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPServer {
    private static final int PORT = 12345;
    private JTextArea textArea;

    public UDPServer() {
        JFrame frame = new JFrame("UDP Server");
        textArea = new JTextArea(20, 40);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        startServer();
    }

    private void startServer() {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            textArea.append("Server started on port " + PORT + "\n");
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                executor.submit(() -> handlePacket(packet));
            }
        } catch (Exception e) {
            textArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    private void handlePacket(DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength());
        String clientAddress = packet.getAddress().getHostAddress() + ":" + packet.getPort();
        System.out.println("Message from " + clientAddress + ": " + message);
        textArea.append("Message from " + clientAddress + ": " + message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UDPServer::new);
    }
}
