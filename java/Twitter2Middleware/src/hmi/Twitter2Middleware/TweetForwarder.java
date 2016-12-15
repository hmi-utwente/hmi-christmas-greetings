package hmi.Twitter2Middleware;

import static nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.object;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.utwente.hmi.middleware.Middleware;
import nl.utwente.hmi.middleware.MiddlewareListener;
import nl.utwente.hmi.middleware.stomp.STOMPMiddleware;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;



public final class TweetForwarder implements MiddlewareListener {
	Middleware middleware;
	
	String[] knownKeywords = new String[]{ "zeno", "greet", "poem" };
  	ObjectMapper om;
  	private long followId;
 // etc. etc.


	Properties prop;
 
	public TweetForwarder() {
		this("127.0.0.1", 61613, "/topic/Tweets", "/topic/Twitter2MiddlewareCMD");
	}
	
	public TweetForwarder(String ip, int port, String targetTopic, String feedbackTopic) {
		middleware = new STOMPMiddleware(ip, port, targetTopic, feedbackTopic);
		middleware.addListener(this);
		prop = new Properties();
		InputStream input = null;

        om = new ObjectMapper();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		 try {
		 	input = getClass().getClassLoader().getResourceAsStream("config.properties");
		 	// load a properties file
		 	prop.load(input);
			cb.setDebugEnabled(true)
			  .setOAuthConsumerKey(prop.getProperty("oAuthConsumerKey"))
			  .setOAuthConsumerSecret(prop.getProperty("oAuthConsumerSecret"))
			  .setOAuthAccessToken(prop.getProperty("oAuthAccessToken"))
			  .setOAuthAccessTokenSecret(prop.getProperty("oAuthAccessTokenSecret"));
			followId = Long.parseLong(prop.getProperty("followId"));
			cb.setDebugEnabled(true);
			System.out.println(prop.getProperty("oAuthConsumerKey"));
			System.out.println(prop.getProperty("oAuthConsumerSecret"));
			System.out.println(prop.getProperty("oAuthAccessToken"));
			System.out.println(prop.getProperty("oAuthAccessTokenSecret"));
			System.out.println("Configured builder");
		 } catch (IOException ex) {
		 	ex.printStackTrace();
		 } finally {
		 	if (input != null) {
		 		try {
		 			input.close();
		 		} catch (IOException e) {
		 			e.printStackTrace();
		 		}
		 	}
		 }

		TwitterStreamFactory tsf = new TwitterStreamFactory(cb.build());		
		StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
            	HashtagEntity[] hashtags = status.getHashtagEntities();
            	String finalString = status.getText();
            	ArrayList<String> containedKnownKeywords = new ArrayList<String>();
            	
            	for (int i = 0; i < hashtags.length; i++) {
            		if (Arrays.asList(knownKeywords).contains(hashtags[i].getText())) {
            			containedKnownKeywords.add(hashtags[i].getText());
            		}

            		//for (int k = 0; k <  (knownKeywords.)
            	}
            	
            	long time = status.getCreatedAt().getTime() / 1000L;
            	//String 
            	Forward(status.getUser().getScreenName(), finalString, containedKnownKeywords.toArray(new String[containedKnownKeywords.size()]), time);
                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        TwitterStream twitterStream = tsf.getInstance();
        twitterStream.addListener(listener);
        
        ArrayList<Long> follow = new ArrayList<Long>();
        ArrayList<String> track = new ArrayList<String>();
        follow.add(followId);
        
        long[] followArray = new long[follow.size()];
        for (int i = 0; i < follow.size(); i++) {
            followArray[i] = follow.get(i);
        }
        String[] trackArray = track.toArray(new String[track.size()]);

        // filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
        twitterStream.filter(new FilterQuery(0, followArray, trackArray));
        
        System.out.println("setup done");
	}
	
	public void Forward(String user, String content, String[] knownKeywords, long time) {
		ObjectNode on = om.createObjectNode();
		
		
		on.put("time", time+"");
		on.put("user", user);
		on.put("content", content);
      	
		ArrayNode keywords = om.createArrayNode();
		for (int i = 0; i < knownKeywords.length; i++) {
			keywords.add(knownKeywords[i]);
		}
		
      	on.put("keywords", keywords);
      	
		middleware.sendData(on);
	}


	@Override
	public void receiveData(JsonNode jn) {
		System.out.println("/topic/Twitter2MiddlewareCMD: "+jn.toString());

		if (jn.has("cmd") && jn.get("cmd").asText().equals("tweet") && jn.has("content")) {
			// tweet jn.get("content").asText()
			// use the safe queue / abstract worker implementation?
		}
	}
}