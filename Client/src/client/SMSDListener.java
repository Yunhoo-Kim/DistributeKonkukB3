package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import logging.Logging;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import queue.MsgManager;
import queue.PassiveQueue;
import queue.SubscribeQueue;

public class SMSDListener implements Runnable {
	public Socket server_socket = null;
	SubscribeQueue subscribe_queue = null;
	MsgManager msg_manager = new MsgManager();
	private static final Logger logger = Logging.logger;
	
	public SMSDListener(Socket socket, SubscribeQueue subscribe_queue){
		this.server_socket = socket;
		this.subscribe_queue = subscribe_queue;
	}
	
	public void run(){
		while(!Thread.currentThread().isInterrupted()){
			listenToServer();
		}
	}
	private void listenToServer(){
		InputStream in;
		try {
			this.decodeMsg(this.msg_manager.receiveMsg(this.server_socket));
			in = this.server_socket.getInputStream();
			DataInputStream dis = new DataInputStream(in);
			String msg = dis.readUTF();
			this.decodeMsg(this.msg_manager.decodeMsg(msg));
		} catch (IOException e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
//				e1.printStackTrace();
			}
			this.logger.info("disconnected to slave");
			Client client = Client.getInstance();
			client.reconnect();
		}
	}
	private void addToQueue(JSONObject message){
		this.logger.info("add To Subscribe message Queue");
		this.subscribe_queue.accept(message);
	}
	public void decodeMsg(JSONObject message){
		// TODO : distinguish message type
		this.addToQueue(message);
	}
	
	
}
