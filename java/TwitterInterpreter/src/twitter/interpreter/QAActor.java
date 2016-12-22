package twitter.interpreter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import qamatcher.DialogStore;
import qamatcher.DomDialogsParser;

public class QAActor extends Actor {
	private static Logger logger = LoggerFactory.getLogger(QAActor.class.getName());

	private DialogStore store;
	private DomDialogsParser dds;

	private String answer;

	public QAActor(String requestTopic, String feedbackTopic, String qaFile) {
		super(requestTopic, feedbackTopic);
		this.dds = new DomDialogsParser(qaFile);
		this.store = dds.getDialogStore();
	}
	
	@Override
	public void provideContext(String input){
		this.input = input;
		logger.debug("Generating QA response for text: {}", input);
		
		String attName = "type";
		String attValue = "certain";
		store.setAttribute(attName,attValue);
		answer = store.bestMatch(input);
		
		logger.debug("Got QA response: {}", answer);
		
		wantToAct = !answer.equals("");
	}
	
	@Override
	public JsonNode generateAction(){

		logger.debug("Generating BML request for given input: {}", answer);
		logger.info("Speaking: {}", answer);
		
		return buildJSONRequest(buildBML(buildSpeech(answer)));
	}
	

}
