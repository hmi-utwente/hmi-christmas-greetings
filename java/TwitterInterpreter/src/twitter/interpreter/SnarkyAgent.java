package twitter.interpreter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import qamatcher.DialogStore;
import qamatcher.DomDialogsParser;

public class SnarkyAgent extends Agent {
	  private static Logger logger = LoggerFactory.getLogger(SnarkyAgent.class.getName());

	private static final String QA_FILE = "snarky_responses.xml";
	private DialogStore store;
	private DomDialogsParser dds;

	public SnarkyAgent(String requestTopic, String feedbackTopic) {
		super(requestTopic, feedbackTopic);
		this.dds = new DomDialogsParser(QA_FILE);
		this.store = dds.getDialogStore();
	}
	

	@Override
	public JsonNode buildRequest(String s){
		logger.debug("Generating snarky response for text: {}", s);
		
		String attName = "type";
		String attValue = "certain";
		store.setAttribute(attName,attValue);
		String answer = store.bestMatch(s);		
		
		logger.debug("Got snarky response: {}", answer);
		
		return super.buildRequest(answer);
	}
	

}
