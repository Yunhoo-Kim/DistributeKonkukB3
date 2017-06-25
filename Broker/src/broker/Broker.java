package broker;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import logging.Logging;

public class Broker implements Runnable {
	String host = "";
	int port = 0;
	// int[] round_robin = {};
	LinkedList<JSONObject> r_r = new LinkedList<JSONObject>();
	int WEIGHT_AVG = 10;
	int cnt = 0;
	ArrayList<JSONObject> master_table = new ArrayList<JSONObject>();

	// logger
	// singleton
	private static Broker instance;

	public Broker(int port) {
		this.host = host;
		this.port = port;
		instance = this;
	}

	public static synchronized Broker getInstance() {
		return instance;
	}

	public void run() {
		ServerSocket server = null;
		try {
			server = new ServerSocket(this.port);
			Logging.logger.info("Broker Server Start");
			while (true) {
				Socket sock = server.accept();
				Logging.logger.info("Connection Received");
				new Thread(new BrokerListener(sock)).start();
			}
		} catch (Exception ex) {
		} finally {
			if (server != null) {
				try {
					server.close();
				} catch (IOException io) {

				}
			}
		}
	}

	public void runServer() {
		new Thread(this).start();
	}

	public ArrayList<JSONObject> getMasterTable() {
		return this.master_table;
	}

	public void addMaster(String ip, int port, int id, int weight) {
		JSONObject data = new JSONObject();

		data.put("ip", ip);
		data.put("port", port);
		data.put("node_id", id);
		data.put("weight", weight);

		this.master_table.add(data);

		int workload = weight / this.WEIGHT_AVG;

		for (int i = 0; i <= workload; i++) {
			this.r_r.addLast(data);
		}
		
		Logging.logger.info("master reigster");
	}

	public void updateMaster(int prev_id, String ip, int port, int id,
			int weight) {
		// Iterator<JSONObject> j_iter = this.master_table.iterator();
		// Iterator<JSONObject> list = this.r_r.iterator();
		for (int i = 0; i < this.master_table.size(); i++) {
			JSONObject temp = (JSONObject) this.master_table.get(i);
			if ((Integer) temp.get("node_id") == prev_id) {
				this.master_table.remove(temp);
			}
		}
		for (int i = 0; i < this.r_r.size(); i++) {
			JSONObject temp = (JSONObject) this.r_r.get(i);
			if ((Integer) temp.get("node_id") == prev_id) {
				this.r_r.remove(temp);
			}
		}

		this.addMaster(ip, port, id, weight);

	}

	public void removeMaster(int id) {
		Iterator<JSONObject> j_iter = this.master_table.iterator();

		while (j_iter.hasNext()) {
			JSONObject temp = (JSONObject) j_iter.next();
			if (((Number) temp.get("node_id")).intValue() == id) {
				this.master_table.remove(temp);
			}
		}
	}

	private void allocateWorkload() {

		

	}

	public class WeightComparator implements Comparator<JSONObject> {

		@Override
		public int compare(JSONObject o1, JSONObject o2) {
			// TODO Auto-generated method stub
			Integer weight1 = ((Number) o1.get("weight")).intValue();
			Integer weight2 = ((Number) o2.get("weight")).intValue();
			return weight1.compareTo(weight2);
		}

	}

	public JSONObject loadBalancing() {
		JSONObject return_value = new JSONObject();
		return_value = this.r_r.get(this.cnt % this.r_r.size());
		this.cnt++;
		return return_value;
	}

}
