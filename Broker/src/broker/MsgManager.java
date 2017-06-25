package broker;

import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MsgManager {
	JSONObject message;
	
	public JSONObject decodeMsg(String msg){
		JSONParser parser = new JSONParser();
		JSONObject j_data = null;
		
		try{
			Object data = parser.parse(msg);
			j_data = (JSONObject)data;
		}catch(ParseException pe){
			
		}
		
		return j_data;
	}
	
	public String encodeMsg(JSONObject msg){
		StringWriter out = new StringWriter();
		try {
			msg.writeJSONString(out);
			String return_string = out.toString();
			return return_string;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
