package hmi;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import nl.utwente.hmi.middleware.Middleware;
import nl.utwente.hmi.middleware.MiddlewareListener;
import nl.utwente.hmi.middleware.loader.GenericMiddlewareLoader;
import nl.utwente.hmi.middleware.worker.AbstractWorker;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class Middleware2Hue extends AbstractWorker implements MiddlewareListener {
	
	static final String USER_AGENT = "Mozilla/5.0";

	private static Logger logger = LoggerFactory.getLogger(Middleware2Hue.class.getName());
	Middleware middleware;

	private static final String OUTPUT_TOPIC = "/topic/Random";
	private static final String INPUT_TOPIC = "/topic/Hue";

	public Middleware2Hue() {
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

		try {
			sendLights("{ \"on\":true, \"sat\":254, \"bri\":254,\"hue\":10000} ", 1);
			sendLights("{ \"on\":true, \"sat\":254, \"bri\":254,\"hue\":10000} ", 2);
			sendLights("{ \"on\":true, \"sat\":254, \"bri\":254,\"hue\":10000} ", 3);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
				try {
					sendLights("{ \"on\":false }", 1);
					sendLights("{ \"on\":false }", 2);
					sendLights("{ \"on\":false }", 3);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		}));
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
		logger.debug("/topic/Hue:{}", jn.toString());

		if (jn.has("polarity") && jn.has("subjectivity")) {
			float pol = Float.parseFloat(jn.get("polarity").asText());
			float sub = Float.parseFloat(jn.get("subjectivity").asText());
			sendSentiment(pol, sub);
		} else {
			try {
				sendLights(jn.toString(), 1);
				sendLights(jn.toString(), 2);
				sendLights(jn.toString(), 3);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendSentiment(float polarity, float subjectivity) {
		int hue1 = (int) (((polarity+1)/4) * 65535);
		if (polarity>0) {
			hue1 = (int) ((polarity) * 25000 + 40000);
		}
		int hue2 = (int) (((subjectivity+1)/4) * 65535);
		if (subjectivity>0) {
			hue2 = (int) ((subjectivity) * 25000 + 40000);
		}
		try {
			if (polarity > 0) {
				sendLights("{ \"on\": true, \"hue\":10000 }", 1);
			} else {
				sendLights("{ \"on\": true, \"hue\":45000 }", 1);
			}
			
			if (subjectivity > 0) {
				sendLights("{ \"on\": true, \"hue\":500 }", 1);
			} else {
				sendLights("{ \"on\": true, \"hue\":60000 }", 1);
			}
			
			if (Math.random() < 0.5) {
				sendLights("{ \"on\": true, \"hue\":50000 }", 3);
			} else {
				sendLights("{ \"on\": true, \"hue\":2000 }", 3);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void sendLights(String state, int lamp) throws Exception {
			String numLamp = Integer.toString(lamp);
			
			String url = "http://192.168.1.216/api/EXbZojJb8I0PtiKjb3Vz8ANFB8EbkZZgJVBHugy2/lights/"+numLamp+"/state";

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setDoOutput(true);
			// optional default is GET
			con.setRequestMethod("PUT");

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
			
			//add body
			con.setRequestProperty("Content-Type","application/json"); 
	        OutputStream os = con.getOutputStream();
	        os.write(state.getBytes());
	        os.flush();
			
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			//print result
			System.out.println(response.toString());
		}

}