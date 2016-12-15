package twitter.interpreter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class Agent {

	private String requestTopic;
	private String feedbackTopic;
	private ObjectMapper om;

	protected static final String BML_STRING = "<bml id=\"bml1\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" xmlns:sze=\"http://hmi.ewi.utwente.nl/zenoengine\">$bmlcontent$</bml>"; 
	protected static final String SPEECH_STRING = "<speech id=\"speech1\" start=\"0\"><text>$speechtext$</text></speech>"; 
	
	//TODO: create specific implementations for our supported agents
	public Agent(String requestTopic, String feedbackTopic){
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
	 * This takes a speech string and generates some awesome BML
	 * Default case is to just speak the string
	 * @param s the speech string
	 * @return a JSON node that contains the STOMP input/output topics and the BML for this agent
	 */
	public JsonNode buildRequest(String s){
		String speech = SPEECH_STRING.replace("$speechtext$", s);
		String bml = BML_STRING.replace("$bmlcontent$", speech);
		
		ObjectNode request = om.createObjectNode();
		request.put("agent", getRequestTopic());
		request.put("agentFeedback", getFeedbackTopic());
		//TODO: add merijn's extra BML stuff
		request.put("bml", bml);
		
		return request;
	}
	
}
