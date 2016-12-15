package twitter.interpreter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Agent {

	private String requestTopic;
	private String feedbackTopic;
	private String bml;
	private ObjectMapper om;

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

	public String getBml() {
		return bml;
	}

	public void setBml(String bml) {
		this.bml = bml;
	}
	
	public JsonNode buildRequest(){
		ObjectNode request = om.createObjectNode();
		request.put("agent", getRequestTopic());
		request.put("agentFeedback", getFeedbackTopic());
		//TODO: add merijn's extra BML stuff
		request.put("bml", getBml());
		
		return request;
	}
	
}
