import java.util.Vector;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class JavaSensor implements MqttCallback {

	Vector ArrivedData = new Vector();
	MqttClient client;

	public JavaSensor() {
	}

	public static void main(String[] args) {
		new JavaSensor().exportacao();
	}

	public void exportacao() {
		String MessageText;
		try {
			// client = new MqttClient("tcp://iot.eclipse.org:1883","/sid_lab_2020");
			client = new MqttClient("tcp://broker.mqtt-dashboard.com:1883", "/sid_lab_2020");
			client.connect();
			client.setCallback(this);
			client.subscribe("/sid_lab_2020");
		} catch (MqttException e) {
			System.out.println("erro");
			e.printStackTrace();
		}
	}

	public void connectionLost(Throwable cause) {
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		ArrivedData.addElement(message.toString());
		System.out.println(message);
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
	}

}