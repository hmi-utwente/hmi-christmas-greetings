package twitter.interpreter;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.utwente.hmi.middleware.Middleware;
import nl.utwente.hmi.middleware.MiddlewareListener;
import nl.utwente.hmi.middleware.loader.GenericMiddlewareLoader;
import nl.utwente.hmi.middleware.worker.AbstractWorker;


/**
 * This actor will use emotional expressions and look at a target
 * This agent will send a blocking request to an external python module. 
 * It waits for a response before continuing, or gives a default response after a timeout
 * @author davisond
 *
 */
public class EmotionalActor extends Actor {
	  private static Logger logger = LoggerFactory.getLogger(EmotionalActor.class.getName());

	  private static final long TIMEOUT = 2000;
	  
	private static final String INPUT_TOPIC = "/topic/sentimentOutput";
	private static final String OUTPUT_TOPIC = "/topic/sentimentInput";
	private TWSSResponseWorker sentimentListener;
	private Middleware middleware;
	
	/**
	 * The response from the external module
	 */
	private JsonNode sentimentResponse = null;

	/**
	 * Tells our middleware worker if we are waiting for a response, or if the response should be discarded
	 */
	public boolean waitingForResponse = false;

	private double polarity = 0.0;

	private double subjectivity = 0.0;
	
	public EmotionalActor(String requestTopic, String feedbackTopic) {
		super(requestTopic, feedbackTopic);
		
        init();
	}
	
	/**
	 * Inits the middleware communication with the TWSS module
	 */
	private void init(){

		Properties ps = new Properties();
		ps.put("iTopic", INPUT_TOPIC);
		ps.put("oTopic", OUTPUT_TOPIC);
		
		this.sentimentListener = new TWSSResponseWorker();
		
		new Thread(sentimentListener).start();
		
        GenericMiddlewareLoader gml = new GenericMiddlewareLoader("nl.utwente.hmi.middleware.stomp.STOMPMiddlewareLoader", ps);
        middleware = gml.load();

        middleware.addListener(sentimentListener);	
	}
	
	@Override
	public void provideContext(String input){
		logger.debug("Requesting Sentiment module for sentence: {}", input);
		//request a response from external module
		sentimentResponse = null;
		waitingForResponse = true;
		ObjectNode sentimentReq = om.createObjectNode();
		sentimentReq.put("sentence", input);
		middleware.sendData(sentimentReq);
		
		//now we should wait a bit for response from the sentiment module
		//if we get no response after x seconds, we should do default something
		long startTime = System.currentTimeMillis();
		long currTime = startTime;
		
		//now wait for a response :-)
		while(sentimentResponse == null && (currTime <= startTime + TIMEOUT)){
			//wait for a response
			//TODO: this is now a busy wait, we could use a locking mechanism or a notify pattern or something :-)
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			currTime = System.currentTimeMillis();
		}
		//TODO: potential threading issues, should synchronise..?
		waitingForResponse = false;
		
		//Should we make the TWSS response..?
		if(sentimentResponse != null){
			polarity = sentimentResponse.path("polarity").asDouble(0.0);
			subjectivity = sentimentResponse.path("subjectivity").asDouble(0.0);
			
			wantToAct = sentimentResponse != null;
		}
	}

	@Override
	public JsonNode generateAction(){
		logger.info("subjectivity: {} polarity: {}", subjectivity, polarity);
		if(polarity <= -0.75){
			return buildJSONRequest(buildBML(buildGestureLexeme("e_angry")));
		} else if (-0.75 <= polarity && polarity <= -0.25){			
			return buildJSONRequest(buildBML(buildGestureLexeme("e_sad")));
		} else if (0.0 <= polarity && polarity <= 0.25){			
			return buildJSONRequest(buildBML(buildGestureLexeme("e_sleepy")));
		} else if (0.25 <= polarity && polarity <= 0.5){			
			return buildJSONRequest(buildBML(buildGestureLexeme("e_amazed")));
		} else if (0.5 <= polarity){			
			return buildJSONRequest(buildBML(buildGestureLexeme("e_excited")));
		} else {
			return buildJSONRequest(buildBML(buildGestureLexeme("e_neutral")));
		}
	}
	
	/**
	 * Very simple worker that just listens for a response from our TWSS module. If a response is given, the main procesing thread should pick it up
	 * @author davisond
	 *
	 */
	private class TWSSResponseWorker extends AbstractWorker implements MiddlewareListener {

		@Override
		public void processData(JsonNode jn) {
			logger.debug("Got response from TWSS module: {}", jn.toString());
			if(waitingForResponse){
				sentimentResponse = jn;
			}
		}

		@Override
		public void receiveData(JsonNode jn) {
			addDataToQueue(jn);
		}
		
	}
	
}
