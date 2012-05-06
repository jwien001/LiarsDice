package liarsdice.gamedata;

import java.util.ArrayList;

public class GameState {
    
    private Settings settings;
    private ArrayList<Player> players;
    private int numReady;
    private int totalDice;
    private int currPlayer;
    private boolean palafico;
    private boolean onesWild;
    
    public GameState() {
        settings = null;
        players = new ArrayList<Player>();
        numReady = 0;
        totalDice = 0;
        currPlayer = -1;
        palafico = false;
        onesWild = false;
    }
    
    public GameState(Settings settings) {
        this.settings = settings;
        players = new ArrayList<Player>(settings.maxPlayers);
        numReady = 0;
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
        return numReady == players.size();
    }
}
