package liarsdice;


public interface LDListener {
    
    void chatReceived(String msg);
    
    void update(/* TODO Pass in the game state */);
}
