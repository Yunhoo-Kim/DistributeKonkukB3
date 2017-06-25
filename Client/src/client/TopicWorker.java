package client;

import java.net.Socket;

import logging.Logging;

import org.json.simple.JSONObject;

import queue.TopicQueue;

public class TopicWorker implements Runnable {
	private Socket server_socket;
	private TopicQueue topic_queue = null;
	private Client client = null;
	private ClientConnectionDelegator con_dele = new ClientConnectionDelegator();
	
	public TopicWorker(Socket sock, TopicQueue queue){
		this.server_socket = sock;
		this.topic_queue = queue;
		this.client = Client.getInstance();
	}
	
	private void registerTopic(JSONObject json){

		Logging.logger.info("register Topic " + json.toJSONString());
		this.con_dele.sendMsg(json);
	}
	
	private void unregisterTopic(JSONObject json){
		Logging.logger.info("unregister Topic " + json.toJSONString());
		this.con_dele.sendMsg(json);

	}
	private void decodeMsg(JSONObject json){
		if(((String)json.get("op_type")).equals("add topic"))
			this.registerTopic(json);
		else if(((String)json.get("op_type")).equals("remove topic"))
			this.unregisterTopic(json);
	}
	
	public void run(){
		while(!Thread.currentThread().isInterrupted()){
			JSONObject json = this.topic_queue.release();
			this.decodeMsg(json);
		}
	}
}
