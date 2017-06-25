package client;

import java.net.Socket;

import org.json.simple.JSONObject;

import queue.SubscribeQueue;

public class SubscribeWorker implements Runnable{
	Socket server_socket = null;
	SubscribeQueue subscribe_queue = null;
	private Client client = null;
	
	public SubscribeWorker(Socket socket, SubscribeQueue queue){
		this.server_socket = socket;
		this.subscribe_queue = queue;
		this.client = Client.getInstance();
	}
	
	private void msgToClient(JSONObject msg){
		// TODO : pass msg data to client object
		this.client.subscribe(msg);
	}
	
	public void run(){
		while(!Thread.currentThread().isInterrupted()){
			JSONObject msg = this.subscribe_queue.release();
			this.decodeMsg(msg);
		}
	}
	public void decodeMsg(JSONObject msg){
		this.msgToClient(msg);
	}
	
}
