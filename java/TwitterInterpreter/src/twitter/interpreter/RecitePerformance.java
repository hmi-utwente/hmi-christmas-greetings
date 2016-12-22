package twitter.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class RecitePerformance implements Performance {

	
	private static final String SNARKY_RESPONSES = "snarky_responses.xml";
	private HashMap<String, Actor> actors;
	private ObjectMapper om;

	public RecitePerformance() {
		om = new ObjectMapper();
		
		actors = new HashMap<String,Actor>();
		actors.put("armandiaREC", new RecitingActor("/topic/ASAPArmandiaBmlRequest", "/topic/ASAPArmandiaBmlFeedback"));
		actors.put("armandiaQA", new QAActor("/topic/ASAPArmandiaBmlRequest", "/topic/ASAPArmandiaBmlFeedback", SNARKY_RESPONSES));
		actors.put("zenoREC", new RecitingActor("/topic/ASAPZenoBmlRequest", "/topic/ASAPZenoBmlFeedback"));
		actors.put("zenoQA", new QAActor("/topic/ASAPZenoBmlRequest", "/topic/ASAPZenoBmlFeedback", SNARKY_RESPONSES));
		actors.put("UMAREC", new RecitingActor("/topic/ASAPUMABmlRequest", "/topic/ASAPUMABmlFeedback"));
		actors.put("UMAQA", new QAActor("/topic/ASAPUMABmlRequest", "/topic/ASAPUMABmlFeedback", SNARKY_RESPONSES));
		actors.put("UMATWSS", new TWSSActor("/topic/ASAPUMABmlRequest", "/topic/ASAPUMABmlFeedback"));
	}

	@Override
	public ArrayNode generateScript(String input) {
		ArrayNode requests = om.createArrayNode();
		
		//the actors that play in this script
		Actor aRec = actors.get("armandiaREC");
		Actor zRec = actors.get("zenoREC");
		Actor zQA = actors.get("zenoQA");
		Actor uTWSS = actors.get("UMATWSS");
		
		//Start the dialogue when a new tweet enters
		aRec.provideContext("Hey, we got a new tweet!");
		requests.add(aRec.generateAction());
		
		zRec.provideContext("Well read it then!");
		requests.add(zRec.generateAction());
		
		//TODO: have eyepi look at a
		//TODO: do something with the lights aswell
		
		//give all actors the context in which they should act
		for(Entry<String,Actor> actor : actors.entrySet()){
			actor.getValue().provideContext(input);
		}
		
		//this is just one form of a theatre piece
		if(aRec.wantToAct()){
			requests.add(aRec.generateAction());
			
			//potential TWSS?
			if(uTWSS.wantToAct()){
				requests.add(uTWSS.generateAction());
			} else if(zQA.wantToAct()) {
				//make the snarky response
				requests.add(zQA.generateAction());
			}
		}
		
		return requests;
	}

}
