package liarsdice.gamedata;

public class Player {
    
    private String name;
    private int[] dice;
    private Bid lastBid;
    private boolean ready;
    
    public Player(String name) {
        this.name = name;
        dice = new int[0];
        lastBid = null;
        ready = false;
    }
    
    public String getName() {
        return name;
    }
    
    public int[] getDice() {
        return dice;
    }
    
    public int getNumDice() {
        return dice.length;
    }
    
    public Bid getLastBid() {
        return lastBid;
    }
    
    public boolean isReady() {
        return ready;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDice(int[] dice) {
        this.dice = dice;
    }
    
    public void setLastBid(Bid lastBid) {
        this.lastBid = lastBid;
    }
    
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Player))
            return false;
        Player other = (Player) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
