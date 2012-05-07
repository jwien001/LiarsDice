package liarsdice.gamedata;

import java.util.ArrayList;

public class GameState {
    
    private Settings settings;
    private ArrayList<Player> players;
    private int numReady;
    private int numWithDice;
    private int totalDice;
    private int currPlayer;
    private boolean palafico;
    private boolean onesWild;
    
    public GameState() {
        settings = null;
        players = new ArrayList<Player>();
        numReady = 0;
        numWithDice = 0;
        totalDice = 0;
        currPlayer = -1;
        palafico = false;
        onesWild = false;
    }
    
    public GameState(Settings settings) {
        this.settings = settings;
        players = new ArrayList<Player>(settings.maxPlayers);
        numReady = 0;
        numWithDice = 0;
        totalDice = 0;
        currPlayer = -1;
        palafico = false;
        onesWild = settings.onesWild && !settings.openWithOnes && !palafico;
    }
    
    /**
     * Returns whether or not all players are ready.
     * 
     * @return true if all players are ready; false otherwise
     */
    public boolean allReady() {
        return numReady == players.size() && players.size() > 0;
    }
    
    public int numPlayersWithDice() {
        return numWithDice;
    }
    
    public int numPlayers() {
        return players.size();
    }
    
    /**
     * Creates a new player with the given name and adds it to the game.
     * 
     * @param name the name of the new player
     * @return the new player, or null if the game is already full
     */
    public Player addPlayer(String name) {
        if (players.size() >= settings.maxPlayers)
            return null;
        
        Player player = new Player(name);
        players.add(player);
        numWithDice++;
        return player;
    }
    
    /**
     * Returns the player in the game with the given name.
     * 
     * @param name the name of the player to get
     * @return the player with the given name, or null if no such player is found
     */
    public Player getPlayer(String name) {
        if (name == null || name.isEmpty())
            return null;
        
        for (Player p : players)
            if (name.equals(p.getName()))
                return p;
        
        return null;
    }
    
    /**
     * Removes the player with the given name from the game.
     * 
     * @param name the name of the player to remove
     * @return the removed player with the given name, or null if no such player is found
     */
    public Player removePlayer(String name) {
        Player p = getPlayer(name);
        
        if (players.remove(p)) {          
            if (p.isReady())
                numReady--;
            
            if (p.getDiceCount() > 0) {
                numWithDice--;
                totalDice -= p.getDiceCount();
            }
            
            return p;
        }
        
        return null;
    }
    
    public void setReady(String name, boolean ready) {
        getPlayer(name).setReady(ready);
        numReady += ready ? 1 : -1;
    }
    
    /**
     * Returns the list of all players in the game.
     * 
     * @return a non-null, possibly empty list of {@link Player}s.
     */
    public ArrayList<Player> getPlayers() {
        return players;
    }
}
