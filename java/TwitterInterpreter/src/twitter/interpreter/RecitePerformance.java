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
		actors.put("armandia", new RecitingActor("/topic/ASAPArmandiaBmlRequest", "/topic/ASAPArmandiaBmlFeedback"));
		actors.put("zeno", new QAActor("/topic/ASAPZenoBmlRequest", "/topic/ASAPZenoBmlFeedback", SNARKY_RESPONSES));
		actors.put("UMA", new TWSSActor("/topic/ASAPUMABmlRequest", "/topic/ASAPUMABmlFeedback"));
	}

	@Override
	public ArrayNode generateScript(String input) {
		ArrayNode requests = om.createArrayNode();
		
		//the actors that play in this script
		Actor a = actors.get("armandia");
		Actor z = actors.get("zeno");
		Actor u = actors.get("UMA");
		
		//Start the dialogue when a new tweet enters
		a.provideContext("Hey, we got a new tweet!");
		requests.add(a.generateAction());
		
		z.provideContext("Well read it then!");
		requests.add(z.generateAction());
		
		//TODO: have eyepi look at a
		//TODO: do something with the lights aswell
		
		//give all actors the context in which they should act
		for(Entry<String,Actor> actor : actors.entrySet()){
			actor.getValue().provideContext(input);
		}
		
		//this is just one form of a theatre piece
		if(a.wantToAct()){
			requests.add(a.generateAction());
			
			//potential TWSS?
			if(u.wantToAct()){
				requests.add(u.generateAction());
			} else if(z.wantToAct()) {
				//make the snarky response
				requests.add(z.generateAction());
			}
		}
		
		return requests;
	}

}
