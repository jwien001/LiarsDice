package liarsdice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;


public class LDClient implements Runnable {
    
    private final LDServer server;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread thread;
    private LDListener listener;

    /**
     * Constructs a Liar's Dice client that will attempt to join a game hosted at 
     * the specified IP address.
     * 
     * @param serverAddress the IP address of the server to connect to
     * @param portNumber the port on the server to connect to
     * @param clientName the username to represent the client in the game
     * @param listener the object that should be notified when new data is received from the server
     * @throws IOException if the client fails to connect to the server
     */
    public LDClient(String serverAddress, int portNumber, String clientName, LDListener listener) 
            throws IOException {
        server = null;
        
        init(serverAddress, portNumber, clientName, listener);
    }
    
    /**
     * Spawns a Liar's Dice server with the specified settings, then constructs a client
     * that will connect to that server and attempt to join the game.
     * 
     * @param settings the configuration options for the server
     * @param listener the object that should be notified when new data is received from the server
     * @throws IOException if the server or client fails to initialize properly
     */
    public LDClient(Map<String, Object> settings, LDListener listener) throws IOException {
        server = new LDServer(settings);
        
        init("127.0.0.1", server.getPortNumber(), (String) settings.get("clientName"), listener);
    }
    
    private void init(String ipAddress, int portNumber, String clientName, LDListener listener) 
            throws IOException {
        this.listener = listener;
        socket = new Socket();
        socket.connect(new InetSocketAddress(ipAddress, portNumber), 4000);
        out = new PrintWriter(socket.getOutputStream(), true);        
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        thread = new Thread(this);
        thread.start();
        
        out.println("HELO " + clientName);
    }
    
    /**
     * Removes the client from the game and closes the client's connection to the server.
     * If this client is the game host, this method will close the server as well.
     */
    public void exit() {
        if (server != null)
            server.exit();
        else
            sendToServer("QUIT");
        
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendChat(String msg) {
        sendToServer("CHAT " +  msg);
    }
    
    public String getServerIPAddress() {
        if (server != null)
            return server.getIPAddress();
        else
            return socket.getInetAddress().getHostAddress();
    }
    
    public int getServerPortNumber() {
        if (server != null)
            return server.getPortNumber();
        else
            return socket.getPort();
    }
    
    private void handle(String msg) {
        if (msg.startsWith("QUIT")) {
            exit();
        } else if (msg.startsWith("CHAT")) {
            listener.chatReceived(msg.substring(5));
        }
    }
    
    private void sendToServer(String msg) {
        out.println(msg);
    }

    @Override
    public void run() {
        while (true) {
            try {
                handle(in.readLine());
            } catch (IOException e) {
                e.printStackTrace();
                exit();
                break;
            }
        }
    }
}
