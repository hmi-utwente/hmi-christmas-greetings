package hmi.Twitter2Middleware;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
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
		
		Timer timer = new Timer();
		timer.schedule(new RandomTweetTimer(tf), 2*60*1000);
		
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

class RandomTweetTimer extends TimerTask  {
	Random ran;
	
	TweetForwarder tf;
	public static String[] randomInputs = {
			"What do you call people who are afraid of Santa Claus? Claustrophobic.",
			"What is the difference between snowmen and snowwomen? Snowballs.",
			"STRESSED is just DESSERTS spelled backward.",
			"What nationality is Santa Claus? North Polish.",
			"How do you know when Santa's in the room? You can sense his presents.",
			"Remember, children. The best way to get a puppy for Christmas is to beg for a baby brother.",
			"The 3 stages of man: He believes in Santa Claus. He doesn't believe in Santa Claus. He is Santa Claus.",
			"What do elves learn in school?",
			"If i was the Grinch, I wouldn't steal Christmas. I'd steal you.",
			"What kind of motorbike does Santa ride? A Holly Davidson!",
			"Did you know that Santa's not allowed to go down chimneys this year? It was declared unsafe by the Elf and Safety Commission.",
			"Did you hear about the dyslexic Satanist? He sold his soul to Santa.",
			"I am the ghost of Christmas Future Perfect Subjunctive: I will show you what would have happened were you not to have changed your ways!",
			"How do you know that Santa is a man? No woman wears the same attire every year.",
			"When you stop believing in Santa Claus is when you start getting clothes for Christmas!",
			"How do you scare a snowman? You get a hairdryer!",
			"Why can't the Christmas tree stand up? It doesn't have legs.",
			"What's black and white and red all over? Santa covered with chimney soot."
	};
	
	
    public RandomTweetTimer(TweetForwarder tf) {
    	this.tf = tf;
    	ran =  new Random();
    }

    @Override
    public void run() {
    	int x = ran.nextInt(RandomTweetTimer.randomInputs.length);
		tf.Forward("fakeUser", RandomTweetTimer.randomInputs[x], new String[]{ "poem" }, System.currentTimeMillis() / 1000L);
    }
}