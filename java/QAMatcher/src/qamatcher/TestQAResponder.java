package qamatcher;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;


import com.sun.xml.internal.ws.util.StringUtils;

import pk.aamir.stompj.Connection;
import pk.aamir.stompj.ErrorHandler;
import pk.aamir.stompj.ErrorMessage;
import pk.aamir.stompj.Message;
import pk.aamir.stompj.MessageHandler;
import pk.aamir.stompj.StompJException;

public class TestQAResponder implements ErrorHandler, MessageHandler {

	
	String [][] keywordList = {{"ik", "<gesture id=\"","\" lexeme=\"offer\" start=\"speech",":sync","\" />"},
			   {"auto", "<gaze id=\"","\" target=\"car\"    start=\"speech",":sync","\" />"}
			  };
	
	//static Connection con;
    private Connection con;
	private static String inTopic;
	private static String outTopic;
	private static String feedbackTopic;
	private static String speechfinalTopic;
    private long bmlId = new Date().getTime();
    private static long i=0;
    private Boolean readyforBML = true;
    private String leukebmlcalls = "";
    
    static String apolloIP = "127.0.0.1";
	static int apolloPort = 61613;
	static TestQAResponder testqaresponder = new TestQAResponder(apolloIP, apolloPort);
	
	
	//the specification of the matching Q and A's in directory resources
	String filename = "vragen.xml"; //staat in de %ROOT%/resource/qamatcher/vragen.xml
			
	//de qa parser etc
	DomDialogsParser ddp = new DomDialogsParser(filename);
	DialogStore store= ddp.getDialogStore();
    
    public TestQAResponder(String apolloIP, int apolloPort){
		inTopic = "/topic/CleVRCmdFeedback";//CleVRFeedback dingetje
		outTopic = "/topic/bmlRequests";//CleVRCmd
		feedbackTopic = "/topic/bmlFeedback"; //asap bml feedback
		speechfinalTopic = "/topic/speechFinal";
		//inTopic = "/topic/bmlFeedback";//CleVRFeedback dingetje
		//outTopic = "/topic/bmlRequests";//CleVRCmd
		//<MiddlewareProperty name="iTopic" value="/topic/CleVRFeedback"/>
        //<MiddlewareProperty name="oTopic" value="/topic/CleVRCmd"/>
        //<MiddlewareProperty name="iTopic" value="/topic/bmlRequests"/>
        //<MiddlewareProperty name="oTopic" value="/topic/bmlFeedback"/>
        //bmlId = 0;

        try {
            con = new Connection(apolloIP, apolloPort, "admin", "password");
            con.setErrorHandler(this);
			con.connect();
		} catch (StompJException e) {
			System.out.println("Error while initialising STOMP connection: "+e.getMessage());
			e.printStackTrace();
			return;
		}
        
        //topiclisteners clevr
		con.subscribe(inTopic, true);
		con.addMessageHandler(inTopic, this);
		
		//topic speech asr ding
		con.subscribe(speechfinalTopic, true);
		con.addMessageHandler(speechfinalTopic, this);
		
		//topiclisteners bmlfeedback
		con.subscribe(feedbackTopic, true);
		con.addMessageHandler(feedbackTopic, this);
    }
    
   
	
	public static void main(String[] args){
		String query = "";
		System.out.print("Question: ");
		//while (true){ //(query = Console.readString()) !=""
		while (true){ //(query = Console.readString()) !=""){
			//TODO hier nog command voor afsluiten ontvangen en handelen? 
			
		}
		//answer = LeukereBMLs(answer);
	}
	
	
	public String[] LeukereBMLs(String answerIN, int bmlSpeechID) {
		String[] answerOUT = {answerIN,""};
		return answerOUT;
//		int aantalKw = 0;
//		String answerOUT[]= {"",""};
//		//leukebmlcalls = "";
//		String [] bmlBehaviours = {""};
//		
//		String keyword = "";
//		String action = "";
//		
//		
//		
//		//per keyword
//		for (int i = 0; i < keywordList.length; i++){
//			keyword = keywordList[i][0];
//			int counter = 0;
//			
//			//bepalen hoeveel van dit keyword in de anwswerIN zit
//			int idx = 0;
//			aantalKw = 0;
//			while ((idx = answerIN.indexOf(keywordList[i][0], idx)) != -1)
//			{
//			   idx++;
//			   aantalKw++;
//			}
//			//System.out.println("Aantal maal dat \""+keyword+"\" voorkomt: "+aantalKw);
//			
//			if (aantalKw == 0){
//				
//				answerOUT[0] = answersyncpoints(answerIN,keyword,counter);
//				
////				// eerst de syncpoints plaatsen in de answerIN				
////				int nextIndex = answerIN.indexOf(keyword), oldIndex = 0, counter = 0;
////				while(nextIndex != -1) {
////				    answerOUT[0] += answerIN.substring(oldIndex, nextIndex-1) + " <sync id=\"sync" + counter + "\" /> ";
////				    oldIndex = nextIndex;
////				    nextIndex = answerIN.indexOf(keyword, nextIndex+1);
////				    ++counter; 
////				}
////				answerOUT[0] += answerIN.substring(oldIndex);
////				//System.out.println(answerOUT);
//				
//				String temp = "";
//				int idtellertje = 0;
//				
//				// dan de gestures toevoegen en relateren aan de syncpoints 
//				for (int k =0; k < aantalKw; k++){
//					
//					//for het i-de keyword, de bml tags die gemaakt moeten worden (later verzamelen + achter alle speechtags)
//					for (int j = 1; j < keywordList[ i ].length; j++){
//						if (j==0){
//							//dit is altijd het keyword, dus niets doen
//						}else if (j==1){
//							temp = temp+keywordList[i][j]+keyword+k; //"<gesture id=\""
//						}else if (j==2){
//							temp = temp+keywordList[i][j]+bmlSpeechID;//"\" lexeme=\"offer\" start=\"speech"
//						}else if (j==3){
//							temp = temp+keywordList[i][j]+idtellertje;//":sync"
//						}else {
//							temp = temp+keywordList[i][j];//"\" />"
//							
//						}							
//						//System.out.print(keywords[ i ][ j ] + " ");
//						//System.out.println("temp: "+temp);
//						//bmlBehaviours[i] = temp;
//						answerOUT[1]=temp;
//		            }
//					idtellertje++;
//				}
//				
//			}else {
//				//answerOUT[0]=answerIN;				
//			}
//			
//		}
//			
//		return answerOUT;
//		
//		//deze strings moeten nog gevuld worden uit een xml, en dan voor alle rules in die xml, dus iets met een for (int i = 0 ; i < rules.getLength();i++) 
//		
	}
	
//	public String answersyncpoints (String answerIN, String keyword, int counter) {
//		String answersyncOUT = "";
//		
//		// eerst de syncpoints plaatsen in de answerIN				
//		int nextIndex = answerIN.indexOf(keyword), oldIndex = 0;//, counter = 0;
//		while(nextIndex != -1) {
//			answersyncOUT += answerIN.substring(oldIndex, nextIndex-1) + " <sync id=\"sync" + counter + "\" /> ";
//		    oldIndex = nextIndex;
//		    nextIndex = answerIN.indexOf(keyword, nextIndex+1);
//		    ++counter; 
//		}
//		answersyncOUT += answerIN.substring(oldIndex);
//		//System.out.println(answerOUT);
//		
//		return answersyncOUT;
//	}
	
	
	public void sendBml(String bml) {
		String prefix = "{ \"bml\": { \"content\": \"";
		String suffix = "\" } }";
		
		try {
			bml = bml.replace("\r", " ").replace("\n", " ");
			String jsonthing = prefix+URLEncoder.encode(bml, "UTF-8")+suffix;
			//jsonthing = jsonthing.replace("%0A", " ");//%0A
			System.out.println(bml);
			//System.out.println(jsonthing);
			con.send(jsonthing, outTopic);
		} catch (UnsupportedEncodingException e) {
			System.out.println("[sendBml] Encoding failed.");
		}
	}

    public void sayshort(String answer) {
        String prefix = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml"+(++bmlId)+"\"><speech id=\"speech1\" start=\"0\"><text>";
        
        String answerleukebml[] = LeukereBMLs(answer,1);
        
        String suffix = "</text></speech>"+answerleukebml[1]+"</bml>";
       // System.out.println(prefix+answerleukebml[0]+suffix);
        sendBml(prefix+answerleukebml[0]+suffix);
    }
    
    public void saylong (String answer){
		String subanswer = "";
		//String subanswerleukebml[];// = {"",""};
		String longanswer = "";
		String startAt = "0";
		int j = 0;
		
		String prefix = "";
		String suffix = "";
		
		//zoeken in lange antwoorden naar punten, op de punten splitsen we de string en maken er een langere bml van
		while (answer.contains(".")){
			prefix = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bml"+(++bmlId)+"\" composition=\"MERGE\">";
			
			int i = answer.indexOf( '.' );
			System.out.println("answer1: "+answer);
			
			subanswer = answer.substring(0, (i+1));
			System.out.println("subanswer1: "+subanswer);
			
			String subanswerleukebml[] = LeukereBMLs(subanswer,j);
			System.out.println("j: "+j);
			System.out.println("subbml1: "+subanswerleukebml[0]+subanswerleukebml[1]);
			
			
			longanswer = "<speech id=\"speech"+j+"\" start=\""+startAt+"\"><text>"+subanswerleukebml[0]+"</text></speech>";
			

			answer = answer.replace(subanswer, "");
			System.out.println("answer2: "+answer);
			
			j++;

			suffix = subanswerleukebml[1]+"</bml>";

			sendBml(prefix+longanswer+suffix);
			
			//dingen leegmaken voor nieuwe subanswer
			subanswerleukebml[0] = "";
			subanswerleukebml[1] = "";
			suffix = "</bml>";
		}
		j=0;
    }
	
	@Override
	public void onMessage(Message msg) {			
//		try {
//		    System.out.println("[onMessage] "+URLDecoder.decode(msg.getContentAsString(), "UTF-8"));	
//		} catch (UnsupportedEncodingException e) {
//			System.out.println("[onMessage] Decoding failed.");
//		}
		
		
		//System.out.println(msg.getContentAsString());
		String msgstring = msg.getContentAsString();
		if (msgstring.substring(0, 5) .equals("{ \"se")){
			//asr input
			System.out.println("ASR!");
			String userSaid = decodeASRmsg(msg);
			String answer = getAnswer(userSaid);
			ttsFriendlyAnswer(answer);
			
		}else if (msgstring.substring(0, 5) .equals("{\"use")){
			//clevr gui text input
			System.out.println("clevrtext!");
			String userSaid = decodeCleVRmsg(msg);
			String answer = getAnswer(userSaid);
			ttsFriendlyAnswer(answer);
			
		}else if (msgstring.contains("bmla:status=\"DONE\"")){
			System.out.println("bml done!");
			readyforBML = true;//notify flag waarmee de wait opgeheven wordt
			//bml done
		}		
	}
	
	public void ttsFriendlyAnswer(String answer){
		//als het een kort antwoord is kan het gelijk, anders moet het in stukken voor de tts
		int i = answer.length();
		System.out.println("Answerlength: "+Integer.toString(i));
		if (i<45){
			testqaresponder.sayshort(answer);
		} else {
			testqaresponder.saylong(answer);
		}	
	}	
	
	public String decodeCleVRmsg(Message msg){
		String msgstring = "";
		try {
			msgstring = URLDecoder.decode(msg.getContentAsString(), "UTF-8");
			msgstring = msgstring.replace("{\"userResponse\":\"","");
			int index = msgstring.indexOf("\"");
			msgstring = msgstring.substring(0, index);
			System.out.println(msgstring);			
		} catch (UnsupportedEncodingException e) {
			System.out.println("[decodeCleVRmsg] Decoding failed.");
			e.printStackTrace();
		}
		return msgstring;	
	}
	
	public String decodeASRmsg(Message msg){
		String msgstring = "";				
		try {	
			msgstring = URLDecoder.decode(msg.getContentAsString(), "UTF-8");
			//dingen weggooien uit de herkende string waar ik niets mee kan
			msgstring = msgstring.replace("{ \"sentence\": \"","");
			msgstring = msgstring.replace("<unk>","");
			msgstring = msgstring.replace("."," ");
			int index = msgstring.indexOf("\"");
			msgstring = msgstring.substring(0, index);
			System.out.println(msgstring);			
		} catch (UnsupportedEncodingException e) {
			System.out.println("[decodeASRmsg] Decoding failed.");
			e.printStackTrace();
		}
		return msgstring;	
	}
	
	public String getAnswer (String query){
		// 		set value of attribute used to filter answer given in response to question	
		String attName = "type"; 		//"type"
		String attValue = "certain"; 	// "certain" or "uncertain"				
		//		Dialog d = store.getBestMatchingDialog(query);
		//		String answer = store.answerString(d, attName , attValue );
		// 		the above two lines have the same effect as the next line
		//		String answer = store.bestMatch(query, attName, attValue);
		// 		alternative: first set the attribute for filtering answers
		store.setAttribute(attName,attValue);
		String answer = store.bestMatch(query);		
		
		System.out.print("Answer: "+answer);
		System.out.print("Question: ");
		System.out.flush();
		return answer;
	}

	@Override
	public void onError(ErrorMessage err) {
		System.out.println("[onError] "+err.getMessage());
	}

}