import javax.swing.*;
import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPServer {
    private static final int PORT = 12345;
    private final JTextArea textArea;
    private final Set<ClientInfo> clients = new HashSet<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

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
        executor.submit(() -> {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {
                textArea.append("Server started on port " + PORT + "\n");
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    executor.submit(() -> handlePacket(socket, packet));
                }
            } catch (Exception e) {
                textArea.append("Error: " + e.getMessage() + "\n");
            }
        });
    }

    private void handlePacket(DatagramSocket socket, DatagramPacket packet) {
        try {
            InetAddress clientAddress = packet.getAddress();
            int clientPort = packet.getPort();

            ClientInfo clientInfo = new ClientInfo(clientAddress, clientPort);
            String message = new String(packet.getData(), 0, packet.getLength());
            int colonIndex = message.indexOf(":");
            if (colonIndex != -1) {
                String username = message.substring(0, colonIndex);
                String msg = message.substring(colonIndex + 1).trim();
                textArea.append(username + ": " + msg + "\n");
            } else {
                textArea.append("Message from " + clientInfo + ": " + message + "\n");
            }

            if (clients.add(clientInfo)) {
                textArea.append("New client connected: " + clientInfo + "\n");
            }

            broadcastMessage(socket, message, clientInfo);

        } catch (Exception e) {
            textArea.append("Error handling packet: " + e.getMessage() + "\n");
        }
    }

    private void broadcastMessage(DatagramSocket socket, String message, ClientInfo sender) {
        byte[] buffer = message.getBytes();
        for (ClientInfo client : clients) {

            if (!client.equals(sender)) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, client.getAddress(),
                            client.getPort());
                    socket.send(packet);
                } catch (Exception e) {
                    textArea.append("Error sending to " + client + ": " + e.getMessage() + "\n");
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UDPServer::new);
    }

    private static class ClientInfo {
        private final InetAddress address;
        private final int port;

        public ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ClientInfo that = (ClientInfo) o;
            return port == that.port && address.equals(that.address);
        }

        @Override
        public int hashCode() {
            return address.hashCode() * 31 + port;
        }

        @Override
        public String toString() {
            return address.getHostAddress() + ":" + port;
        }
    }
}