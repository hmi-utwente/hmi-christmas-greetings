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
 * This agent will send a blocking request to an external python module. 
 * It waits for a response before continuing, or gives a default response after a timeout
 * @author davisond
 *
 */
public class TWSSActor extends Actor {
	  private static Logger logger = LoggerFactory.getLogger(TWSSActor.class.getName());

	  private static final long TIMEOUT = 2000;
	  private static final double TWSS_THRESHOLD = 0.5;
	  
	private static final String INPUT_TOPIC = "/topic/twssOutput";
	private static final String OUTPUT_TOPIC = "/topic/twssInput";
	private TWSSResponseWorker twss;
	private Middleware middleware;
	
	/**
	 * The response from the external module
	 */
	private JsonNode twssResponse = null;

	/**
	 * Tells our middleware worker if we are waiting for a response, or if the response should be discarded
	 */
	public boolean waitingForResponse = false;

	private String twssSpeech = "That's what she said!";
	
	public TWSSActor(String requestTopic, String feedbackTopic) {
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
		
		this.twss = new TWSSResponseWorker();
		
		new Thread(twss).start();
		
        GenericMiddlewareLoader gml = new GenericMiddlewareLoader("nl.utwente.hmi.middleware.stomp.STOMPMiddlewareLoader", ps);
        middleware = gml.load();

        middleware.addListener(twss);	
	}
	
	@Override
	public void provideContext(String input){
		logger.debug("Requesting TWSS for sentence: {}", input);
		//request a response from external module
		twssResponse = null;
		waitingForResponse = true;
		ObjectNode twssReq = om.createObjectNode();
		twssReq.put("sentence", input);
		middleware.sendData(twssReq);
		
		//now we should wait a bit for response from the TWSS module
		//if we get no response after x seconds, we should do default something
		long startTime = System.currentTimeMillis();
		long currTime = startTime;
		
		//now wait for a response :-)
		while(twssResponse == null && (currTime <= startTime + TIMEOUT)){
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
		wantToAct = twssResponse != null && twssResponse.path("score").asDouble(0.0) >= TWSS_THRESHOLD;
	}

	@Override
	public JsonNode generateAction(){
		logger.info("Speaking: {}", twssSpeech);
		
		return buildJSONRequest(buildBML(buildSpeech(twssSpeech)));
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
				twssResponse = jn;
			}
		}

		@Override
		public void receiveData(JsonNode jn) {
			addDataToQueue(jn);
		}
		
	}
	
}
