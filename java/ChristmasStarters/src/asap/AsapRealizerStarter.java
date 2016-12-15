package asap;

import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.Environment;
import hmi.jcomponentenvironment.JComponentEnvironment;
import hmi.util.Console;
import hmi.util.SystemClock;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import nl.utwente.hmi.middleware.loader.GenericMiddlewareLoader;
import saiba.bml.BMLInfo;
import saiba.bml.core.FaceLexemeBehaviour;
import saiba.bml.core.HeadBehaviour;
import saiba.bml.core.PostureShiftBehaviour;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;

/**
 * Simple demo for the AsapRealizer+environment
 * @author dennisr
 * 
 */
public class AsapRealizerStarter
{

	//static { YarpInitializer.init(); }
	
    protected JFrame mainJFrame = null;
	
    public AsapRealizerStarter(JFrame j, String spec) throws IOException
    {
		//GenericMiddlewareLoader.setGlobalPropertiesFile("defaultmiddleware.properties");
        Console.setEnabled(false);
        mainJFrame = j;

        BMLTInfo.init();
        BMLInfo.addCustomFloatAttribute(FaceLexemeBehaviour.class, "http://asap-project.org/convanim", "repetition");
        BMLInfo.addCustomStringAttribute(HeadBehaviour.class, "http://asap-project.org/convanim", "spindirection");
        BMLInfo.addCustomFloatAttribute(PostureShiftBehaviour.class, "http://asap-project.org/convanim", "amount");

        final AsapEnvironment ee = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("LJWGL_JOAL");
        ClockDrivenCopyEnvironment ce = new ClockDrivenCopyEnvironment(1000 / 50);

        final JComponentEnvironment jce = setupJComponentEnvironment();

        aue.init();
        ce.init();

        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(ee);
        environments.add(aue);
        environments.add(jce);
        environments.add(ce);

		// if no physics, use the following:
		/**/
		SystemClock clock = new SystemClock(1000 / 100, "clock"); // the ideal frequency on which to run update messages depends on the robot and on the type of messages that you exchange... "play this animatino" does not typically require high frequency, but when you start to more or less directly control motors, you probably need much higher frequency. 
		ee.init(environments, clock);
		clock.start();
		clock.addClockListener(ee);

        System.out.println("loading spec "+spec);
        ee.loadVirtualHuman("", spec, "AsapRealizer -- HMI Christmas Greetings");

        j.addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(WindowEvent winEvt)
            {
                System.exit(0);
            }
        });

        mainJFrame.setSize(1000, 600);

        mainJFrame.setVisible(true);

    }

    private JComponentEnvironment setupJComponentEnvironment()
    {
        final JComponentEnvironment jce = new JComponentEnvironment();
                mainJFrame.setLayout(new BorderLayout());

                JPanel jPanel = new JPanel();
                jPanel.setPreferredSize(new Dimension(400, 40));
                jPanel.setLayout(new GridLayout(1, 1));
                jce.registerComponent("textpanel", jPanel);
                mainJFrame.add(jPanel, BorderLayout.SOUTH);
        return jce;
    }


    public static void main(String[] args) throws IOException
    {
    	String help = "Expecting commandline arguments in the form of \"-<argname> <arg>\".\nAccepting the following argnames: agentspec, middlewareprops";
    	
        String spec = "Zeno/loaders/agentspec.xml";
    	String propFile = "defaultmiddleware.properties";
    	
        if(args.length % 2 != 0){
        	System.err.println(help);
        	System.exit(0);
        }
        
        for(int i = 0; i < args.length; i = i + 2){
        	if(args[i].equals("-agentspec")){
        		spec = args[i+1];
        	} else if(args[i].equals("-middlewareprops")){
        		propFile = args[i+1];
        	} else {
            	System.err.println("Unknown commandline argument: \""+args[i]+" "+args[i+1]+"\".\n"+help);
            	System.exit(0);
        	}
        }
    	
		GenericMiddlewareLoader.setGlobalPropertiesFile(propFile);
		
        AsapRealizerStarter demo = new AsapRealizerStarter(new JFrame("AsapRealizer Hmmm version"), spec);
    }
}
