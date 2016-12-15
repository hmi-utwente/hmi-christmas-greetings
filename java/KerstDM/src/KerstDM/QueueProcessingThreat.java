package KerstDM;

import java.util.AbstractQueue;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


public class QueueProcessingThreat extends Thread {
	
	Queue<Command> q;
	pk.aamir.stompj.Connection connection;
	
	private boolean canProcess;
	
	
	public QueueProcessingThreat (pk.aamir.stompj.Connection connection) {
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
			String feedBackTopic = c.getAgentFeedback();
			String bml = c.getBml();
			
			connection.send(bml, outTopic);
			canProcess = false;
		}
	}
	
	long millis = 100;
	
	@Override
	public void run() {		
		
		while (true) {
			if (canProcess) {
				process();				
				try {
					currentThread().sleep(millis);
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
	

}
