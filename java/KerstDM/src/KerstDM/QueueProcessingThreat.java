package KerstDM;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractQueue;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.databind.JsonNode;

import nl.utwente.hmi.middleware.stomp.StompHandler;
import pk.aamir.stompj.Message;
import pk.aamir.stompj.MessageHandler;

import static nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.array;
import static nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.object;

public class QueueProcessingThreat extends Thread implements MessageHandler {
	
	Queue<Command> q;
	StompHandler connection;
	
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
		Command c = q.poll();
		
		if (c != null) {
			// process
			String outTopic = c.getAgent();
			String bml = c.getBml();
			
			try {
				JsonNode value = object("bml", object("content", URLEncoder.encode(bml, "UTF-8"))).end();
				connection.sendMessage(value.toString(), outTopic);
				canProcess = false;
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
			
			if (canProcess) {
				process();	
			}
		}
		
	}


	public void setCanProcess(boolean canProcess) {
		this.canProcess = canProcess;
	}

	@Override
	public void onMessage(Message msg) {
		boolean isPresent = isBMLSpeechEndIs(msg.getContentAsString());
		
		if (isPresent){
			setCanProcess(true);
		}
	}
	
	private boolean isBMLSpeechEndIs (String str ){		
		boolean found = false;
		String prefix = "id=\"bml";
		String suffix= ":end\"";
		
		String [] tokens = str.split(" ");
		
		for (int i = 0; i < tokens.length && !found; i++) {
			String t = tokens[i];
			if (t.startsWith(prefix) && t.endsWith(suffix))
				found = true;
		}		
		
		return found;
	}

}
