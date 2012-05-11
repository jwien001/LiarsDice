package liarsdice.gamedata;

public class Bid {
    
    public static final String[] displayNames = {"", "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes"};
    
    public final int quantity;
    public final int value;
    
    public Bid(int quantity, int value) {
        this.quantity = quantity;
        this.value = value;
    }
    
    public Bid(String dataStr) {
        String[] strs = dataStr.split("\\s+");
        int i = 0;
        quantity = Integer.parseInt(strs[i++]);
        value = Integer.parseInt(strs[i++]);
    }
    
    public String toPrettyString() {
        return quantity + " " + displayNames[value];
    }

    @Override
    public String toString() {
        return quantity + " " + value;
    }
}
