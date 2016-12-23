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

	protected static final String BML_STRING = "<bml id=\"bml1\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"  xmlns:mwe=\"http://hmi.ewi.utwente.nl/middlewareengine\" xmlns:sze=\"http://hmi.ewi.utwente.nl/zenoengine\">$bmlcontent$</bml>"; 
	protected static final String SPEECH_STRING = "<speech id=\"speech1\" start=\"0\"><text>$speechtext$</text></speech>"; 
	protected static final String GESTURE_STRING = "<gesture lexeme=\"$lexeme$\" id=\"e2\" start=\"1\"/>"; 
	protected static final String MIDDLEWARE_DATA_STRING = "<mwe:sendJsonMessage id=\"example\" start=\"0\">$json$</mwe:sendJsonMessage>";

	private int syncpointCounter = 0;
	
	String [][] keywordList = {	//gestures
			{" me ", "<gesture id=\"","\" lexeme=\"deictic_self\" start=\"bml",":speech",":sync","\" />"},								
			{"myself", "<gesture id=\"","\" lexeme=\"deictic_self\" start=\"bml",":speech",":sync","\" />"},
			{" you ", "<gesture id=\"","\" lexeme=\"deictic_you\" start=\"bml",":speech",":sync","\" />"},
			{"little", "<gesture id=\"","\" lexeme=\"BEAT_CHOP\" start=\"bml",":speech",":sync","\" />"},
			{" so ", "<gesture id=\"","\" lexeme=\"BEAT_LOW\" start=\"bml",":speech",":sync","\" />"},
			{" not ", "<gesture id=\"","\" lexeme=\"stop\" start=\"bml",":speech",":sync","\" />"},
			{"and then", "<gesture id=\"","\" lexeme=\"contemplate\" start=\"bml",":speech",":sync","\" />"},
			{"right", "<gesture id=\"","\" lexeme=\"indicateright\" start=\"bml",":speech",":sync","\" />"},
			{" men ", "<gesture id=\"","\" lexeme=\"indicateleft\" start=\"bml",":speech",":sync","\" />"},
			{"left", "<gesture id=\"","\" lexeme=\"indicateleft\" start=\"bml",":speech",":sync","\" />"},
			{"types", "<gesture id=\"","\" lexeme=\"indicateleft\" start=\"bml",":speech",":sync","\" />"},
			{"hello", "<faceLexeme id=\"","\" lexeme=\"happy\" start=\"bml",":speech",":sync","\" />"},
			{" ok ", "<faceLexeme id=\"","\" lexeme=\"happy\" start=\"bml",":speech",":sync","\" />"},
			{"good", "<faceLexeme id=\"","\" lexeme=\"joy\" start=\"bml",":speech",":sync","\" />"},
			{"fine", "<faceLexeme id=\"","\" lexeme=\"smile\" start=\"bml",":speech",":sync","\" />"},
			{"stupid", "<faceLexeme id=\"","\" lexeme=\"anger\" start=\"bml",":speech",":sync","\" />"},
			{"scary", "<faceLexeme id=\"","\" lexeme=\"afraid\" start=\"bml",":speech",":sync","\" />"},
			{"fear", "<faceLexeme id=\"","\" lexeme=\"afraid\" start=\"bml",":speech",":sync","\" />"},
			{"crazy", "<faceLexeme id=\"","\" lexeme=\"surprise\" start=\"bml",":speech",":sync","\" />"},
			{"sorry", "<faceLexeme id=\"","\" lexeme=\"sad\" start=\"bml",":speech",":sync","\" />"},
			{"what", "<faceLexeme id=\"","\" lexeme=\"ask\" start=\"bml",":speech",":sync","\" />"},
			{"think", "<faceLexeme id=\"","\" lexeme=\"think\" start=\"bml",":speech",":sync","\" />"},
			{"seems", "<faceLexeme id=\"","\" lexeme=\"think\" start=\"bml",":speech",":sync","\" />"},
			{"know", "<faceLexeme id=\"","\" lexeme=\"furrow\" start=\"bml",":speech",":sync","\" />"},
			//head
			{" yes ", "<head id=\"","\" lexeme=\"NOD\" start=\"bml",":speech",":sync","\" />"},
			{" ok ", "<head id=\"","\" lexeme=\"NOD\" start=\"bml",":speech",":sync","\" />"},
			{" no ", "<head id=\"","\" lexeme=\"SHAKE\" start=\"bml",":speech",":sync","\" />"},
			};
	
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
		String answerleukebml[] = LeukereBMLs(speech,1);
		
		return SPEECH_STRING.replace("$speechtext$", answerleukebml[0]) + answerleukebml[1];
	}

	public String buildGestureLexeme(String lexeme){
		return GESTURE_STRING.replace("$lexeme$", lexeme);
	}

	public String buildMiddlewareData(String json){
		return MIDDLEWARE_DATA_STRING.replace("$json$", json);
	}
	
	/**
	 * Generates a BML block for the given behaviours
	 * @param content the BML behaviours to be enclosed in the BML block
	 * @return a BML block with the given behaviours
	 */
	public String buildBML(String content){
		return BML_STRING.replace("$bmlcontent$", content);
	}
	

	public String[] LeukereBMLs(String answerIN, int bmlSpeechID) {
        //per keyword
         
        answerIN =  answerIN.toLowerCase();
        String[] inOut = {answerIN,""};
        int indexKey = 0;
        if (indexKey==keywordList.length) {
        	return inOut; // we zijn klaar
        } else {
        	replace(inOut, indexKey, keywordList, syncpointCounter, bmlSpeechID);  
        }
        return inOut;
	}
	
	
	 /**Deze recursive methode levert in out de resultaat string op.
	 indexKey is de index van het keyword dat vervangen wordt.
	 counter is de counter die gebruikt wordt voor het eerst volgende sync point.**/
	private void replace(String[] inOut, int indexKey, String[][] keywordList, int syncpointcounter, int bmlSpeechID){
	        // proces de string inout en geef deze een nieuwe waarde voordat je deze
	        // methode weer aanroept 
	        
	        int aantalKw = 0;
	        // tel hoe vaak het kewyord voorkomt in inout.
	        String [] bmlBehaviours = {""};
	        String keyword = keywordList[indexKey][0];
	        String action = "";
	            
	        //bepalen hoeveel keer dit keyword in de anwswerIN zit
	        int idx = 0;
	        //aantalKw = 0;
	        while ((idx = inOut[0].indexOf(keywordList[indexKey][0], idx)) != -1)
	        {
	            idx++;
	            aantalKw++;
	        }
	        //System.out.println("Aantal maal dat \""+keyword+"\" voorkomt: "+aantalKw);
	        
	        if (aantalKw==0){
	        	indexKey++;
	        	if (indexKey==keywordList.length){
	        		return; // we zijn klaar
	        	} else {
	        		replace(inOut, indexKey, keywordList, syncpointcounter, bmlSpeechID); // volgende keyword afhandelen 
	        	}
	        }
	        else { 
	            // vervang de string[] inOut door een nieuwe string 
	        	//code waarin iedere instantie van het keyword vervangt door een stukje string
	        	
	        	int nextIndex = inOut[0].indexOf(keyword), oldIndex = 0;
	        	String tempnew = "";
	        	//counter = 0;
				while(nextIndex != -1) {
					tempnew += inOut[0].substring(oldIndex, nextIndex) + " <sync id=\"sync" + syncpointcounter + "\" /> ";
					//System.out.println("in while loop, tempnew: "+tempnew);
				    oldIndex = nextIndex;
				    nextIndex = inOut[0].indexOf(keyword, nextIndex+1);
				    ++syncpointcounter; 
				}
				tempnew += inOut[0].substring(oldIndex);
				
				inOut[0] = tempnew;
				//System.out.println(inOut[0]);
				
				addBehaviour(inOut, indexKey, keywordList, syncpointcounter, bmlSpeechID, aantalKw);
				
	        	//inOut = ... ;
	        	indexKey++;
	        	if (indexKey == keywordList.length) return;
	        	// roep replace opnieuw aan met het volgende keyword en een nieuwe counter waarde
	        	replace(inOut, indexKey, keywordList, syncpointcounter, bmlSpeechID);
	        } //System.out.println(inOut[0]);
	        
	}
	
	
	public void addBehaviour(String[] inOut, int indexKey, String[][] keywordList, int syncpointcounter, int bmlSpeechID, int aantalKw){
		// dan de gestures toevoegen en relateren aan de syncpoints
		String temp = "";
		if (aantalKw == 0){
			return; //klaar: bij geen keywords hoeft er geen behaviour te worden toegevoegd
		}
		String keyword = keywordList[indexKey][0];
		
		int tellertje = (syncpointcounter - aantalKw); //0;//sync# tellertje
		//System.out.println(syncpointcounter+" "+aantalKw+" "+tellertje);
		
		for (int k =0; k < aantalKw; k++){ //voor elk keyword-occurance (syncpoint in de text) moeten we een behaviour toevoegen
			//System.out.println("k: "+k);
			//for het k-de keyword, de bml tags die gemaakt moeten worden (verzamelen in inOut[1], later achter alle speechtags plaatsten)
			for (int j = 1; j < keywordList[ indexKey ].length; j++){
				//voorbeeld: {"ik", "<gesture id=\"","\" lexeme=\"offer\" start=\"bml",":speech",":sync","\" />"}
				//				0	1				  2									3		  4 	  5
				if (j==0){
					//dit is altijd het keyword, dus niets doen
				}else if (j==1){
					temp = temp+keywordList[indexKey][j]+keyword+k; //"<gesture id=\""+ (k dus voor elk keyword wordt er geteld hoevaak ze voorkomen: resultaat bv: "ik ik" id=ik0, id=ik1)
				}else if (j==2){
					temp = temp+keywordList[indexKey][j]+bmlSpeechID;//"\" lexeme=\"offer\" start=\"bml"+
				}else if (j==3){
					temp = temp+keywordList[indexKey][j]+0;//":speech"+
				}else if (j==4){
					temp = temp+keywordList[indexKey][j]+tellertje;//":sync"+
				}else if (j==5){
					temp = temp+keywordList[indexKey][j];//"\" />"
					//hier is temp een volledige behaviour tag
					//System.out.println("temp j : "+temp);
				}							
				//System.out.print(keywords[ i ][ j ] + " ");
				//System.out.println("temp: "+temp);
				//bmlBehaviours[i] = temp;				
            }
			//hier temp (volledige btag) toevoegen aan een vorige btag
			//System.out.println("temp k : "+temp);
			inOut[1] += temp;
			//System.out.println("inOut[1]: "+inOut[1]);
			temp = "";
			tellertje++;
		}
	}
	
}
