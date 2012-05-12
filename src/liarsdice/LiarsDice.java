package liarsdice;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import liarsdice.core.LDClient;
import liarsdice.core.LDListener;
import liarsdice.gamedata.Bid;
import liarsdice.gamedata.GameState;
import liarsdice.gamedata.Player;
import liarsdice.gamedata.Settings;

public class LiarsDice implements ActionListener, LDListener {
    
    public static final int MAX_PLAYERS = 8;
    public static final int MAX_DICE = 10;
	
	private JFrame frame;
	private PlayerPanel[] playerPanels;
	private JTextArea messagePanel;
	private JPanel actionPanel, chatPanel;
	private JButton hostButton, joinButton, quitButton, readyButton, bidButton, lieButton, spotOnButton;
	private JComboBox quantityCombo, valueCombo;
	private JTextArea chatArea;
	private JTextField chatField;
	
	private JoinGameDialog joinGameDialog;
	
	private LDClient client;

	public static void main(String[] args) {
		new LiarsDice();
	}
	
	public LiarsDice() {
		frame = new JFrame("Liar's Dice");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.addWindowListener(new WindowListener() {            
            @Override
            public void windowOpened(WindowEvent e) {}
            
            @Override
            public void windowIconified(WindowEvent e) {}
            
            @Override
            public void windowDeiconified(WindowEvent e) {}
            
            @Override
            public void windowDeactivated(WindowEvent e) {}
            
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null)
                    client.exit();
            }
            
            @Override
            public void windowClosed(WindowEvent e) {}
            
            @Override
            public void windowActivated(WindowEvent arg0) {}
        });
		
		joinGameDialog = new JoinGameDialog(this);
		
		JPanel gamePanel = new JPanel(new BorderLayout());
		
        JPanel graphicsPanel = new JPanel(new GridLayout(3, 3));
        graphicsPanel.setPreferredSize(new Dimension(720, 480));
        
        playerPanels = new PlayerPanel[8];
        for (int i=0; i<playerPanels.length; i++) {
            playerPanels[i] = new PlayerPanel();
            playerPanels[i].setPreferredSize(new Dimension(240, 160));
        }
        
        messagePanel = new JTextArea();
        messagePanel.setPreferredSize(new Dimension(240, 160));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        messagePanel.setEditable(false);
        messagePanel.setLineWrap(true);
        messagePanel.setWrapStyleWord(true);
        messagePanel.setText("LIAR'S DICE");
        
        graphicsPanel.add(playerPanels[3]);
        graphicsPanel.add(playerPanels[4]);
        graphicsPanel.add(playerPanels[5]);
        graphicsPanel.add(playerPanels[2]);
        graphicsPanel.add(messagePanel);
        graphicsPanel.add(playerPanels[6]);
        graphicsPanel.add(playerPanels[1]);
        graphicsPanel.add(playerPanels[0]);
        graphicsPanel.add(playerPanels[7]);
		
		gamePanel.add(graphicsPanel, BorderLayout.CENTER);
		
		actionPanel = new JPanel();
		
		hostButton = new JButton("Host New Game");
		hostButton.setHorizontalTextPosition(SwingConstants.CENTER);
		hostButton.addActionListener(this);
		hostButton.setActionCommand("host");		
		actionPanel.add(hostButton);
		
		joinButton = new JButton("Join Game");
		joinButton.setHorizontalTextPosition(SwingConstants.CENTER);
		joinButton.addActionListener(this);
		joinButton.setActionCommand("join");		
		actionPanel.add(joinButton);
		
		quitButton = new JButton("Quit");
		quitButton.setHorizontalTextPosition(SwingConstants.CENTER);
		quitButton.addActionListener(this);
		quitButton.setActionCommand("quit");
		
		readyButton = new JButton("Ready");
		readyButton.setHorizontalTextPosition(SwingConstants.CENTER);
		readyButton.addActionListener(this);
		readyButton.setActionCommand("ready");
		
		bidButton = new JButton("Bid");
		bidButton.setHorizontalTextPosition(SwingConstants.CENTER);
		bidButton.addActionListener(this);
		bidButton.setActionCommand("bid");
        
        lieButton = new JButton("\"Lie!\"");
        lieButton.setHorizontalTextPosition(SwingConstants.CENTER);
        lieButton.addActionListener(this);
        lieButton.setActionCommand("lie");
        
        spotOnButton = new JButton("\"Spot On!\"");
        spotOnButton.setHorizontalTextPosition(SwingConstants.CENTER);
        spotOnButton.addActionListener(this);
        spotOnButton.setActionCommand("spotOn");
        
        quantityCombo = new JComboBox();
        quantityCombo.addActionListener(this);
        quantityCombo.setActionCommand("quantity");
        
        valueCombo = new JComboBox();
		
		gamePanel.add(actionPanel, BorderLayout.SOUTH);
		
		frame.add(gamePanel, BorderLayout.CENTER);
		
		chatPanel = new JPanel(new BorderLayout());
		chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));
		
		chatArea = new JTextArea(27, 20);
		chatArea.setEditable(false);
		chatArea.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(true);
		final JScrollPane chatScollPane = new JScrollPane(chatArea);
		chatScollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {			
			BoundedRangeModel brm = chatScollPane.getVerticalScrollBar().getModel();
			boolean wasAtBottom = true;
			
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (!brm.getValueIsAdjusting()) {
					if (wasAtBottom)
						brm.setValue(brm.getMaximum());
				} else
					wasAtBottom = ((brm.getValue() + brm.getExtent()) == brm.getMaximum());
			
			 }
		});
		chatPanel.add(chatScollPane, BorderLayout.CENTER);
		
		JPanel chatInputPanel = new JPanel();	
		
		chatField = new JTextField(20);
		chatField.addActionListener(this);
		chatField.setActionCommand("send");
		chatInputPanel.add(chatField);
		
		JButton sendButton = new JButton("Send");
		sendButton.setHorizontalTextPosition(SwingConstants.CENTER);
		sendButton.addActionListener(this);
		sendButton.setActionCommand("send");
		chatInputPanel.add(sendButton);
	
		chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
		
		chatPanel.setVisible(false);
		frame.add(chatPanel, BorderLayout.EAST);

		setupFavicon();

		frame.setVisible(true);
		frame.pack();
	}

	public void resetToMainMenu() {
	    client = null;
        chatPanel.setVisible(false);
        chatArea.setText(null);
	    actionPanel.removeAll();
        actionPanel.add(hostButton);
        actionPanel.add(joinButton);
        messagePanel.setText("LIAR'S DICE");
        frame.setTitle("Liar's Dice");
        for (PlayerPanel p : playerPanels) {
            p.setData(null, null);
            p.repaint();
        }
        frame.pack();
        frame.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		String cmd = evt.getActionCommand();
		
		if ("send".equalsIgnoreCase(cmd)) {
		    client.sendChat(chatField.getText());
			chatField.setText("");
		} else if ("host".equalsIgnoreCase(cmd)) {
		    //TODO Open settings dialog
		    Settings settings = new Settings();
		    
		    synchronized (this) {
    		    try {
                    client = new LDClient(settings, "Host", this);
                } catch (IOException e) {
                    String msg = "Failed to initialize a game.";
                    JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(frame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                }
                actionPanel.removeAll();
                frame.pack();
                joinGameDialog.dispose();
		    }
		} else if ("join".equalsIgnoreCase(cmd)) {
		    joinGameDialog.display();
		} else if ("joinSettings".equalsIgnoreCase(cmd)) {
		    //TODO Validate join settings dialog
		    
		    synchronized (this) {
    		    try {
                    client = new LDClient(joinGameDialog.getIPAddress(),
                                          joinGameDialog.getPortNumber(),
                                          joinGameDialog.getUsername(),
                                          this);
                } catch (IOException e) {
                    String msg = "Failed to connect to the server at " + joinGameDialog.getIPAddress() 
                                + " on port " + joinGameDialog.getPortNumber() + ".";
                    JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(frame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                }
                actionPanel.removeAll();
                frame.pack();
                joinGameDialog.dispose();
		    }
		} else if ("quit".equalsIgnoreCase(cmd)) {
		    String msg = "Are you sure you want to quit this game?";
		    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame, msg, "Quit Game", JOptionPane.YES_NO_OPTION)) {
		        client.exit();
		        resetToMainMenu();
		    }
		} else if ("ready".equalsIgnoreCase(cmd)) {
		    client.setReady(true);
		    readyButton.setText("Not Ready");
            readyButton.setActionCommand("notready");
		} else if ("notready".equalsIgnoreCase(cmd)) {
            client.setReady(false);
            readyButton.setText("Ready");
            readyButton.setActionCommand("ready");
        } else if ("bid".equalsIgnoreCase(cmd)) {
            client.bid(new Bid((Integer) quantityCombo.getSelectedItem(), Arrays.asList(Bid.displayNames).indexOf(valueCombo.getSelectedItem())));
        } else if ("lie".equalsIgnoreCase(cmd)) {
            client.callLie();
        } else if ("spotOn".equalsIgnoreCase(cmd)) {
            client.callSpotOn();
        } else if ("quantity".equalsIgnoreCase(cmd)) {
            HashMap<Integer, Integer> minBids = client.getGameState().getMinimumBids();
            
            String selected = (String) valueCombo.getSelectedItem();
            valueCombo.removeAllItems();
            for (int i = minBids.get(quantityCombo.getSelectedItem()); i<=client.getGameState().getSettings().maxDiceValue; i++)
                valueCombo.addItem(Bid.displayNames[i]);
            valueCombo.setSelectedIndex(0);
            valueCombo.setSelectedItem(selected);
        }
	}

    @Override
    public void chatMessage(String msg) {
        chatArea.append(msg + "\n");
    }

    @Override
    public synchronized void gameUpdate() {
        GameState state = client.getGameState();
        
        frame.setTitle("Liar's Dice - " + client.getName());
    
        if (!state.allReady()) {
            chatPanel.setVisible(true);
            
            // Remove Bid button
            if (bidButton.getParent() == actionPanel)
                actionPanel.remove(bidButton);
            
            // Remove value combo box
            if (valueCombo.getParent() == actionPanel)
                actionPanel.remove(valueCombo);
            
            // Remove quantity combo box
            if (quantityCombo.getParent() == actionPanel)
                actionPanel.remove(quantityCombo);
            
            //Remove Spot On button
            if (spotOnButton.getParent() == actionPanel)
                actionPanel.remove(spotOnButton);
            
            //Remove Lie button
            if (lieButton.getParent() == actionPanel)
                actionPanel.remove(lieButton);
            
            // Add Quit button
            if (quitButton.getParent() != actionPanel)
                actionPanel.add(quitButton);
            
            // Add Ready button
            if (readyButton.getParent() != actionPanel) {
                readyButton.setText("Ready");
                readyButton.setActionCommand("ready");
                actionPanel.add(readyButton, 0);
            }
            
            messagePanel.setText("Waiting for more players...\nPress Ready to start the game.\nThe game will begin when all players are ready.");
        } else if (state.getWinner() != null) {
            actionPanel.removeAll();
            actionPanel.add(quitButton);
        } else if (state.getCurrentPlayer() != null) {
            messagePanel.setText("Total dice: " + state.getTotalDice());
            if (state.getLastBid() != null)
                messagePanel.append("\nLast bid: " + state.getLastBid().toPrettyString() + " by " + state.getPreviousPlayer().getName());
            if (state.isPalafico())
                messagePanel.append("\nPalafico round!");
            messagePanel.append("\nOnes are currently" + (state.areOnesWild() ? "" : " not") + " wild.");
            messagePanel.append("\n" + state.getCurrentPlayer().getName() + "'s turn");
            
            // Remove ready button
            if (readyButton.getParent() == actionPanel)
                actionPanel.remove(readyButton);
            
            // If a bid has been made and this player's turn or out-of-order calls allowed
            if (state.getLastBid() != null && (state.isCurrentPlayer(client.getName()) 
                    || (state.getSettings().callOutOfOrder 
                            && state.getPlayer(client.getName()).getDiceCount() > 0 
                            && !state.isPreviousPlayer(client.getName())))) {                
                // Add Spot On button
                if (state.getSettings().spotOn && spotOnButton.getParent() != actionPanel)
                    actionPanel.add(spotOnButton, 0);
                
                // Add Lie button
                if (lieButton.getParent() != actionPanel)
                    actionPanel.add(lieButton, 0);
            } else {                
                //Remove Spot On button
                if (spotOnButton.getParent() == actionPanel)
                    actionPanel.remove(spotOnButton);
                
                //Remove Lie button
                if (lieButton.getParent() == actionPanel)
                    actionPanel.remove(lieButton);
            }            

            HashMap<Integer, Integer> minBids = state.getMinimumBids();
            
            // If this player's turn and there are bidding options
            if (state.isCurrentPlayer(client.getName()) && !minBids.isEmpty()) {                
                // Add Bid button
                if (bidButton.getParent() != actionPanel)
                    actionPanel.add(bidButton, 0);
                
                List<Integer> quantities = new ArrayList<Integer>(minBids.keySet());
                Collections.sort(quantities);
                
                quantityCombo.removeActionListener(this);
                quantityCombo.removeAllItems();
                for (Integer i : quantities)
                    quantityCombo.addItem(i);
                quantityCombo.setSelectedIndex(0);
                quantityCombo.addActionListener(this);
                
                valueCombo.removeAllItems();
                if (state.isPalafico() && state.getLastBid() != null)
                    valueCombo.addItem(Bid.displayNames[minBids.get(quantities.get(0))]);
                else
                    for (int i = minBids.get(quantities.get(0)); i<=state.getSettings().maxDiceValue; i++)
                        valueCombo.addItem(Bid.displayNames[i]);
                valueCombo.setSelectedIndex(0);
                
                // Add value combo box
                if (valueCombo.getParent() != actionPanel)
                    actionPanel.add(valueCombo, 0);
                
                // Add quantity combo box
                if (quantityCombo.getParent() != actionPanel)
                    actionPanel.add(quantityCombo, 0);
            } else {
                // Remove Bid button
                if (bidButton.getParent() == actionPanel)
                    actionPanel.remove(bidButton);
                
                // Remove value combo box
                if (valueCombo.getParent() == actionPanel)
                    actionPanel.remove(valueCombo);
                
                // Remove quantity combo box
                if (quantityCombo.getParent() == actionPanel)
                    actionPanel.remove(quantityCombo);
            }
        }

        // Players should be displayed clockwise from this user, which will always be displayed at the bottom
        // Relative index is from this player
        int relativeIndex = 0;
        int nextPanel = 0;
        for (int x=0; x<MAX_PLAYERS; x++) {
            if (x == nextPanel) {
                // Translate the relative index into the actual index in the player list
                int actualIndex = (state.getPlayers().indexOf(state.getPlayer(client.getName())) + relativeIndex) % state.numPlayers();
                playerPanels[x].setData(state, state.getPlayers().get(actualIndex));
                
                // Calculate the which panel should display the next player
                nextPanel = Math.round((float) MAX_PLAYERS *  (float) ++relativeIndex / (float) state.numPlayers());
            } else
                playerPanels[x].setData(null, null);
            
            playerPanels[x].repaint();
        }
        frame.pack();
        frame.repaint();
        
        if (state.getWinner() != null) {
            // Determine who called out who
            Player defendant, challenger;
            if (state.isPreviousPlayer(state.getWinner().getName())) {
                defendant = state.getWinner();
                challenger = state.getLoser();
            } else {
                defendant = state.getLoser();
                challenger = state.getWinner();
            }
            
            for (int i=state.getSettings().delayBetweenRounds; i>0; i--) {
                messagePanel.setText(challenger.getName() + " called " + (state.getSpotOnCall() ? "Spot On" : "Lie") 
                        + " on " + defendant.getName() + "'s bid of " + state.getLastBid().toPrettyString() + ".");
                
                if (state.numPlayersWithDice() == 1)
                    messagePanel.append("\n" + state.getWinner().getName() + " wins the round and the game!");
                else
                    messagePanel.append("\n" + state.getWinner().getName() + " wins the round!");
                
                if (state.getLoser().getDiceCount() == 1)
                    messagePanel.append(" " + state.getLoser().getName() + " is out of the game.");
                else
                    messagePanel.append(" " + state.getLoser().getName() + " loses a die.");
                
                if (state.numPlayersWithDice() == 1)
                    messagePanel.append("\nThe game will end in " + i + " seconds.");
                else
                    messagePanel.append("\nThe next round will begin in " + i + " seconds.");
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Override
    public void gameError(String errorCode) {
        if (errorCode.startsWith("GAME FULL")) {
            String msg = "The game at " + joinGameDialog.getIPAddress() + " on port " + joinGameDialog.getPortNumber() + " is full.";
            JOptionPane.showMessageDialog(frame, msg, "Full Game", JOptionPane.ERROR_MESSAGE);
            resetToMainMenu();
        } else if (errorCode.startsWith("GAME IN PROGRESS")) {
            String msg = "The game at " + joinGameDialog.getIPAddress() + " on port " + joinGameDialog.getPortNumber() + " is already in progress."
                    + " You may be able to join when the game has finished.";
            JOptionPane.showMessageDialog(frame, msg, "Game In Progress", JOptionPane.ERROR_MESSAGE);
            resetToMainMenu();
        } else if (errorCode.startsWith("CONNECTION LOST")) {
            String msg = "The connection to the game was lost.";
            JOptionPane.showMessageDialog(frame, msg, "Connection Lost", JOptionPane.ERROR_MESSAGE);
            resetToMainMenu();
        } else if (errorCode.startsWith("HOST QUIT")) {
            String msg = "The host has closed the game.";
            JOptionPane.showMessageDialog(frame, msg, "Host Quit", JOptionPane.ERROR_MESSAGE);
            resetToMainMenu();
        } else if (errorCode.startsWith("OUT OF TURN")) {
            String msg = "It is not your turn, you cannot do that. (" + errorCode.substring(12) + ")";
            JOptionPane.showMessageDialog(frame, msg, "Out of Turn", JOptionPane.WARNING_MESSAGE);
        } else if (errorCode.startsWith("INVALID BID")) {
            String msg = "Your bid was below the minimum required bid. (" + errorCode.substring(12) + ")";
            JOptionPane.showMessageDialog(frame, msg, "Invalid Bid", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Set up an os-dependent icon.
     */
	private void setupFavicon() {
		String fav = "favicon_other.png";
		
		// OSX dock doesn't work the same way. Investigating.
		/*if (System.getProperty("os.name").contains("mac")) {
            //fav = "favicon_osx.png";
			
        }*/
		
		fav = "resources/" + fav;
		
		Image img = null;
		try {
			img = (new ImageIcon(ClassLoader.getSystemResource(fav))).getImage();
		} catch (final Exception noImageinJar) {
			try {
				img = ImageIO.read(new File(fav));
			} catch (final IOException nope) {
				System.err.println("Cannot locate favicon.");
			}
		}
		if(img != null) {
			frame.setIconImage(img);
		}
	}
}

@SuppressWarnings("serial")
class JoinGameDialog extends JFrame implements ActionListener {
    
    private JTextField ipAddrField;
    private JTextField portNumField;
    private JTextField usernameField;
    
    public JoinGameDialog(ActionListener listener) {
        super("Join Game");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        JPanel inputPanel = new JPanel();
        
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.add(new JLabel("IP Address:"));
        labelPanel.add(new JLabel("Port Number:"));
        labelPanel.add(new JLabel("Username:"));
        inputPanel.add(labelPanel);
        
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        ipAddrField = new JTextField(10);
        fieldPanel.add(ipAddrField);
        portNumField = new JTextField(4);
        fieldPanel.add(portNumField);
        usernameField = new JTextField(10);
        fieldPanel.add(usernameField);
        inputPanel.add(fieldPanel);
        
        add(inputPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        
        JButton okButton = new JButton("OK");
        okButton.setHorizontalTextPosition(SwingConstants.CENTER);
        okButton.addActionListener(listener);
        okButton.setActionCommand("joinSettings");        
        buttonPanel.add(okButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("cancel");        
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public String getIPAddress() {
        return ipAddrField.getText();
    }
    
    public int getPortNumber() {
        return Integer.valueOf(portNumField.getText());
    }
    
    public String getUsername() {
        return usernameField.getText();
    }
    
    public void display() {
        pack();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        
        if ("cancel".equalsIgnoreCase(cmd)) {
            this.dispose();
        }
    }
}
