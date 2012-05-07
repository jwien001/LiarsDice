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
    
    public Settings(String dataStr) {
        String[] strs = dataStr.split("\\s+");
        maxPlayers = Integer.parseInt(strs[0]);
        startingDice = Integer.parseInt(strs[1]);
        biddingRule = BiddingRule.valueOf(strs[2]);
        callOutOfOrder = Boolean.parseBoolean(strs[3]);
        spotOn = Boolean.parseBoolean(strs[4]);
        onesWild = Boolean.parseBoolean(strs[5]);
        openWithOnes = Boolean.parseBoolean(strs[6]);
        palafico = Boolean.parseBoolean(strs[7]);
    }
    
    @Override
    public String toString() {
        String str = "";
        str += maxPlayers;
        str += " " + startingDice;
        str += " " + biddingRule.name();
        str += " " + (callOutOfOrder ? "true" : "false");
        str += " " + (spotOn ? "true" : "false");
        str += " " + (onesWild ? "true" : "false");
        str += " " + (openWithOnes ? "true" : "false");
        str += " " + (palafico ? "true" : "false");
        str += "|";
        return str;
    }
}
