package asap;

import java.io.IOException;

import javax.swing.JFrame;

import nl.utwente.hmi.middleware.loader.GenericMiddlewareLoader;
import twitter.interpreter.TwitterInterpreter;

public class TwitterInterpreterStarter {

	public static void main(String[] args){
		String help = "Expecting commandline arguments in the form of \"-<argname> <arg>\".\nAccepting the following argnames: middlewareprops";

    	String mwPropFile = "defaultmiddleware.properties";
    	
        if(args.length % 2 != 0){
        	System.err.println(help);
        	System.exit(0);
        }
        
        for(int i = 0; i < args.length; i = i + 2){
        	if(args[i].equals("-middlewareprops")){
        		mwPropFile = args[i+1];
        	} else {
            	System.err.println("Unknown commandline argument: \""+args[i]+" "+args[i+1]+"\".\n"+help);
            	System.exit(0);
        	}
        }
    	
		GenericMiddlewareLoader.setGlobalPropertiesFile(mwPropFile);
		
		TwitterInterpreter ti = new TwitterInterpreter();
		ti.init();
		
	}
}
