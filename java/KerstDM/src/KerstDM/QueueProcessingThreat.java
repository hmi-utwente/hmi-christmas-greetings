package KerstDM;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.AbstractQueue;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.media.jfxmedia.logging.Logger;

import nl.utwente.hmi.middleware.stomp.StompHandler;
import pk.aamir.stompj.Message;
import pk.aamir.stompj.MessageHandler;

import static nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.array;
import static nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.object;

public class QueueProcessingThreat extends Thread implements MessageHandler {
	
	Queue<Command> q;
	StompHandler connection;
	Command c;
	long lastProcess = 0;
	
	private boolean canProcess;
	
	
	public QueueProcessingThreat (StompHandler connection) {
		q = new LinkedBlockingQueue<>();
		this.connection = connection;
		canProcess = false;
	}
	
	public void addCommands (List<Command> commands) {
		int n = q.size();
		q.addAll(commands);
		if (n==0) {
			canProcess = true;
		}
	}
	
	public void process () {
		c = q.poll();
		
		if (c != null) {
			// process
			String outTopic = c.getAgent();
			String bml = c.getBml();
			
			System.out.println("Waiting for end on topic: "+c.getAgentFeedback());
			try {
				JsonNode value = object("bml", object("content", URLEncoder.encode(bml, "UTF-8"))).end();
				connection.sendMessage(value.toString(), outTopic);
				canProcess = false;
				System.out.println("Waiting for feedback on: "+c.getAgentFeedback());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	long millis = 100;
	
	@Override
	public void run() {		

		while (true) {
			
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			long now = (new Date()).getTime();
			if (canProcess || (c!= null && c.getAgentFeedback().equals("/topic/dummyfeedback") || now-lastProcess > 1000*20)) {
				process();
				lastProcess = now;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}


	public void setCanProcess(boolean canProcess) {
		this.canProcess = canProcess;
	}

	@Override
	public void onMessage(Message msg) {
		boolean isPresent = false;
		try {
			isPresent = isBMLSpeechEndIs(URLDecoder.decode(msg.getContentAsString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (isPresent){
			setCanProcess(true);
		}
	}
	
	private boolean isBMLSpeechEndIs (String str ){
		//System.out.println("BML??? "+str);
		boolean found = false;
		String prefix = "id=\"bml";
		String suffix= ":end\"";
		String pattern = "id=\"bml(\\d+):end";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(str);
		
		found = m.find();
		/*
		String [] tokens = str.split(" ");
		
		for (int i = 0; i < tokens.length && !found; i++) {
			String t = tokens[i];
			if (t.startsWith(prefix) && t.endsWith(suffix))
				found = true;
		}		
*/
		if (found) System.out.println("got end tag match in bml:\n "+str);
		//System.out.println("res??? "+found);
		return found;
	}

}
