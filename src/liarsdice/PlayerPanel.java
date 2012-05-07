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
        g.setColor(player.isReady() ? Color.GREEN : Color.BLACK);
        g.drawRect(0, 0, 239, 159);
        
        g.setColor(Color.BLACK);
        g.drawString(player.getName(), 8, 16);
    }
}
