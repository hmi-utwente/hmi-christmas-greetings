package asap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import hmi.animation.VJoint;
import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.Environment;
import hmi.jcomponentenvironment.JComponentEnvironment;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment.RenderStyle;
import hmi.util.Console;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObjectEnvironment;
import nl.utwente.hmi.middleware.loader.GenericMiddlewareLoader;
import saiba.bml.BMLInfo;
import saiba.bml.core.FaceLexemeBehaviour;
import saiba.bml.core.HeadBehaviour;
import saiba.bml.core.PostureShiftBehaviour;

/**
 * Simple demo for the AsapRealizer+environment
 * @author hvanwelbergen
 * 
 */
public class ArmandiaRealizerStarter
{
    private final HmiRenderEnvironment hre;
    private final OdePhysicsEnvironment ope;

    private VJoint sphereJoint;
    protected JFrame mainJFrame = null;

    public ArmandiaRealizerStarter(JFrame j, String spec) throws IOException
    {
        Console.setEnabled(false);
        System.setProperty("sun.java2d.noddraw", "true"); // avoid potential
                                                          // interference with
                                                          // (non-Jogl) Java
                                                          // using direct draw
        mainJFrame = j;

        BMLTInfo.init();
        BMLInfo.addCustomFloatAttribute(FaceLexemeBehaviour.class, "http://asap-project.org/convanim", "repetition");
        BMLInfo.addCustomStringAttribute(HeadBehaviour.class, "http://asap-project.org/convanim", "spindirection");
        BMLInfo.addCustomFloatAttribute(PostureShiftBehaviour.class, "http://asap-project.org/convanim", "amount");

        hre = new HmiRenderEnvironment()
        {
            @Override
            protected void renderTime(double currentTime)
            {
                super.renderTime(currentTime);
                double speed = 1;
                if (sphereJoint != null) sphereJoint.setTranslation(0.75f + (float) Math.sin(currentTime * speed) * 1.5f,
                        (float) Math.cos(currentTime * speed) * 1f + 1.5f, 0.5f);
            }
        };

        ope = new OdePhysicsEnvironment();

        WorldObjectEnvironment we = new WorldObjectEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        final AsapEnvironment ee = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("LJWGL_JOAL");

        final JComponentEnvironment jce = setupJComponentEnvironment();

        hre.init(); // canvas does not exist until init was called
        we.init();
        ope.init();
        aue.init();
        mae.init(ope, 0.002f);

        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(hre);
        environments.add(we);
        environments.add(ope);
        environments.add(mae);
        environments.add(ee);
        environments.add(aue);
        environments.add(jce);

        ee.init(environments, ope.getPhysicsClock()); // if no physics, just use renderclock here!

        // this clock method drives the engines in ee. if no physics, then register ee as a listener at the render clock!
        ope.addPrePhysicsCopyListener(ee);

        // hre.getRenderClock().addClockListener(ee);

        hre.loadCheckerBoardGround("ground", 0.5f, 0f);
        hre.setBackground(0.2f, 0.2f, 0.2f);

        hre.loadBox("bluebox", new float[] { 0.05f, 0.05f, 0.05f }, RenderStyle.FILL, new float[] { 0.2f, 0.2f, 1, 1 },
                new float[] { 0.2f, 0.2f, 1, 1 }, new float[] { 0.2f, 0.2f, 1, 0 }, new float[] { 0.2f, 0.2f, 1, 1 });
        VJoint boxJoint = hre.getObjectRootJoint("bluebox");
        boxJoint.setTranslation(0.1f, 1.5f, 0.4f);
        we.getWorldObjectManager().addWorldObject("bluebox", new VJointWorldObject(boxJoint));

        hre.loadBox("redbox", new float[] { 0.05f, 0.05f, 0.05f }, RenderStyle.FILL, new float[] { 1f, 0.2f, 0.2f, 1 },
                new float[] { 1f, 0.2f, 0.2f, 1 }, new float[] { 1f, 0.2f, 0.2f, 0 }, new float[] { 1f, 0.2f, 0.2f, 1 });
        VJoint rboxJoint = hre.getObjectRootJoint("redbox");
        rboxJoint.setTranslation(0.6f, 1.5f, 0.4f);
        we.getWorldObjectManager().addWorldObject("redbox", new VJointWorldObject(rboxJoint));

        hre.loadSphere("greensphere", 0.05f, 25, 25, RenderStyle.FILL, new float[] { 0.2f, 1, 0.2f, 1 }, new float[] { 0.2f, 1, 0.2f, 1 },
                new float[] { 0.2f, 1, 0.2f, 0 }, new float[] { 0.2f, 1, 0.2f, 1 });
        sphereJoint = hre.getObjectRootJoint("greensphere");
        sphereJoint.setTranslation(-0.8f, 2.1f, 0.2f);
        we.getWorldObjectManager().addWorldObject("greensphere", new VJointWorldObject(sphereJoint));

        // add worldobject "camera" that we can use to look at user :)
        VJoint camera = hre.getCameraTarget();
        we.getWorldObjectManager().addWorldObject("camera", new VJointWorldObject(camera));

        // set camera position
        // hre.setNavigationEnabled(false);
        // hre.setViewPoint(new float[]{0,2,4});

        AsapVirtualHuman mandia = ee.loadVirtualHuman("", spec, "AsapRealizer demo");
        mandia.getRealizerPort().performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"  id=\"bml1\" xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\">"
        		+ "<bmlt:blinkemitter id=\"blinkemitter1\" start=\"0\" range=\"0.5\" avgwaitingtime=\"4.5\"/>"
        		+ "<bmlt:breathingemitter id=\"breathingemitter1\" start=\"0\" range=\"2\" avgwaitingtime=\"9\"/>"
        		+ "<postureShift id=\"pose1\"><stance type=\"STANDING\"/><pose part=\"LEGS\" lexeme=\"LEGS_OPEN\"/></postureShift>"
                + "<bmlt:noise id=\"noise1\" type=\"perlin\" joint=\"vl5\" start=\"0\">"
                + "  <bmlt:parameter name=\"basefreqx\" value=\"0.5\"/>"
                + "  <bmlt:parameter name=\"baseamplitudex\" value=\"0.035\"/>"
                + "  <bmlt:parameter name=\"basefreqy\" value=\"0.3\"/>"
                + "  <bmlt:parameter name=\"baseamplitudey\" value=\"0.02\"/>"
                + "   <bmlt:parameter name=\"basefreqz\" value=\"0.3\"/>"
                + "  <bmlt:parameter name=\"baseamplitudez\" value=\"0.02\"/>"
                + "</bmlt:noise>"
                + "<bmlt:noise id=\"noise2\" type=\"perlin\" joint=\"skullbase\" start=\"0\">"
                + "  <bmlt:parameter name=\"basefreqx\" value=\"1.25\"/>"
                + "  <bmlt:parameter name=\"baseamplitudex\" value=\"0.08\"/>"
                + "  <bmlt:parameter name=\"basefreqy\" value=\"1.25\"/>"
                + "  <bmlt:parameter name=\"baseamplitudey\" value=\"0.02\"/>"
                + "  <bmlt:parameter name=\"basefreqy\" value=\"1.25\"/>"
                + "  <bmlt:parameter name=\"baseamplitudey\" value=\"0.02\"/>"
                + "</bmlt:noise>"
        		+ "</bml>");

        j.addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(WindowEvent winEvt)
            {
                System.exit(0);
            }
        });

        mainJFrame.setSize(1000, 600);

        java.awt.Component canvas = hre.getAWTComponent(); // after init, get canvas and add to window
        mainJFrame.add(canvas, BorderLayout.CENTER);
        mainJFrame.setVisible(true);

    }

    private JComponentEnvironment setupJComponentEnvironment()
    {
        final JComponentEnvironment jce = new JComponentEnvironment();
        try
        {
            SwingUtilities.invokeAndWait(() -> {
                mainJFrame.setLayout(new BorderLayout());

                JPanel jPanel = new JPanel();
                jPanel.setPreferredSize(new Dimension(400, 40));
                jPanel.setLayout(new GridLayout(1, 1));
                jce.registerComponent("textpanel", jPanel);
                mainJFrame.add(jPanel, BorderLayout.SOUTH);
            });
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        return jce;
    }

    public void startClocks()
    {
        hre.startRenderClock();
        ope.startPhysicsClock();
    }

    public static void main(String[] args) throws IOException
    {
    	String help = "Expecting commandline arguments in the form of \"-<argname> <arg>\".\nAccepting the following argnames: agentspec, middlewareprops";
    	
        String spec = "ARMANDIA/loaders/agentspec.xml";
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
        ArmandiaRealizerStarter demo = new ArmandiaRealizerStarter(new JFrame("AsapRealizer Christmas Greetings - Armandia"), spec);
        demo.startClocks();
    }
}
