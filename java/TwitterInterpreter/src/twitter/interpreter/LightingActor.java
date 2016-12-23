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
 * This actor will influence the lighting of the scene based on a sentiment analysis
 * This agent will send a blocking request to an external python module. 
 * It waits for a response before continuing, or gives a default response after a timeout
 * @author davisond
 *
 */
public class LightingActor extends Actor {
	  private static Logger logger = LoggerFactory.getLogger(LightingActor.class.getName());

	  private static final long TIMEOUT = 2000;
	  
	private static final String INPUT_TOPIC = "/topic/sentimentOutput";
	private static final String OUTPUT_TOPIC = "/topic/sentimentInput";
	private SentimentResponseWorker sentimentListener;
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
	
	public LightingActor(String requestTopic, String feedbackTopic) {
		super(requestTopic, feedbackTopic);
		
        init();
	}
	
	/**
	 * Inits the middleware communication with the Sentiment module
	 */
	private void init(){

		Properties ps = new Properties();
		ps.put("iTopic", INPUT_TOPIC);
		ps.put("oTopic", OUTPUT_TOPIC);
		
		this.sentimentListener = new SentimentResponseWorker();
		
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
		
		//Should we make the Sentiment response..?
		wantToAct = sentimentResponse != null;
	}

	@Override
	public JsonNode generateAction(){
		logger.info("Setting the mood lights: {}", sentimentResponse.toString());
		if(sentimentResponse != null){
			return buildJSONRequest(buildBML(buildMiddlewareData(sentimentResponse.toString())));
		} else {
			return buildJSONRequest(buildBML(buildMiddlewareData("{\"empty\":\"empty\"}")));
		}
	}
	
	/**
	 * Very simple worker that just listens for a response from our Sentiment module. If a response is given, the main procesing thread should pick it up
	 * @author davisond
	 *
	 */
	private class SentimentResponseWorker extends AbstractWorker implements MiddlewareListener {

		@Override
		public void processData(JsonNode jn) {
			logger.debug("Got response from Sentiment module: {}", jn.toString());
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
