package hmi.Twitter2Middleware;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TestGui {
	
	public static void main(String[] args) {
		TweetForwarder tf = new TweetForwarder();
		tf.init();
		new TestGui(tf);
	}

	public TestGui(TweetForwarder tf) {
		JFrame guiFrame = new JFrame();
	
		guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		guiFrame.setTitle("Simulate tweets");
		guiFrame.setSize(445,60);
		
		guiFrame.setLocationRelativeTo(null);
		
		JTextField textField = new JTextField(30);
		JButton sendButton = new JButton( "Send");
	
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// send, TODO: add dropdown for keywords
				tf.Forward("fakeUser", textField.getText(), new String[]{ "poem" }, System.currentTimeMillis() / 1000L);
				System.out.println("Send: "+textField.getText());
			}
		});
	
		guiFrame.add(textField, BorderLayout.WEST);
		guiFrame.add(sendButton,BorderLayout.EAST);
	
		//make sure the JFrame is visible
		guiFrame.setVisible(true);
	}

}