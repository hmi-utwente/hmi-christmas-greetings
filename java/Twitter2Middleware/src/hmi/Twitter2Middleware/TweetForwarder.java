package hmi.Twitter2Middleware;

import static nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.array;
import static nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.object;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.utwente.hmi.middleware.Middleware;
import nl.utwente.hmi.middleware.MiddlewareListener;
import nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.ArrayNodeBuilder;
import nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.ObjectNodeBuilder;
import nl.utwente.hmi.middleware.loader.GenericMiddlewareLoader;
import nl.utwente.hmi.middleware.worker.AbstractWorker;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;



public final class TweetForwarder extends AbstractWorker implements MiddlewareListener {
	
	private static Logger logger = LoggerFactory.getLogger(TweetForwarder.class.getName());
	Middleware middleware;
	

	private static final String OUTPUT_TOPIC = "/topic/Tweets";
	private static final String INPUT_TOPIC = "/topic/Twitter2MiddlewareCMD";
	
	String[] knownKeywords = new String[]{ "zeno", "greet", "poem" };
  	ObjectMapper om;
  	private long followId;
	Properties prop;
 
	public TweetForwarder() {
		super();
	}
	
	public void init() {
		Properties ps = new Properties();
		ps.put("iTopic", INPUT_TOPIC);
		ps.put("oTopic", OUTPUT_TOPIC);
		
		new Thread(this).start();
		
        GenericMiddlewareLoader gml = new GenericMiddlewareLoader("nl.utwente.hmi.middleware.stomp.STOMPMiddlewareLoader", ps);
        middleware = gml.load();

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
            	System.out.println("@" + status.getUser().getScreenName() + ": " + status.getText());
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            	logger.info("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            	logger.info("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
            	logger.info("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
            	logger.info("Got stall warning:" + warning);
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
	}
	
	public void Forward(String user, String content, String[] knownKeywords, long time) {
		ObjectNodeBuilder on = object();
		
		on.with("time", time+"");
		on.with("user", user);
		on.with("content", content);

    	ArrayNodeBuilder keywords = array();
		for (int i = 0; i < knownKeywords.length; i++) {
			keywords.with(knownKeywords[i]);
		}
		
      	on.with("keywords", keywords.end());
      	
		middleware.sendData(on.end());
	}


	@Override
	public void receiveData(JsonNode jn) {
		addDataToQueue(jn);
	}
	

	@Override
	public void addDataToQueue(JsonNode jn) {
		queue.add(jn);
	}
	
	@Override
	public void processData(JsonNode jn) {
		logger.debug("/topic/Twitter2MiddlewareCMD:{}", jn.toString());
		if (jn.has("cmd") && jn.get("cmd").asText().equals("tweet") && jn.has("content")) {
			// tweet jn.get("content").asText()
			// use the safe queue / abstract worker implementation?
		}
	}
}