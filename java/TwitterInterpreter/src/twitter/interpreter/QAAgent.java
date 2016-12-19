package twitter.interpreter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import qamatcher.DialogStore;
import qamatcher.DomDialogsParser;

public class QAAgent extends Agent {
	private static Logger logger = LoggerFactory.getLogger(QAAgent.class.getName());

	private DialogStore store;
	private DomDialogsParser dds;

	public QAAgent(String requestTopic, String feedbackTopic, String qaFile) {
		super(requestTopic, feedbackTopic);
		this.dds = new DomDialogsParser(qaFile);
		this.store = dds.getDialogStore();
	}
	

	@Override
	public JsonNode buildRequest(String s){
		logger.debug("Generating QA response for text: {}", s);
		
		String attName = "type";
		String attValue = "certain";
		store.setAttribute(attName,attValue);
		String answer = store.bestMatch(s);		
		
		logger.debug("Got QA response: {}", answer);
		
		return super.buildRequest(answer);
	}
	

}
