package liarsdice.core;

public interface LDListener {
    
    /**
     * This method is called when the client receives a new chat message.
     * 
     * @param msg the chat message
     */
    void chatMessage(String msg);
    
    /**
     * This method is called when the client's game state has been updated.
     * The updated game state can be accessed with the {@link LDClient#getGameState()} method.
     */
    void gameUpdate();
    
    /**
     * This method is called when the client encounters an error locally or from the server. 
     * The types of error messages are:
     * <ul>
     * <li>HOST QUIT
     * <li>CONNECTION LOST
     * <li>GAME FULL
     * </ul>
     * 
     * @param errorCode a message indicating the type of error
     */
    void gameError(String errorCode);
}
