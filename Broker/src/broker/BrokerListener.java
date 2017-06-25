package broker;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.simple.JSONObject;

public class BrokerListener implements Runnable {

	BrokerMsgManager msg_manager;
	Socket client_socket;

	public BrokerListener(Socket sock) {
		this.client_socket = sock;
		this.msg_manager = new BrokerMsgManager(this);
	}

	public void run() {
		// pass msg to receive manager after received
		try {
			InputStream in = this.client_socket.getInputStream();
			DataInputStream dis = new DataInputStream(in);
			String msg = dis.readUTF();
			this.receiveMsg(msg);
			this.client_socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void receiveMsg(String msg) {
		this.msg_manager.decodeToExecute(msg);
	}
	
	public void sendMsg(String msg) {
		try {
			OutputStream out = this.client_socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeUTF(msg);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void sendOkMsg(){
		JSONObject json = new JSONObject();
		json.put("op_type", "ok");
		String msg = this.msg_manager.encodeMsg(json);
		this.sendMsg(msg);
		
	}
	
}
