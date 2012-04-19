package liarsdice;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

public class LiarsDice implements ActionListener, LDListener {
	
	private JFrame frame;
	private JPanel actionPanel;
	private JButton hostButton, joinButton;
	private JPanel chatPanel;
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
		
		@SuppressWarnings("serial")
        JPanel graphicsPanel = new JPanel() {
			public void paintComponent(Graphics g) {
				render((Graphics2D) g);
            }
		};
		graphicsPanel.setPreferredSize(new Dimension(640, 480));
		
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
		
		gamePanel.add(actionPanel, BorderLayout.SOUTH);
		
		frame.add(gamePanel, BorderLayout.CENTER);
		
		chatPanel = new JPanel(new BorderLayout());
		chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));
		
		chatArea = new JTextArea(28, 20);
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

        frame.setVisible(true);
		frame.pack();
	}
	
	public void resetToMainMenu() {
	    client = null;
	    actionPanel.removeAll();
        actionPanel.add(hostButton);
        actionPanel.add(joinButton);
        chatPanel.setVisible(false);
        frame.pack();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		String cmd = evt.getActionCommand();
		
		if ("send".equalsIgnoreCase(cmd)) {
		    client.sendChat(chatField.getText());
			chatField.setText("");
		} else if ("host".equalsIgnoreCase(cmd)) {
		    //TODO Open settings dialog
		    HashMap<String, Object> settings = new HashMap<String, Object>();
		    settings.put("maxClients", 2);
		    
		    try {
                client = new LDClient(settings, "Host", this);
            } catch (IOException e) {
                String msg = "Failed to initialize a game.";
                JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }
		    actionPanel.removeAll();
		    joinGameDialog.dispose();
		    frame.pack();
		} else if ("join".equalsIgnoreCase(cmd)) {
		    joinGameDialog.display();
		} else if ("joinSettings".equalsIgnoreCase(cmd)) {
		    try {
                client = new LDClient(joinGameDialog.getIPAddress(),
                                      joinGameDialog.getPortNumber(),
                                      joinGameDialog.getUsername(),
                                      this);
            } catch (IOException e) {
                String msg = "Failed to connect to the server at " + joinGameDialog.getIPAddress() 
                            + " on port " + joinGameDialog.getPortNumber() + ".";
                JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
                return;
            }
            actionPanel.removeAll();
            joinGameDialog.dispose();
            frame.pack();
		}
	}

    @Override
    public void chatReceived(String msg) {
        chatArea.append(msg + "\n");
    }

    @Override
    public void gameUpdate() {
        //TODO Fill out for real
        chatPanel.setVisible(true);
        frame.pack();
    }
    
    @Override
    public void gameError(String errorCode) {
        if ("FULL".equals(errorCode)) {
            String msg = "The game at " + joinGameDialog.getIPAddress() 
                    + " on port " + joinGameDialog.getPortNumber() + " is full.";
            JOptionPane.showMessageDialog(frame, msg, "Full Game", JOptionPane.ERROR_MESSAGE);
            resetToMainMenu();
        } else if ("CONNECTION LOST".equals(errorCode)) {
            String msg = "The connection to the game was lost.";
            JOptionPane.showMessageDialog(frame, msg, "Connection Lost", JOptionPane.ERROR_MESSAGE);
            resetToMainMenu();
        } else if ("HOST QUIT".equals(errorCode)) {
            String msg = "The host has closed the game.";
            JOptionPane.showMessageDialog(frame, msg, "Host Quit", JOptionPane.ERROR_MESSAGE);
            resetToMainMenu();
        }
    }
	
	public void render(Graphics2D g) {
		g.drawRect(0, 0, 639, 479);
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
