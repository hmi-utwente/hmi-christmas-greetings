package twitter.interpreter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import nl.utwente.hmi.middleware.Middleware;
import nl.utwente.hmi.middleware.MiddlewareListener;
import nl.utwente.hmi.middleware.helpers.JSONHelper;
import nl.utwente.hmi.middleware.loader.GenericMiddlewareLoader;
import nl.utwente.hmi.middleware.worker.AbstractWorker;

/**
 * Simple class to "interpret" tweets, and generate BML for (multiple) agents
 * The BML can contain speech and behaviours, and is sent to the BehaviourManager module.
 * @author davisond
 *
 */
public class TwitterInterpreter extends AbstractWorker implements MiddlewareListener {
  private static Logger logger = LoggerFactory.getLogger(TwitterInterpreter.class.getName());

	private static final String INPUT_TOPIC = "/topic/Tweets";
	private static final String OUTPUT_TOPIC = "/topic/BehaviourRequests";
	
	private Middleware middleware;

	private ObjectMapper om;
	
	public TwitterInterpreter(){
		super();
	}

	/**
	 * init the middleware instance, create the agent definitions, etc
	 */
	public void init() {
		om = new ObjectMapper();

		//start listening for input from the twitter module
		Properties ps = new Properties();
		ps.put("iTopic", INPUT_TOPIC);
		ps.put("oTopic", OUTPUT_TOPIC);
		
		new Thread(this).start();
		
        GenericMiddlewareLoader gml = new GenericMiddlewareLoader("nl.utwente.hmi.middleware.stomp.STOMPMiddlewareLoader", ps);
        middleware = gml.load();

        middleware.addListener(this);
	}

  	/**
	 * Callback method which is called by the Middleware when a new data package arrives
	 * @param d the recieved data
	 */
	public void receiveData(JsonNode jn)
    {
		addDataToQueue(jn);
	}

	@Override
	public void processData(JsonNode jn) {
		logger.debug("incoming data{}", jn.toString());
		System.out.println("got tweet: "+jn.toString());
		if(!jn.path("time").isMissingNode() && !jn.path("user").isMissingNode() && !jn.path("content").isMissingNode()){
			Tweet t = new Tweet(Long.parseLong(jn.path("time").asText()), jn.path("user").asText(), jn.path("content").asText());
			interpretTweet(t);
		}
	}
	
	/**
	 * Here we look at the content of the tweet to see which agent should do what
	 * Some agents may generate additional nonverbal BML that can be based on the speech
	 * @param t the tweet to interpret
	 */
	private void interpretTweet(Tweet t){
		String content = t.getContent();
		//TODO: make a "Performance Generator", which chooses actors and their roles, and constructs the dialogue (based on the content of the tweet)

		//TODO: make FAQ: listen for some keywords that indicate a question (who, what, why, where, when, etc) combined with a question mark, and use a different QA module that responds to FAQ
		//TODO: make actions: listen some keywords about actions (do, make, say, dance, shake, etc)
		if(content.contains("what")){
			
		}
		
		Performance p = new RecitePerformance();
		ArrayNode script = p.generateScript(t.getContent());
		
		middleware.sendData(script);
	}
	
	
	
}
