package KerstDM;

import java.util.List;
import java.util.Properties;
import java.util.ArrayList;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import nl.utwente.hmi.middleware.Middleware;
import nl.utwente.hmi.middleware.MiddlewareListener;
import nl.utwente.hmi.middleware.loader.GenericMiddlewareLoader;
import nl.utwente.hmi.middleware.stomp.StompHandler;
import nl.utwente.hmi.middleware.worker.AbstractWorker;
import pk.aamir.stompj.ErrorHandler;
import pk.aamir.stompj.ErrorMessage;

public class KerstScheduler extends AbstractWorker implements MiddlewareListener, ErrorHandler {
	private static Logger logger = LoggerFactory.getLogger(KerstScheduler.class.getName());
	private static final String INPUT_TOPIC = "/topic/BehaviourRequests";
	private static final String OUTPUT_TOPIC = "/topic/BehaviourFeedback";
	private Middleware middleware;
	private StompHandler feedbackConnection;
	private List<String> subscribedFeedback;
	private QueueProcessingThreat qpt;
	
	public KerstScheduler() {
		
	}
	
	public void init() {
		Properties ps = new Properties();
		ps.put("iTopic", INPUT_TOPIC);
		ps.put("oTopic", OUTPUT_TOPIC);
		
		new Thread(this).start();
		
        GenericMiddlewareLoader gml = new GenericMiddlewareLoader("nl.utwente.hmi.middleware.stomp.STOMPMiddlewareLoader", ps);
        middleware = gml.load();
        middleware.addListener(this);
        
    	feedbackConnection = new StompHandler(
			GenericMiddlewareLoader.getGlobalProperties().getProperty("apolloIP"),
			Integer.parseInt(GenericMiddlewareLoader.getGlobalProperties().getProperty("apolloPort"))
		);
    	
    	subscribedFeedback = new ArrayList<String>();
			
		qpt = new QueueProcessingThreat(feedbackConnection);
		qpt.start();
	}
	

	public void receiveData(JsonNode jn) {
		addDataToQueue(jn);
	}
	
	@Override
	public void addDataToQueue(JsonNode jn) {
		queue.add(jn);
	}

	@Override
	public void processData(JsonNode jn) {
		logger.debug("incoming data{}", jn.toString());
		List<Command> requests = new ArrayList<Command>();
		
		if (jn.isArray()) {
			ArrayNode jna = (ArrayNode) jn;
			for (int i = 0; i < jna.size(); i++) {
				String agent = jna.get(i).get("agent").asText();
				String agentFeedback = jna.get(i).get("agentFeedback").asText();
				String bml = jna.get(i).get("bml").asText();
				logger.debug("CMD "+i+" ("+agent+" | "+agentFeedback+"): "+bml);
				requests.add(new Command(agent, agentFeedback, bml));
				
				if (!subscribedFeedback.contains(agentFeedback)) {
					feedbackConnection.registerCallback(agentFeedback, qpt);
					subscribedFeedback.add(agentFeedback);
				}
			}
		} else {
			logger.debug("Data is not an array!");
		}

		//Requests requests = gsonParser.fromJson(msg.getContentAsString(), Requests.class);
    	if (qpt != null) {
			qpt.addCommands(requests);						    			
		} else {
			logger.debug("qpt is not initialized!");
		}
	}

	@Override
	public void onError(ErrorMessage err) {
		System.out.println("[onError] "+err.getMessage());
	}



}