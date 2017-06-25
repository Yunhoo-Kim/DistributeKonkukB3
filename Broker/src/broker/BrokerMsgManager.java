package broker;

import org.json.simple.JSONObject;

public class BrokerMsgManager extends MsgManager {
	BrokerListener broker_listener;
	public BrokerMsgManager(BrokerListener broker){
		this.broker_listener = broker;
	}

	public void decodeToExecute(String msg){
		JSONObject j_data = this.decodeMsg(msg);
		BrokerWorker worker = new BrokerWorker(this);
		worker.workExecutor(j_data);
	}
}
