package liarsdice.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import liarsdice.gamedata.Bid;
import liarsdice.gamedata.GameState;
import liarsdice.gamedata.Player;
import liarsdice.gamedata.Settings;

public class LDClient implements Runnable {
    
    private final LDServer server;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread thread;
    private LDListener listener;
    
    private boolean outputEnabled;
    
    private GameState state;
    private String clientName;

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
    public LDClient(Settings settings, String clientName, LDListener listener) 
            throws IOException {
        server = new LDServer(settings);
        
        init("127.0.0.1", server.getPortNumber(), clientName, listener);
        
        chatMessage("*** Created a server at " + server.getIPAddress() + " on port " + server.getPortNumber());
    }
    
    private void init(String ipAddress, int portNumber, String clientName, LDListener listener) 
            throws IOException {
        if (clientName == null || (clientName = clientName.trim()).length() == 0 || clientName.split("\\s+").length > 1)
            throw new IllegalArgumentException("Client name must not contain any whitespace.");
        
        outputEnabled = true;
        state = null;
        this.clientName = clientName;
        
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
     * Returns the current game state.
     * 
     * @return the game state
     */
    public GameState getGameState() {
        return state;
    }
    
    /**
     * Returns this client's name as recognized by the server.
     * 
     * @return the name of this client
     */
    public String getName() {
        return clientName;
    }
    
    /**
     * Removes the client from the game and closes the client's connection to the server.
     * If this client is the game host, this method will close the server as well.
     */
    public void exit() {
        state = null;
        if (server != null) {
            outputEnabled = false;
            server.exit();
        } else if (outputEnabled) {
            outputEnabled = false;
            sendToServer("QUIT");
        }
        
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sends a chat message to all of the other players.
     * 
     * @param msg the message to send
     */
    public void sendChat(String msg) {
        sendToServer("CHAT " +  msg);
    }
    
    public void setReady(boolean ready) {
        sendToServer((ready ? "" : "NOT") + "READY");
    }
    
    public void bid(Bid bid) {
        sendToServer("BID " + bid);
    }
    
    private synchronized void handle(String msg) {
        if (msg.startsWith("QUIT")) {
            gameError("HOST QUIT");
            exit();
        } else if (msg.startsWith("CHAT")) {
            chatMessage(msg.substring(5));
        } else if (msg.startsWith("JOIN")) {
            state.addPlayer(msg.substring(5));

            gameUpdate();
            chatMessage("*** " + msg.substring(5) + " has joined the game.");
        } else if (msg.startsWith("LEFT")) {
            state.removePlayer(msg.substring(5));
            
            gameUpdate();
            chatMessage("*** " + msg.substring(5) + " has left the game.");
        } else if (msg.startsWith("HELO")) {
            msg = msg.substring(5);
            
            clientName = msg.substring(0, msg.indexOf(" "));
            msg = msg.substring(msg.indexOf(" ") + 1);
            
            state = new GameState(new Settings(msg.substring(0, msg.indexOf("|"))));
            msg = msg.substring(msg.indexOf(Settings.DELIM) + 2);
            
            String[] players = msg.split("\\s+");
            for (int i=0; i<players.length; i+=2) {
                Player p = state.addPlayer(players[i]);
                state.setReady(p.getName(), Boolean.parseBoolean(players[i+1]));
            }
                
            gameUpdate();
        } else if (msg.startsWith("READY")) {
            state.setReady(msg.substring(6), true);
            
            gameUpdate();
        } else if (msg.startsWith("NOTREADY")) {
            state.setReady(msg.substring(9), false);
            
            gameUpdate();
        } else if (msg.startsWith("NEWGAME")) {
            state.startNewGame(false);
            
            String[] gameData = msg.substring(8).split("\\s+");
            state.setCurrentPlayer(gameData[0]);
            
            Player p = state.getPlayer(clientName);
            for (int i=1; i<gameData.length; i++)
                p.getDice()[i-1] = Integer.parseInt(gameData[i]);

            gameUpdate();
            chatMessage("*** The game has begun! " + gameData[0] + " will open the first round.");
        } else if (msg.startsWith("BID")) {
            msg = msg.substring(4);
            int spaceIndex = msg.indexOf(" ");
            
            state.playerBid(msg.substring(0, spaceIndex), new Bid(msg.substring(spaceIndex + 1)));
            
            gameUpdate();
        } else if (msg.startsWith("FATAL")) {
            gameError(msg.substring(6));
            outputEnabled = false;
        } else if (msg.startsWith("ERR")) {
            gameError(msg.substring(4));
        }
    }
    
    private void sendToServer(String msg) {
        out.println(msg);
    }
    
    private void chatMessage(String msg) {
        listener.chatMessage(msg);
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
