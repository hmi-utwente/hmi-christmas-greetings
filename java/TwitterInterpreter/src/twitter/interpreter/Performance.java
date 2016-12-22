package twitter.interpreter;

import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;

public interface Performance {

	public ArrayNode generateScript(String input);
	
}
