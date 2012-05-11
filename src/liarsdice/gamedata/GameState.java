package liarsdice.gamedata;

import java.util.ArrayList;
import java.util.HashMap;

public class GameState {
    
    private Settings settings;
    private ArrayList<Player> players;
    private int numReady;
    private int numWithDice;
    private int totalDice;
    private int currPlayer;
    private Bid lastBid;
    private HashMap<Integer, Integer> minimumBids;
    private boolean palafico;
    private boolean onesWild;
    
    public GameState() {
        settings = null;
        players = new ArrayList<Player>();
        numReady = 0;
        numWithDice = 0;
        totalDice = 0;
        currPlayer = -1;
        lastBid = null;
        minimumBids = new HashMap<Integer, Integer>();
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
        lastBid = null;
        minimumBids = new HashMap<Integer, Integer>();
        palafico = false;
        onesWild = settings.onesWild && !settings.openWithOnes;
    }
    
    public Settings getSettings() {
        return settings;
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
    
    public boolean isPalafico() {
        return palafico;
    }
    
    public void startNewGame(boolean randomize) {
        for (Player p : players)
            p.setDice(new int[settings.startingDice]);
        numWithDice = players.size();
        totalDice = numWithDice * settings.startingDice;
        if (randomize)
            currPlayer = (int) (Math.random() * players.size());
        palafico = false;
        
        startNewRound(randomize);
    }
    
    public void startNewRound(boolean randomize) {
        if (randomize)
            for (Player p : players)
                for (int i=0; i<p.getDiceCount(); i++)
                    p.getDice()[i] = (int) (Math.random() * settings.maxDiceValue) + 1;        

        lastBid = null;
        onesWild = settings.onesWild && !settings.openWithOnes && !palafico;
        
        minimumBids.clear();
        for (int i = 1; i<=totalDice; i++)
            minimumBids.put(i, (onesWild ? 2 : 1));
    }
    
    /**
     * Validates the bid, updates the minimum bids, and selects the next player.
     * 
     * @param name the name of player that made the bid
     * @param bid the bid
     * @return true if the bid is valid and the game state was updated successfully; false otherwise
     */
    public boolean playerBid(String name, Bid bid) {
        // Validate the new bid
        Integer minValue = minimumBids.get(bid.quantity);
        if (minValue == null || bid.value < minValue || (palafico && bid.value != lastBid.value))
            return false;
        
        // Adjust ones being wild in case ones were not called on the first bid in a normal round
        if (lastBid == null && bid.value != 1 && !palafico)
            onesWild = true;
        
        // Update the last bid
        lastBid = bid;
        players.get(currPlayer).setLastBid(bid);
        
        // Select the next player with dice
        do {
            currPlayer = (currPlayer + 1) % players.size();
        } while (players.get(currPlayer).getDiceCount() == 0);        
        
        // Update the minimum bids
        minimumBids.clear();
        if (palafico) {
            for (int i = bid.quantity + 1; i<=totalDice; i++)
                minimumBids.put(i, bid.value); // Higher quantity, same value
        } else {
            switch (settings.biddingRule) {
                case INCREASING_QUANTITY:
                    if (bid.value < settings.maxDiceValue)
                        minimumBids.put(bid.quantity, bid.value + 1); // Same quantity, higher value
                    for (int i = bid.quantity + 1; i<=totalDice; i++)
                        minimumBids.put(i, (onesWild ? 2 : 1)); // Higher quantity, any value
                    break;
                case INCREASING_VALUE:
                    for (int i = bid.value < settings.maxDiceValue ? 1 : bid.quantity + 1; i<=totalDice; i++)
                        if (i > bid.quantity)
                            minimumBids.put(i, bid.value); // Higher quantity, same value
                        else
                            minimumBids.put(i, bid.value + 1); // Any quantity, higher value
                    break;
                case INCREASING_BOTH:
                    if (bid.value < settings.maxDiceValue)
                        minimumBids.put(bid.quantity, bid.value + 1); // Same quantity, higher value
                    for (int i = bid.quantity + 1; i<=totalDice; i++)
                        minimumBids.put(i, bid.value); // Higher quantity, same value
                    break;
                default:
                    throw new IllegalStateException("Unidentified bidding rule encountered!");
            }
        }
        
        return true;
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
     * Checks if the player with the specified name is the current player.
     * 
     * @param name the name of the player to check
     * @return true if the specified player is the current player; false otherwise
     */
    public boolean isCurrentPlayer(String name) {
        if (currPlayer < 0 || currPlayer >= players.size())
            return false;        
        return players.get(currPlayer).getName().equals(name);
    }
    
    /**
     * Returns the last bid made in the current round.
     * 
     * @return the previous {@link Bid}
     */
    public Bid getLastBid() {
        return lastBid;
    }
    
    /**
     * Returns a map of the lowest valid bids. Quantities are mapped to the minimum value allowed for that quantity.
     * 
     * @return a non-null, possibly empty {@link HashMap} of the lowest valid bids
     */
    public HashMap<Integer, Integer> getMinimumBids() {
        return minimumBids;
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
            
            if (players.size() == 1) {
                setReady(players.get(0).getName(), false);
                currPlayer = -1;
            } else {                
                currPlayer %= players.size();
                while (players.get(currPlayer).getDiceCount() == 0) {
                    currPlayer = (currPlayer + 1) % players.size();
                }
            }
            
            return p;
        }
        
        return null;
    }
    
    /**
     * Sets the specified player's ready status to the specified value.
     * 
     * @param name the name of the player
     * @param ready the ready status to set
     * @return true if the ready state was changed, false if no change was made
     */
    public boolean setReady(String name, boolean ready) {
        Player p = getPlayer(name);
        if (p.isReady() != ready) {
            p.setReady(ready);
            numReady += ready ? 1 : -1;
            return true;
        }
        return false;
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
