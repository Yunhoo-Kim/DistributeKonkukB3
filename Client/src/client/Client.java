package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//import java.lang.Character.Subset;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import logging.Logging;

//import java.util.logging.ConsoleHandler;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.logging.SimpleFormatter;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import queue.MsgManager;
import queue.PublishQueue;
import queue.SubscribeQueue;
import queue.TopicQueue;

public class Client {
	String server_ip;
	String broker_ip;
	int server_port;
	int broker_port;
	int id;
	
	Thread subscribe_worker;
	Thread publish_worker;
	Thread sdms_listener;
	Thread topic_worker;
	
	MsgManager msg_manager = new MsgManager();
	//topic list
	ArrayList<JSONObject> topic_list = new ArrayList<JSONObject>();
	//socket
	Socket server_socket = null;
	// logger
	
	
	// singleton
	public static Client client = null;
	// msg queue
	private TopicQueue topic_queue = new TopicQueue();
	private SubscribeQueue subscribe_queue = new SubscribeQueue();
	private PublishQueue publish_queue = new PublishQueue();
	
	public static synchronized Client getInstance(){
		return client;
	}
	
	public Client(String host, int port){
		
		this.broker_ip = host;
		this.broker_port = port;
		client = this;
		this.id = (int)(System.currentTimeMillis()/1000);
//		this.logger.logp(Level.INFO, "aa", "aa", "aaaaa");
	}
	
	public void reconnect(){
		this.interruptThreads();
		this.connect();
		this.addTopicAfterReconnect();
	}
	public void interruptThreads(){
		this.sdms_listener.interrupt();
		this.publish_worker.interrupt();
		this.subscribe_worker.interrupt();
		this.topic_worker.interrupt();
	}
	public void connect(){
		JSONObject broker_data = new JSONObject();
		broker_data.put("op_type", "client connect");
		String b_data = msg_manager.encodeMsg(broker_data);
		try {
			Socket socket = new Socket(this.broker_ip, this.broker_port);
			OutputStream out = socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			out.flush();
			dos.writeUTF(b_data);
			InputStream in = socket.getInputStream();
			DataInputStream dis = new DataInputStream(in);
			String msg = dis.readUTF();
			broker_data = this.msg_manager.decodeMsg(msg);
			socket.close();
//			this.logger.info("ip : " + (String)broker_data.get("ip") + "port " +((Number)broker_data.get("port")).intValue());
			this.connect((String)broker_data.get("ip"), ((Number)broker_data.get("port")).intValue());
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void connect(String ip, int port){
		this.server_ip = ip;
		this.server_port = port;
		
		try {
			Socket socket = new Socket(this.server_ip, this.server_port);
			this.server_socket = socket;
			this.addToSlave();
			this.runService();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void subscribe(JSONObject msg){
		Logging.logger.info(msg.toJSONString());
	}
	
	public void publish(String topic, String message){
		JSONObject msg = new JSONObject();
		msg.put("message", message);
		msg.put("topic", topic);
		msg.put("op_type", "publish");
		msg.put("client_id", this.getClientId());
		this.publish_queue.accept(msg);
	}
	public void addTopic(String topic){
		JSONObject add_topic = new JSONObject();
		add_topic.put("op_type", "add topic");
		add_topic.put("topic", topic);
		add_topic.put("client_id", this.getClientId());
		this.topic_queue.accept(add_topic);
		this.topic_list.add(add_topic);
	}
	private void addTopicAfterReconnect(){
		for(int i=0;i<this.topic_list.size();i++){
			this.topic_queue.accept(this.topic_list.get(i));
		}
	}
	public void removeTopic(String topic){
		JSONObject remove_topic = new JSONObject();
		remove_topic.put("op_type", "remove topic");
		remove_topic.put("topic", topic);
		remove_topic.put("client_id", this.getClientId());
		this.topic_queue.accept(remove_topic);
	}
	private void runService(){
		this.subscribe_worker = new Thread(new SubscribeWorker(this.server_socket, this.subscribe_queue));
		this.publish_worker = new Thread(new PublishWorker(this.server_socket, this.publish_queue));
		this.topic_worker = new Thread(new TopicWorker(this.server_socket, this.topic_queue));
		this.sdms_listener = new Thread(new SMSDListener(this.server_socket, this.subscribe_queue));
		this.subscribe_worker.start();
		this.publish_worker.start();
		this.topic_worker.start();
		this.sdms_listener.start();
		
	}
	
	private void addToSlave(){
		ClientConnectionDelegator con = new ClientConnectionDelegator();
		JSONObject register = new JSONObject();
		register.put("client_id", this.id);
		register.put("op_type", "add Client");
		con.sendMsg(register);
	}
	
	public int getClientId(){
		return this.id;
	}
	
	public static void main(String[] args){
		String host = args[0];
		int port= Integer.parseInt(args[1]);
		
		Client client = new Client(host, port);
		client.connect();
		for(;;){
			Scanner scan = new Scanner(System.in);
			System.out.println("1. add topic");
			System.out.println("2. remove topic");
			System.out.println("3. publish");
			int select = scan.nextInt();
			if(select == 1){
				System.out.println("input topic : ");
				String topic = scan.next();
				client.addTopic(topic);
			}else if(select == 2){
				System.out.println("input topic : ");
				String topic = scan.next();
				client.removeTopic(topic);
				
			}else if(select == 3){
				System.out.println("input topic : ");
				String topic = scan.next();
				System.out.println("input message : ");
				String message = scan.next();
				client.publish(topic, message);
				
			}
		}
	}
}
