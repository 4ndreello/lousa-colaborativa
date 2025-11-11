package Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class ServerConnection {
    private final Socket socket;
    private final PrintWriter output;
    private final BufferedReader input;
    private final Consumer<String> onMessageReceived;

    public ServerConnection(String host, int port, Consumer<String> onMessageReceived) throws IOException {
        this.socket = new Socket(host, port);
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.onMessageReceived = onMessageReceived;
        // REMOVIDO: new Thread(this::listen).start();
        // Agora esperamos ser chamados explicitamente
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