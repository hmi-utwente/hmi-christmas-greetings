package twitter.interpreter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class Actor {
	  private static Logger logger = LoggerFactory.getLogger(Actor.class.getName());

	private String requestTopic;
	private String feedbackTopic;
	protected ObjectMapper om;

	protected String input;

	protected boolean wantToAct;

	protected static final String BML_STRING = "<bml id=\"bml1\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" xmlns:sze=\"http://hmi.ewi.utwente.nl/zenoengine\">$bmlcontent$</bml>"; 
	protected static final String SPEECH_STRING = "<speech id=\"speech1\" start=\"0\"><text>$speechtext$</text></speech>"; 
	
	//TODO: create specific implementations for our supported agents
	public Actor(String requestTopic, String feedbackTopic){
		om = new ObjectMapper();
		this.setRequestTopic(requestTopic);
		this.setFeedbackTopic(feedbackTopic);
		
	}

	public String getRequestTopic() {
		return requestTopic;
	}

	public void setRequestTopic(String requestTopic) {
		this.requestTopic = requestTopic;
	}

	public String getFeedbackTopic() {
		return feedbackTopic;
	}

	public void setFeedbackTopic(String feedbackTopic) {
		this.feedbackTopic = feedbackTopic;
	}

	/**
	 * Does this actor want to act on the given context? (Default: true)
	 * @return whether or not it wants to act on the given context
	 */
	public boolean wantToAct(){
		return wantToAct;
	}
	
	/**
	 * Give this actor the context in which he should act
	 * @param input the context in which to act (what should he respond to?)
	 */
	public void provideContext(String input){
		this.input = input;
		wantToAct = true;
	}
	
	/**
	 * This generates the action for this actor, given the previously provided context
	 * @return a JSON node that contains the STOMP input/output topics and the generated BML for this actor
	 */
	public JsonNode generateAction(){
		logger.debug("Generating BML request for given input: {}", input);
		logger.info("Speaking: {} (Actor: {})", input, this.getClass());
		
		return buildJSONRequest(buildBML(buildSpeech(input)));
	}
	
	/**
	 * This takes a BML block and generates the request for the KerstDM
	 * @param s the speech string
	 * @return a JSON node that contains the STOMP input/output topics and the BML for this agent
	 */
	public JsonNode buildJSONRequest(String bml){
		logger.debug("Generating request for BML: {}", bml);
		
		ObjectNode request = om.createObjectNode();
		request.put("agent", getRequestTopic());
		request.put("agentFeedback", getFeedbackTopic());
		
		request.put("bml", bml);
		
		logger.debug("JSON BML Request: {}", request.toString());
		
		return request;
	} 
	
	/**
	 * Generates a bml speech tag for the given text 
	 * @param speech the speech string
	 * @return a bml speech tag
	 */
	public String buildSpeech(String speech){
		//TODO: add merijn's extra BML stuff
		return SPEECH_STRING.replace("$speechtext$", speech);
	}
	
	/**
	 * Generates a BML block for the given behaviours
	 * @param content the BML behaviours to be enclosed in the BML block
	 * @return a BML block with the given behaviours
	 */
	public String buildBML(String content){
		return BML_STRING.replace("$bmlcontent$", content);
	}
	
}
