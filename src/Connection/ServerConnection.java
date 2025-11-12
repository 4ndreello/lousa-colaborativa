package Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;
import java.net.InetSocketAddress;

public class ServerConnection {
    private final Socket socket;
    private final PrintWriter output;
    private final BufferedReader input;
    private final Consumer<String> onMessageReceived;

    /**
     * modified constructor to support a connection timeout.
     * @param host server address
     * @param port server port
     * @param timeoutMillis connection timeout in milliseconds
     * @param onMessageReceived callback for messages
     * @throws IOException if connection fails or times out
     */
    public ServerConnection(String host, int port, int timeoutMillis, Consumer<String> onMessageReceived) throws IOException {
        this.socket = new Socket();

        InetSocketAddress address = new InetSocketAddress(host, port);

        this.socket.connect(address, timeoutMillis);

        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.onMessageReceived = onMessageReceived;
    }

    public void startListening() {
        new Thread(this::listen).start();
    }

    private void listen() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                if (onMessageReceived != null) {
                    onMessageReceived.accept(message);
                }
            }
        } catch (IOException e) {
            System.out.println("connection lost: " + e.getMessage());
        }
    }

    public void sendMessage(String msg) {
        if (output != null) {
            output.println(msg);
        }
    }
}