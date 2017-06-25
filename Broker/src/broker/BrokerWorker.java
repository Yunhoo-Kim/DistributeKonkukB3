package broker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import logging.Logging;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

public class BrokerWorker {
	JSONObject message;
	BrokerMsgManager msg_manager;
	private static final Logger logger = Logging.logger;
	public BrokerWorker(BrokerMsgManager manager) {
		this.msg_manager = manager;
	}

	public void workExecutor(JSONObject data) {
		this.message = data;
		String op_type = (String) this.message.get("op_type");
//		this.logger.info("op_type is " + op_type );
		switch (op_type) {
		case "register server":
			this.registerServer();
			break;
		case "unregister server":
			this.unregisterServer();
			break;
		case "get masters":
			this.returnMasterList();
			break;
		case "client connect":
			this.forwardClient();
			break;
		case "update master":
			this.updateMasterTable();
			break;
		}
	}

	private void registerServer() {
		String type = (String)this.message.get("node_type");
		if (type.equals("master")) {
			this.registerMaster();
		} else {
			this.registerSlave();
		}
	}

	private void forwardClient() {
		JSONObject master = this.findServer();
		JSONObject slave = this.getSlaveAddress(master);
		slave.put("op_type", "return forward");
		this.msg_manager.broker_listener.sendMsg(this.msg_manager.encodeMsg(slave));
	
	}
	
	private JSONObject getSlaveAddress(JSONObject master){
		JSONObject json = null;
		try{
			Socket socket = new Socket((String)master.get("ip"), ((Number)master.get("port")).intValue());
			json = new JSONObject();
			String msg = null;
			json.put("op_type", "forward client");
			msg = this.msg_manager.encodeMsg(json);
			OutputStream out = socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeUTF(msg);
			out.flush();
			
			InputStream in = socket.getInputStream();
			DataInputStream dis = new DataInputStream(in);
			msg = dis.readUTF();
			JSONObject json1 = this.msg_manager.decodeMsg(msg);
//			System.out.println("In Broker Worker" + json.toJSONString());
			socket.close();
			return json1;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return json;
		
	}
	private void registerMaster() {
		Broker b = Broker.getInstance();
		String ip = (String) this.message.get("ip");
		int id = ((Number)this.message.get("node_id")).intValue();
		int port = ((Number)this.message.get("port")).intValue();
		int weight = ((Number)this.message.get("weight")).intValue();
		// add to master table
		b.addMaster(ip, port, id, weight);
		this.msg_manager.broker_listener.sendOkMsg();
	}

	private void registerSlave() {
		Broker b = Broker.getInstance();
		ArrayList<JSONObject> ms = b.getMasterTable();
		JSONObject data = new JSONObject();
		JSONObject server = new JSONObject();
		data.put("op_type", "return register slave");
		
		if (!ms.isEmpty()) {
			server = this.findServer();
		}else{
			server = new JSONObject();
		}

		data.put("master_address", server);
		this.msg_manager.broker_listener.sendMsg(this.msg_manager
				.encodeMsg(data));

	}

	private void unregisterServer() {
		Broker b = Broker.getInstance();
		int id = ((Number)this.message.get("node_id")).intValue();
		b.removeMaster(id);
		// response
		this.msg_manager.broker_listener.sendOkMsg();
	}

	private void updateMasterTable() {
		Broker b = Broker.getInstance();
		int prev_id = ((Number)this.message.get("prev_id")).intValue();
		String ip = (String) this.message.get("ip");
		int id = ((Number)this.message.get("node_id")).intValue();
		int port = ((Number)this.message.get("port")).intValue();
		int weight = ((Number)this.message.get("weight")).intValue();
		b.updateMaster(prev_id, ip, port, id, weight);
		this.msg_manager.broker_listener.sendOkMsg();
	}

	public void returnMasterList() {
		Broker b = Broker.getInstance();
		this.logger.info("Return master List");
		ArrayList<JSONObject> master_list = b.getMasterTable();
		JSONObject return_data = new JSONObject();
		return_data.put("master_list", master_list);
//		this.logger.info("Return master List");
		String data = this.msg_manager.encodeMsg(return_data);
		this.msg_manager.broker_listener.sendMsg(data);
	}

	private JSONObject findServer() {
		Broker b = Broker.getInstance();
		JSONObject obj = b.loadBalancing();
		return obj;
	}

}
