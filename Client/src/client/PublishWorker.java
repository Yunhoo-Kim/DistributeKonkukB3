package client;

import java.io.IOException;
import java.net.Socket;

import org.json.simple.JSONObject;

import queue.PublishQueue;

public class PublishWorker implements Runnable{
	private Socket server_socket = null;
	private PublishQueue publish_queue = null;
	private Client client = null;
	
	public PublishWorker(Socket sock, PublishQueue queue){
		this.publish_queue = queue;
		this.server_socket = sock;
		this.client = Client.getInstance();
	}
	
	public void run(){
		while(!Thread.currentThread().isInterrupted()){
			JSONObject msg = this.publish_queue.release();
			this.decodeMsg(msg);
		}
	}
	
	private void publishToServer(JSONObject json){
		ClientConnectionDelegator con_dele = new ClientConnectionDelegator();
		con_dele.sendMsg(json);
	}
	
	private void decodeMsg(JSONObject json){
		this.publishToServer(json);
	}
	
	
}
