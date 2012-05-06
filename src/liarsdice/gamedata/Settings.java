package liarsdice.gamedata;

public class Settings {
    
    public int maxPlayers;
    public int startingDice;
    public BiddingRule biddingRule;
    public boolean callOutOfOrder;
    public boolean spotOn;
    public boolean onesWild;
    public boolean openWithOnes;
    public boolean palafico;
    
    public Settings() {
        maxPlayers = 4;
        startingDice = 5;
        biddingRule = BiddingRule.INCREASING_QUANTITY;
        callOutOfOrder = false;
        spotOn = false;
        onesWild = true;
        openWithOnes = false;
        palafico = false;
    }
}
