package liarsdice.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LDServer implements Runnable {

    private enum State {
        PREGAME
    };

    // Networking variables
    private final ServerSocket socket;
    private String ipAddress;
    private final Thread thread;
    private final Map<String, Object> settings;
    private final Map<String, LDServerThread> pendingClients;
    private final Map<String, LDServerThread> clients;
    
    // Game variables
    private State state;

    public LDServer(final Map<String, Object> settings) throws IOException {
        this.settings = settings;
        pendingClients = new HashMap<String, LDServerThread>();
        clients = new HashMap<String, LDServerThread>((Integer) settings.get("maxClients"));
        socket = new ServerSocket(0);
        socket.setSoTimeout(2000);
        state = State.PREGAME;
        thread = new Thread(this);
        thread.start();
    }
    
    synchronized void handle(String clientName, String msg) {
        System.out.println(clientName + ": " + msg);
        
        // Messages from any client
        if (msg.startsWith("QUIT")) {
            disconnect(clientName);
            return;
        }
        
        if (pendingClients.containsKey(clientName)) {
            // Messages from pending clients
            if (msg.startsWith("HELO") && state == State.PREGAME) {
                String newName = msg.substring(5);
                for (int i=1; clients.containsKey(newName); i++)
                    newName = msg.substring(5) + " (" + i + ")";
                //LDServerThread client = clients.put(newName, pendingClients.remove(clientName));
                LDServerThread client = pendingClients.remove(clientName);
                client.setClientName(newName);
                clients.put(newName, client);
                
                String response = "HELO " + newName + " " + (clients.size() - 1);
                for (String name : clients.keySet())
                    if (!name.equals(newName)) {
                        response += " " + name;
                        clients.get(name).send("JOIN " + newName);
                    }
                client.send(response);
            }
        } else if (clients.containsKey(clientName)) {
            // Messages from accepted clients
            
            // Stateless messages
            if (msg.startsWith("CHAT")) {
                sendAll("CHAT " + clientName + ": " + msg.substring(5));
            } else {        
                // State-specific messages
                switch (state) {
                    case PREGAME:
                        break;
                    default:
                        exit();
                }
            }
        }
    }
    
    private void sendAll(String msg) {
        if (state == null) // Exiting, so do not broadcast messages
            return;
        
        System.out.println("Server->All: " + msg);
        
        for (LDServerThread t : clients.values())
            t.send(msg, false);
    }
    
    synchronized void disconnect(String clientName) {
        LDServerThread client;
        if ((client = clients.remove(clientName)) == null) {
            client = pendingClients.remove(clientName);
            if (client == null)
                return;
            client.send("QUIT");
            client.close();
            return;
        }
        client.send("QUIT");
        client.close();
        sendAll("LEFT " + clientName);
        //TODO Remove player from game
    }

    public void exit() {
        state = null;
        for (String name : pendingClients.keySet())
            disconnect(name);
        for (String name : clients.keySet())
            disconnect(name);
    }
    
    public String getIPAddress() {
        if (ipAddress != null)
            return ipAddress;
        URL url;
        try {
            url = new URL("http://automation.whatismyip.com/n09230945.asp");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            return (ipAddress = in.readLine().trim());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            try {
                url = new URL("http://api.externalip.net/ip/");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                return (ipAddress = in.readLine().trim());
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {}
        }
        return null;
    }
    
    public int getPortNumber() {
        return socket.getLocalPort();
    }

    @Override
    public void run() {
        while (state != null) {
            if (state == State.PREGAME) {
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
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
