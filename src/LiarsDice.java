import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;



public class LiarsDice implements ActionListener {
	
	private JFrame frame;
	private JPanel chatPanel;
	private JTextArea chatArea;
	private JTextField chatField;

	public static void main(String[] args) {
		new LiarsDice();
	}
	
	public LiarsDice() {
		frame = new JFrame("Liar's Dice");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		JPanel gamePanel = new JPanel(new BorderLayout());
		
		JPanel graphicsPanel = new JPanel() {
			public void paintComponent(Graphics g) {
				render((Graphics2D) g);
            }
		};
		graphicsPanel.setPreferredSize(new Dimension(640, 480));
		
		gamePanel.add(graphicsPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		
		JButton hostButton = new JButton("Host New Game");
		hostButton.setHorizontalTextPosition(SwingConstants.CENTER);
		hostButton.addActionListener(this);
		hostButton.setActionCommand("host");		
		buttonPanel.add(hostButton);
		
		JButton joinButton = new JButton("Join Game");
		joinButton.setHorizontalTextPosition(SwingConstants.CENTER);
		joinButton.addActionListener(this);
		joinButton.setActionCommand("join");		
		buttonPanel.add(joinButton);
		
		gamePanel.add(buttonPanel, BorderLayout.SOUTH);
		
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
		
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if ("send".equalsIgnoreCase(cmd)) {
			chatArea.append(chatField.getText() + "\n");
			chatField.setText("");
		}
		chatPanel.setVisible(true);
		frame.pack();
	}
	
	public void render(Graphics2D g) {
		g.drawRect(0, 0, 639, 479);
	}
}
