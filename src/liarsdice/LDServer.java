package liarsdice;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class LDServer implements Runnable {

    private enum State {
        PREGAME
    };

    // Networking variables
    private final ServerSocket socket;
    private final Thread thread;
    private final Map<String, Object> settings;
    private final Map<String, LDServerThread> pendingClients;
    private final Map<String, LDServerThread> clients;
    
    // Game variables
    private State state = State.PREGAME;

    public LDServer(final Map<String, Object> settings) throws IOException {
        this.settings = settings;
        pendingClients = new HashMap<String, LDServerThread>();
        clients = new HashMap<String, LDServerThread>((Integer) settings.get("maxClients"));
        socket = new ServerSocket(1991);
        socket.setSoTimeout(2000);
        thread = new Thread(this);
        thread.start();
    }
    
    public synchronized void handle(String clientName, String msg) {
        System.out.println(clientName + ": " + msg);
        
        // Stateless messages
        if (msg.startsWith("QUIT")) {
            disconnect(clientName);
        } else if (msg.startsWith("CHAT")) {
            String chatMsg = msg.substring(5);
            for (LDServerThread t : clients.values())
                ; //TODO Send message to each
        } else {        
            // State-specific messages
            switch (state) {
                case PREGAME:
                    break;
                default:
                    //TODO Shutdown server
            }
        }
    }
    
    public synchronized void disconnect(String clientName) {
        LDServerThread client;
        if ((client = clients.remove(clientName)) == null)
            client = pendingClients.remove(clientName);
        client.send("QUIT");
        client.close();
        //TODO Remove player from game
    }
    
    /*
     * Note: This should only be called from the host client
     */
    public void exit() {
        //TODO Change game state to something besides PREGAME
        //TODO Tell all clients to quit
    }

    @Override
    public void run() {
        while (state == State.PREGAME) {
            Socket client;
            try {
                client = socket.accept();
            } catch (IOException e) {
                continue;
            }            
            
            if (clients.size() < (Integer) settings.get("maxClients")) {
                try {
                    LDServerThread newClient = new LDServerThread(this, client);
                    pendingClients.put(newClient.getClientName(), newClient);
                    newClient.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    out.println("ERR FULL");
                    out.close();
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
