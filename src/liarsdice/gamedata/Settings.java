package liarsdice.gamedata;

public class Settings {
    
    public static final String DELIM = "|";
    
    public int maxPlayers;
    public int startingDice;
    public int maxDiceValue;
    public BiddingRule biddingRule;
    public boolean callOutOfOrder;
    public boolean spotOn;
    public boolean onesWild;
    public boolean openWithOnes;
    public boolean palafico;
    public int delayBetweenRounds;
    
    public Settings() {
        maxPlayers = 4;
        startingDice = 5;
        maxDiceValue = 6;
        biddingRule = BiddingRule.INCREASING_QUANTITY;
        callOutOfOrder = true;
        spotOn = true;
        onesWild = true;
        openWithOnes = false;
        palafico = true;
        delayBetweenRounds = 10;
    }
    
    public Settings(String dataStr) {
        String[] strs = dataStr.split("\\s+");
        int i = 0;
        maxPlayers = Integer.parseInt(strs[i++]);
        startingDice = Integer.parseInt(strs[i++]);
        maxDiceValue = Integer.parseInt(strs[i++]);
        biddingRule = BiddingRule.valueOf(strs[i++]);
        callOutOfOrder = Boolean.parseBoolean(strs[i++]);
        spotOn = Boolean.parseBoolean(strs[i++]);
        onesWild = Boolean.parseBoolean(strs[i++]);
        openWithOnes = Boolean.parseBoolean(strs[i++]);
        palafico = Boolean.parseBoolean(strs[i++]);
        delayBetweenRounds = Integer.parseInt(strs[i++]);
    }
    
    @Override
    public String toString() {
        String str = "";
        str += maxPlayers;
        str += " " + startingDice;
        str += " " + maxDiceValue;
        str += " " + biddingRule.name();
        str += " " + (callOutOfOrder ? "true" : "false");
        str += " " + (spotOn ? "true" : "false");
        str += " " + (onesWild ? "true" : "false");
        str += " " + (openWithOnes ? "true" : "false");
        str += " " + (palafico ? "true" : "false");
        str += " " + delayBetweenRounds;
        str += DELIM;
        return str;
    }
}
