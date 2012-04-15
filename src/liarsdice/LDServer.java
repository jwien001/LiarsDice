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

    private ServerSocket socket;
    private Thread thread;
    private final int maxClients;
    private HashMap<String, Thread> clients;
    private State state = State.PREGAME;

    public LDServer(Map<String, ? extends Object> settings) throws IOException {
        maxClients = (Integer) settings.get("maxClients");
        socket = new ServerSocket(1991);
        socket.setSoTimeout(2000);
        thread = new Thread(this);
        thread.start();
    }
    
    public synchronized void handle(String clientName, String msg) {
        // Stateless messages
        if (msg.startsWith("QUIT")) {
            disconnect(clientName);
            //TODO Remove player from game
        } else if (msg.startsWith("CHAT")) {
            String chatMsg = msg.substring(5);
            for (Thread t : clients.values())
                ; //TODO Send message to each
        } else {        
            // State-based messages
            switch (state) {
                case PREGAME:
                    break;
                default:
                    //TODO Send ERR INVALID back?
            }
        }
    }
    
    public synchronized void disconnect(String clientName) {
        //TODO Close client thread
        clients.remove(clientName);
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
            
            if (clients.size() < maxClients) {
                //TODO Create new client thread and add to map
            } else {
                try {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    out.println("ERR FULL");
                    out.close();
                } catch (IOException e) {
                    try {
                        client.close();
                    } catch (IOException e1) {}
                    continue;
                }
            }
        }
        try {
            socket.close();
        } catch (IOException e) {}
    }
}
