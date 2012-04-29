package liarsdice.core;


public interface LDListener {
    
    void chatMessage(String msg);
    
    void gameUpdate(/* TODO Pass in the game state */);
    
    void gameError(String errorCode);
}
