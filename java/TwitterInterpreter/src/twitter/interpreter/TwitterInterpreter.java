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


public class TwitterInterpreter extends AbstractWorker implements MiddlewareListener {
  private static Logger logger = LoggerFactory.getLogger(TwitterInterpreter.class.getName());

	private static final String INPUT_TOPIC = "/topic/Tweets";
	private static final String OUTPUT_TOPIC = "/topic/BehaviourRequests";
	
	private static final String BML_STRING = "<bml id=\"bml1\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" xmlns:sze=\"http://hmi.ewi.utwente.nl/zenoengine\">$bmlcontent$</bml>"; 
	private static final String SPEECH_STRING = "<speech id=\"speech1\" start=\"0\"><text>$speechtext$</text></speech>"; 
	
	private Map<String,Agent> agents;
	
	private Middleware middleware;

	private JSONHelper jh;

	private ObjectMapper om;
	
	public TwitterInterpreter(){
		super();
	}

	public void init() {
		om = new ObjectMapper();

		agents = new HashMap<String,Agent>();
		agents.put("armandia", new Agent("/topic/ASAPArmandiaBmlRequest", "/topic/ASAPArmandiaBmlFeedback"));
		agents.put("zeno", new Agent("/topic/ASAPZenoBmlRequest", "/topic/ASAPZenoBmlFeedback"));
		
		jh = new JSONHelper();
		
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
	public void addDataToQueue(JsonNode jn) {
		queue.add(jn);
	}

	@Override
	public void processData(JsonNode jn) {
		logger.debug("incoming data{}", jn.toString());
		
		if(!jn.path("time").isMissingNode() && !jn.path("user").isMissingNode() && !jn.path("content").isMissingNode()){
			Tweet t = new Tweet(Long.parseLong(jn.path("time").asText()), jn.path("user").asText(), jn.path("content").asText());
			
			String speech = SPEECH_STRING.replace("$speechtext$", t.getContent());
			String bml = BML_STRING.replace("$bmlcontent$", speech);
			
			//TODO: choose an agent at random, or based on a hashtag #zeno or something
			Agent a = agents.get("armandia");
			a.setBml(bml);
			
			ArrayNode requests = om.createArrayNode();
			requests.add(a.buildRequest());
			
			middleware.sendData(requests);
		}
	}
	
	
	
}
