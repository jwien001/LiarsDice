package liarsdice;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import liarsdice.gamedata.GameState;
import liarsdice.gamedata.Player;

@SuppressWarnings("serial")
public class PlayerPanel extends JPanel {
    
    private GameState state;
    private Player player;
    
    public void setData(GameState state, Player player) {
        this.state = state;
        this.player = player;
    }
    
    @Override
    public void paintComponent(final Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        
        if (state == null || player == null) {
            g.clearRect(0, 0, getWidth(), getHeight());
            return;
        }        

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 239, 159);
        
        if (!state.allReady())
            if (player.isReady())
                g.setColor(Color.GREEN);
            else
                g.setColor(Color.BLACK);
        else
            if (state.getWinner() == null && player.equals(state.getCurrentPlayer()))
                g.setColor(Color.YELLOW);
            else if (player.equals(state.getWinner()))
                g.setColor(Color.GREEN);
            else if (player.equals(state.getLoser()))
                g.setColor(Color.RED);
            else
                g.setColor(Color.BLACK);
        g.drawRect(0, 0, 239, 159);
        
        g.setColor(Color.BLACK);
        g.drawString(player.getName(), 8, 16);        
        
        if (state.allReady()) {
            if (player.getDiceCount() == 0) {
                g.setColor(new Color(0f, 0f, 0f, 0.2f));
                g.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g.drawString("Last bid: " + (player.getLastBid() == null ? "none" : player.getLastBid().toPrettyString()), 8, 32);
                
                String dice = "Dice:";
                for (int value : player.getDice())
                    dice += " " + (value == 0 ? "?" : value);
                g.drawString(dice, 8, 48);
            }
        }
    }
}
