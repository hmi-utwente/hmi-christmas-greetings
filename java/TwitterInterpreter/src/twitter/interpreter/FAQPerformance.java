package twitter.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * This is a FAQ performance, where an agent reads out a question and a different agent gives a response
 * @author davisond
 *
 */
public class FAQPerformance implements Performance {

	private static final String FAQ_responses = "faq_responses.xml";
	private HashMap<String, Actor> actors;
	private ObjectMapper om;

	public FAQPerformance() {
		om = new ObjectMapper();
		
		actors = new HashMap<String,Actor>();
		actors.put("armandiaREC", new RecitingActor("/topic/ASAPArmandiaBmlRequest", "/topic/ASAPArmandiaBmlFeedback"));
		actors.put("armandiaQA", new QAActor("/topic/ASAPArmandiaBmlRequest", "/topic/ASAPArmandiaBmlFeedback", FAQ_responses));
		actors.put("zenoREC", new RecitingActor("/topic/ASAPZenoBmlRequest", "/topic/ASAPZenoBmlFeedback"));
		actors.put("zenoQA", new QAActor("/topic/ASAPZenoBmlRequest", "/topic/ASAPZenoBmlFeedback", FAQ_responses));
		actors.put("UMAREC", new RecitingActor("/topic/ASAPUMABmlRequest", "/topic/ASAPUMABmlFeedback"));
		actors.put("UMAQA", new QAActor("/topic/ASAPUMABmlRequest", "/topic/ASAPUMABmlFeedback", FAQ_responses));
		actors.put("eyePi", new EmotionalActor("/topic/ASAPEyePiBmlRequest", "/topic/ASAPEyePiBmlFeedback"));
		actors.put("hue", new LightingActor("/topic/Hue", "/topic/dummyfeedback"));
	}

	@Override
	public ArrayNode generateScript(String input) {
		ArrayNode requests = om.createArrayNode();
		
		//the actors that play in this script
		Actor aRec = actors.get("armandiaREC");
		Actor zRec = actors.get("zenoREC");
		Actor zQA = actors.get("zenoQA");
		Actor eyePi = actors.get("eyePi");
		Actor hue = actors.get("hue");
		
		//Start the dialogue when a new tweet enters
		aRec.provideContext("Hmm, this is an interesting question!");
		requests.add(aRec.generateAction());
		
		zRec.provideContext("Really? You think so?");
		requests.add(zRec.generateAction());

		aRec.provideContext("Yeah, listen to this.");
		requests.add(aRec.generateAction());

		//have eyePi make facial expression
		eyePi.provideContext(input);
		if(eyePi.wantToAct()){				
			requests.add(eyePi.generateAction());
		}
		
		//set the mood
		hue.provideContext(input);
		if(hue.wantToAct()){
			requests.add(hue.generateAction());
		}
		
		aRec.provideContext(input);
		requests.add(aRec.generateAction());
			
		zRec.provideContext("Well.. That's easy!");
		requests.add(zRec.generateAction());
		
		//do we have an answer..?
		zQA.provideContext(input);
		if(zQA.wantToAct()){
			requests.add(zQA.generateAction());
		} else {
			zRec.provideContext("The answer is always forty two!");
			requests.add(zRec.generateAction());
		}
		
		return requests;
	}

}
