package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.json.simple.JSONObject;

public class ClientConnectionDelegator {
	private Client client;

	public ClientConnectionDelegator() {
		this.client = Client.getInstance();
	}

	public void sendMsg(JSONObject json) {

		try {
			OutputStream out = this.client.server_socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeUTF(this.client.msg_manager.encodeMsg(json));
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public JSONObject receiveMsg(Socket socket) {
		try {
			InputStream in = socket.getInputStream();
			DataInputStream dis = new DataInputStream(in);
			String msg = dis.readUTF();
			return this.client.msg_manager.decodeMsg(msg);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new JSONObject();
	}
}
