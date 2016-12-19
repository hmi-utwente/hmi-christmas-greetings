package asap;

import java.io.IOException;

import hmi.UmaAgent.UmaAgentStarter;

public class UMARealizerStarter
{


    public static void main(String[] args) throws IOException
    {
    	UmaAgentStarter uas = new UmaAgentStarter();
    	uas.init();
    }
    
}
