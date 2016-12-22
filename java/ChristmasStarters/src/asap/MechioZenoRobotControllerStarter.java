package asap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import nl.utwente.hmi.middleware.Middleware;
import nl.utwente.hmi.middleware.loader.GenericMiddlewareLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.zeno.ZenoRobotControllerMechioImpl;
import asap.zeno.middlewareadapter.MiddlewareToZRC;

public class MechioZenoRobotControllerStarter {
	private static Logger logger = LoggerFactory.getLogger(MechioZenoRobotControllerStarter.class.getName());

	public static void main(String[] args){
        new MechioZenoRobotControllerStarter(args);
    }
    public MechioZenoRobotControllerStarter(String[] args){

    	String help = "Expecting commandline arguments in the form of \"-<argname> <arg>\".\nAccepting the following argnames: mechioprops, middlewareprops";
    	
        String mechioPropFile = "mechio.properties";
    	String mwPropFile = "defaultmiddleware.properties";
    	
        if(args.length % 2 != 0){
        	System.err.println(help);
        	System.exit(0);
        }
        
        for(int i = 0; i < args.length; i = i + 2){
        	if(args[i].equals("-mechioprops")){
        		mechioPropFile = args[i+1];
        	} else if(args[i].equals("-middlewareprops")){
        		mwPropFile = args[i+1];
        	} else {
            	System.err.println("Unknown commandline argument: \""+args[i]+" "+args[i+1]+"\".\n"+help);
            	System.exit(0);
        	}
        }
		
        Properties defaultProp = new Properties();
		defaultProp.put("mechio_ip", "130.89.15.168");
		defaultProp.put("robot_id", "");
		defaultProp.put("animation_location", "resource/zeno_animations/");
		
		//now load the user-defined values (if any)
		Properties prop = new Properties(defaultProp);
		InputStream input = null;
	 
		try {
			input = getClass().getClassLoader().getResourceAsStream(mechioPropFile);
			if (input == null) {
				logger.error("Sorry, unable to find properties file: {}", mechioPropFile);
			} else {
				//load the actual properties
				prop.load(input);
			}
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

		GenericMiddlewareLoader.setGlobalPropertiesFile(mwPropFile);
        
		Properties ps = new Properties();
		ps.put("iTopic", "/topic/robotAction");
		ps.put("oTopic", "/topic/robotFeedback");
		
        GenericMiddlewareLoader gml = new GenericMiddlewareLoader("nl.utwente.hmi.middleware.stomp.STOMPMiddlewareLoader", ps);
        Middleware m = gml.load();

        logger.debug("Finding Zeno controller \"{}\" at {}",prop.getProperty("robot_id"),prop.getProperty("mechio_ip"));
        final ZenoRobotControllerMechioImpl theZRC = new ZenoRobotControllerMechioImpl(prop.getProperty("mechio_ip"),prop.getProperty("robot_id"), prop.getProperty("animation_location"));
        theZRC.playAnimationByName("Default");
        try {Thread.sleep(1500);} catch (Exception ex) {}
        theZRC.speak("init0", "Robot aan");
        //theZRC.playAnimationByName("Idle animation - read animation - huh 2");
        //try {Thread.sleep(8000);} catch (Exception ex) {}
        //theZRC.playAnimationByName("Default");
        MiddlewareToZRC middlewareToZRC = new MiddlewareToZRC(m, theZRC);
	}
}
