package liarsdice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LDServerThread extends Thread {

    private final LDServer server;
    private final Socket socket;
    private String clientName;
    private final PrintWriter out;
    private final BufferedReader in;
    
    public LDServerThread(LDServer server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        clientName = socket.getInetAddress().getHostAddress();
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    public void send(String msg) {
        System.out.println("Server->" + clientName + ": " + msg);
        out.println(msg);
    }
    
    public void close() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                server.handle(clientName, in.readLine());
            } catch (IOException e) {
                e.printStackTrace();
                server.disconnect(clientName);
                break;
            }
        }
    }
    
    public String getClientName() {
        return clientName;
    }
}
