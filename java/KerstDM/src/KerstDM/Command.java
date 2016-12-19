package KerstDM;

public class Command {

	private String agent;
	private String agentFeedback;
	private String bml;
	
	public Command(String agent, String agentFeedback, String bml) {
		this.agent = agent;
		this.agentFeedback = agentFeedback;
		this.bml = bml;
	}
	
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}
	public String getAgentFeedback() {
		return agentFeedback;
	}
	public void setAgentFeedback(String agentFeedback) {
		this.agentFeedback = agentFeedback;
	}
	public String getBml() {
		return bml;
	}
	public void setBml(String bml) {
		this.bml = bml;
	}
	
	
	
}
