package twitter.interpreter;

import java.util.ArrayList;
import java.util.List;

public class Tweet {

	private long unixTimestamp;
	private String user;
	private String content;
	private List<String> hashtags;

	public Tweet(long unixTimestamp, String user, String content){
		this.setUnixTimestamp(unixTimestamp);
		this.setUser(user);
		this.content = content;
		this.hashtags = parseHashTags(content);
	}

	/**
	 * Still a dummy method
	 * TODO: implement hashtag parsing
	 * @param content
	 * @return
	 */
	private List<String> parseHashTags(String content){
		List<String> ht = new ArrayList<String>();
		return ht;
	}
	
	public long getUnixTimestamp() {
		return unixTimestamp;
	}

	public void setUnixTimestamp(long unixTimestamp) {
		this.unixTimestamp = unixTimestamp;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<String> getHashtags() {
		return hashtags;
	}

	public void setHashtags(List<String> hashtags) {
		this.hashtags = hashtags;
	}
	
}
