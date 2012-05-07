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
        onesWild = settings.onesWild && !settings.openWithOnes;
    }
    
    /**
     * Returns whether or not there are 2 or more players and all of them are ready.
     * 
     * @return true if there are 2 or more players and all of them are ready; false otherwise
     */
    public boolean allReady() {
        return players.size() > 1 && numReady == players.size();
    }
    
    public int numPlayersWithDice() {
        return numWithDice;
    }
    
    public int numPlayers() {
        return players.size();
    }
    
    public int getTotalDice() {
        return totalDice;
    }
    
    public void startNewGame(boolean randomize) {
        for (Player p : players)
            p.setDice(new int[settings.startingDice]);
        numWithDice = players.size();
        totalDice = numWithDice * settings.startingDice;
        if (randomize)
            currPlayer = (int) (Math.random() * players.size());
        palafico = false;
        onesWild = settings.onesWild && !settings.openWithOnes;
        
        startNewRound(randomize);
    }
    
    public void startNewRound(boolean randomize) {
        if (randomize)
            for (Player p : players)
                for (int i=0; i<p.getDiceCount(); i++)
                    p.getDice()[i] = (int) (Math.random() * settings.maxDiceValue) + 1;
    }
    
    /**
     * Returns the player whose turn it is.
     * 
     * @return the {@link Player} whose turn it is
     */
    public Player getCurrentPlayer() {
        if (currPlayer < 0 || currPlayer >= players.size())
            return null;
        return players.get(currPlayer);
    }
    
    /**
     * Sets the current player.
     * 
     * @param name the {@link Player} whose turn it is
     */
    public void setCurrentPlayer(String name) {
        currPlayer = players.indexOf(new Player(name));
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
            
            currPlayer %= players.size();
            
            if (players.size() == 1)
                setReady(players.get(0).getName(), false);
            
            return p;
        }
        
        return null;
    }
    
    /**
     * Sets the specified player's ready status to the specified value.
     * 
     * @param name the name of the player
     * @param ready the ready status to set
     */
    public void setReady(String name, boolean ready) {
        Player p = getPlayer(name);
        boolean old = p.isReady();
        p.setReady(ready);
        if (old != ready)
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
