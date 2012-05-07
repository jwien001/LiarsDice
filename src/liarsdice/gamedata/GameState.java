package liarsdice.gamedata;

import java.util.ArrayList;

public class GameState {
    
    private Settings settings;
    private ArrayList<Player> players;
    private int numWithDice;
    private int totalDice;
    private int currPlayer;
    private boolean palafico;
    private boolean onesWild;
    
    public GameState() {
        settings = null;
        players = new ArrayList<Player>();
        numWithDice = 0;
        totalDice = 0;
        currPlayer = -1;
        palafico = false;
        onesWild = false;
    }
    
    public GameState(Settings settings) {
        this.settings = settings;
        players = new ArrayList<Player>(settings.maxPlayers);
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
        if (players == null || players.isEmpty())
            return false;
        
        for (Player p : players)
            if (!p.isReady())
                return false;
        
        return true;
    }
    
    public int numPlayersWithDice() {
        return numWithDice;
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
            if (p.getDiceCount() > 0) {
                numWithDice--;
                totalDice -= p.getDiceCount();
            }
            
            return p;
        }
        
        return null;
    }
}
