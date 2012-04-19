package liarsdice.core;

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
    
    private boolean outputEnabled = true;

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
     * @param clientName the username to represent the client in the game
     * @param listener the object that should be notified when new data is received from the server
     * @throws IOException if the server or client fails to initialize properly
     */
    public LDClient(Map<String, Object> settings, String clientName, LDListener listener) throws IOException {
        server = new LDServer(settings);
        
        init("127.0.0.1", server.getPortNumber(), clientName, listener);
        
        chatReceived("*** Created a server at " + server.getIPAddress() + " on port " + server.getPortNumber());
    }
    
    private void init(String ipAddress, int portNumber, String clientName, LDListener listener) 
            throws IOException {
        if (clientName == null || (clientName = clientName.trim()).length() == 0)
            throw new IllegalArgumentException("Client name must be more than whitespace.");
        
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
        else if (outputEnabled)
            sendToServer("QUIT");
        outputEnabled = false;
        
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
    
    private synchronized void handle(String msg) {
        if (msg.startsWith("QUIT")) {
            //TODO Null game state and update with that
            gameError("HOST QUIT");
            exit();
        } else if (msg.startsWith("CHAT")) {
            chatReceived(msg.substring(5));
        } else if (msg.startsWith("JOIN")) {
            chatReceived("*** " + msg.substring(5) + " has joined the game.");
        } else if (msg.startsWith("LEFT")) {
            chatReceived("*** " + msg.substring(5) + " has left the game.");
        } else if (msg.startsWith("HELO")) {
            gameUpdate();
        } else if (msg.startsWith("ERR")) {
            gameError(msg.substring(4));
            outputEnabled = false;
        }
    }
    
    private void sendToServer(String msg) {
        out.println(msg);
    }
    
    private void chatReceived(String msg) {
        listener.chatReceived(msg);
    }
    
    private void gameUpdate() {
        listener.gameUpdate();
    }
    
    private void gameError(String errorCode) {
        if (outputEnabled)
            listener.gameError(errorCode);
    }

    @Override
    public void run() {
        while (true) {
            try {
                handle(in.readLine());
            } catch (IOException e) {
                e.printStackTrace();
                gameError("CONNECTION LOST");
                exit();
                break;
            }
        }
    }
}
