package broker;

public class MsgReceiveManager {
	public static void main(String[] args){
		Broker b = new Broker(5000);
		new Thread(b).start();
//		System.out.println("Processor : " + Runtime.getRuntime().availableProcessors());
//		System.out.println("Free memory : " + Runtime.getRuntime().freeMemory());
//		MsgManager mana = new MsgManager();
//		BrokerWorker worker = new BrokerWorker(mana);
	}
}
