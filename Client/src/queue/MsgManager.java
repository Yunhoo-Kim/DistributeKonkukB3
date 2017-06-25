package queue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class MsgManager {
	JSONObject message;

	private Logger logger = Logger.getLogger(MsgManager.class.getName());

	public JSONObject decodeMsg(String msg) {
		JSONParser parser = new JSONParser();
		JSONObject j_data = null;

		try {
			Object data = parser.parse(msg);
			j_data = (JSONObject) data;
		} catch (ParseException pe) {

		}

		return j_data;
	}

	public String encodeMsg(JSONObject msg) {
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

	public void sendMultiple(ArrayList<JSONObject> list, JSONObject message) {

		this.logger.info("Send to Multiple");
		Iterator<JSONObject> iter = list.iterator();
		while (iter.hasNext()) {
			Socket sock = (Socket) iter.next().get("socket");
			this.logger.info("send message" + message.toJSONString());
			try {
				this.sendMsg(message, sock);
			} catch (Exception e) {

			}
		}
	}

	public JSONObject receiveMsg(Socket socket) throws IOException {
			InputStream in = socket.getInputStream();
			DataInputStream dis = new DataInputStream(in);
			String msg = dis.readUTF();
			return this.decodeMsg(msg);
	}

	public void sendMsg(JSONObject json, Socket socket) throws IOException {

		OutputStream out = socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		dos.writeUTF(this.encodeMsg(json));
		out.flush();
		this.logger.info("send message : " + json.toJSONString());

	}
}
