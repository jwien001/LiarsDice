package liarsdice.gamedata;

public class Bid {
    
    public final int quantity;
    public final int value;
    
    public Bid(int quantity, int value) {
        this.quantity = quantity;
        this.value = value;
    }

    @Override
    public String toString() {
        return quantity + " " + value;
    }
}
