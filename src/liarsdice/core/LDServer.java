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

import liarsdice.gamedata.GameState;
import liarsdice.gamedata.Player;
import liarsdice.gamedata.Settings;

public class LDServer implements Runnable {
    
    private final ServerSocket socket;
    private String ipAddress;
    private final Thread thread;
    private final Settings settings;
    private final Map<String, LDServerThread> pendingClients;
    private final Map<String, LDServerThread> clients;
    
    private GameState state;

    public LDServer(Settings settings) throws IOException {
        this.settings = settings;
        pendingClients = new HashMap<String, LDServerThread>();
        clients = new HashMap<String, LDServerThread>((int)(settings.maxPlayers / 0.75) + 1);
        socket = new ServerSocket(0);
        socket.setSoTimeout(2000);
        state = new GameState(settings);
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
            if (msg.startsWith("HELO") && !state.allReady()) {
                String newName = msg.substring(5);
                for (int i=1; clients.containsKey(newName); i++)
                    newName = msg.substring(5) + "(" + i + ")";
                LDServerThread client = pendingClients.remove(clientName);
                client.setClientName(newName);
                clients.put(newName, client);
                
                state.addPlayer(newName);
                
                String response = "HELO " + newName + " " + settings;
                for (Player p : state.getPlayers()) {
                    response += " " + p.getName() + " " + (p.isReady() ? "true" : "false");
                    if (!p.getName().equals(newName)) {
                        clients.get(p.getName()).send("JOIN " + newName);
                    }
                }
                client.send(response);
            }
        } else if (clients.containsKey(clientName)) {
            // Messages from accepted clients
            if (msg.startsWith("CHAT")) {
                sendAll("CHAT " + clientName + ": " + msg.substring(5));
            } else if (msg.startsWith("READY")) {
                state.setReady(clientName, true);
                sendAll("READY " + clientName);
                
                if (state.allReady()) {
                    state.startNewGame(true);
                    
                    for (Player p : state.getPlayers()) {
                        String newGameMsg = "NEWGAME " + state.getCurrentPlayer().getName();
                        for (int value : p.getDice())
                            newGameMsg += " " + value;
                        clients.get(p.getName()).send(newGameMsg);
                    }
                }
            } else if (msg.startsWith("NOTREADY")) {
                state.setReady(clientName, false);
                sendAll("NOTREADY " + clientName);
            }
        }
    }
    
    private synchronized void sendAll(String msg) {
        if (state == null) // Exiting, so do not broadcast messages
            return;
        
        System.out.println("Server->All: " + msg);
        
        for (LDServerThread t : clients.values())
            t.send(msg, false);
    }
    
    synchronized void disconnect(String clientName) {
        disconnect(clientName, true);
    }
    
    synchronized void disconnect(String clientName, boolean remove) {
        LDServerThread client;
        if ((client = clients.get(clientName)) == null) {
            client = pendingClients.get(clientName);
            if (client == null)
                return;
            if (remove)
                pendingClients.remove(clientName);
            client.send("QUIT");
            client.close();
            return;
        }
        if (remove)
            clients.remove(clientName);
        client.send("QUIT");
        client.close();

        if (state != null)
            state.removePlayer(clientName);
        
        sendAll("LEFT " + clientName);
    }

    public synchronized void exit() {
        state = null;
        for (String name : pendingClients.keySet())
            disconnect(name, false);
        pendingClients.clear();
        for (String name : clients.keySet())
            disconnect(name, false);
        clients.clear();
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
            Socket client;
            try {
                client = socket.accept();
            } catch (IOException e) {
                continue;
            }            
            
            String error = null;
            if (clients.size() >= settings.maxPlayers) {
                error = "GAME FULL";
            } else if (state.allReady()) {
                error = "GAME IN PROGRESS";
            } else {
                try {
                    LDServerThread newClient = new LDServerThread(this, client);
                    pendingClients.put(newClient.getClientName(), newClient);
                    newClient.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if (error != null) {
                try {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    out.println("ERR " + error);
                    System.out.println("Server->" + client.getInetAddress().getHostAddress() + ": ERR " + error);
                    Thread.sleep(400);
                    out.close();
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                } catch (InterruptedException e) {
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
