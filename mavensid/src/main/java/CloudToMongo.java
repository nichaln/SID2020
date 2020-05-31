import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;

import javax.swing.JOptionPane;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;

public class CloudToMongo implements MqttCallback {
	MqttClient mqttclient;
	static MongoClient mongoClient;
	static DB db;
	static DBCollection mongocolmedicoes;
	static DBCollection mongocolerros;
	static String cloud_server = new String();
	static String cloud_topic = new String();
	static String mongo_host = new String();
	static String mongo_database = new String();
	static String mongo_collection_medicoes = new String();
	static String mongo_collection_erros = new String();
	private LinkedList<Medicao> medicoesAnteriores = new LinkedList<Medicao>(); // leituras todas mesmo

	private static final double VARIACAO_MAXIMA_TEMPERATURA = 5;
	private static final double VARIACAO_MAXIMA_HUMIDADE = 4;
	private static final double VARIACAO_MAXIMA_LUMINOSIDADE = 100;

	private double ultimaMedicaoTemp = 0;
	private double ultimaMedicaoHum = 0;
	private double ultimaMedicaoLum = 0;

	private int errosRajadaTemp;
	private int errosRajadaHum;
	private int errosRajadaLum;

	private boolean startup = true, testing = true;

	public static void main(String[] args) {

		try {
			Properties p = new Properties();
			p.load(new FileInputStream("CloudToMongo.ini"));
			cloud_server = p.getProperty("cloud_server");
			cloud_topic = p.getProperty("cloud_topic");
			mongo_host = p.getProperty("mongo_host");
			mongo_database = p.getProperty("mongo_database");
			mongo_collection_medicoes = p.getProperty("mongo_collection1");
			mongo_collection_erros = p.getProperty("mongo_collection2");
		} catch (Exception e) {

			System.out.println("Error reading CloudToMongo.ini file " + e);
			JOptionPane.showMessageDialog(null, "The CloudToMongo.ini file wasn't found.", "CloudToMongo",
					JOptionPane.ERROR_MESSAGE);
		}
		new CloudToMongo().connectCloud();
		new CloudToMongo().connectMongo();

	}

	public void connectCloud() {
		inicializarVetorMedicoes();
		int i;
		try {
			i = new Random().nextInt(100000);
			mqttclient = new MqttClient(cloud_server, "CloudToMongo_" + String.valueOf(i) + "_" + cloud_topic);
			mqttclient.connect();
			mqttclient.setCallback(this);
			if(testing)
				mqttclient.subscribe("/grupo26topico");
			else
				mqttclient.subscribe(cloud_topic);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void connectMongo() {
//		mongoClient = new MongoClient(new MongoClientURI(mongo_host));
		mongoClient = new MongoClient(Arrays.asList(new ServerAddress("localhost",27017),new ServerAddress("localhost",25017), new ServerAddress("localhost",23017)));
		db = mongoClient.getDB(mongo_database);
		mongocolmedicoes = db.getCollection(mongo_collection_medicoes);
		mongocolerros = db.getCollection(mongo_collection_erros);
		errosRajadaTemp = 0;
		errosRajadaHum = 0;
		errosRajadaLum = 0;
	}

	public void messageArrived(String topic, MqttMessage c) {
		try {
			receberMensagem(c.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void connectionLost(Throwable cause) {
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
	}

	public String clean(String message) {
		return (message.replaceAll("\"\"", "\","));

	}

	public void inicializarVetorMedicoes() {
		for (int i = 0; i < 5; i++) {
			medicoesAnteriores.add(new Medicao());
		}
	}

	public void receberMensagem(String mensagem) {
		String[] vetor = mensagem.split(",");
		String[] parsed = new String[vetor.length];
		for (int i = 0; i != vetor.length; i++) {
			parsed[i] = vetor[i].split("\":\"")[1].replaceAll("\"", "");
		}
		String[] auxTime = parsed[2].split("/");
		String timestamp = auxTime[2] + "-" + auxTime[1] + "-" + auxTime[0] + " " + parsed[3];
		System.out.println(timestamp);
		System.out.println(mensagem);
		
		String[] parsedMessage = parseMessage(mensagem);		
		String valorTmp = parsedMessage[0], valorHum = parsedMessage[1];
		String valorDat = parsedMessage[2], valorTim = parsedMessage[3];
		String valorCel = parsedMessage[4], valorMov = parsedMessage[5];
		
		Medicao m = new Medicao();
		m.setDate(valorDat + " " + valorTim);
		System.out.println("DATE SET: " + valorDat + " " + valorTim);

		if (verify(valorTmp, 't', timestamp) ) {// se correu tudo bem, escreve na medicoessensores e atualiza o vetor
			m.setMedicaoTemperatura("" + Double.parseDouble(valorTmp));
			BasicDBObject document = new BasicDBObject();// não passar para cima por causa do ObjectId
			document.put("TipoSensor", "TEM");
			document.put("ValorMedicao", Double.parseDouble(valorTmp));
			document.put("DataHoraMedicao", timestamp);
			mongocolmedicoes.insert(document);
		} else { // ser string ou valor elevado
			m.setMedicaoTemperatura(valorTmp);
			BasicDBObject document = new BasicDBObject();
			document.put("ValorMedicao", valorTmp);
			document.put("TipoSensor", "TEM");
			document.put("DataHoraMedicao", timestamp);
			ArrayList<DBObject> array = historyTemperatura();
			document.put("ValoresAnteriores", array);
			mongocolerros.insert(document);
		}

		if (verify(valorHum, 'h', timestamp) ) {
			m.setMedicaoHumidade("" + Double.parseDouble(valorHum));
			BasicDBObject document = new BasicDBObject();// não passar para cima por causa do ObjectId
			document.put("TipoSensor", "HUM");
			document.put("ValorMedicao", Double.parseDouble(valorHum));
			document.put("DataHoraMedicao", timestamp);
			mongocolmedicoes.insert(document);
		} else {
			m.setMedicaoHumidade(valorHum);
			BasicDBObject document = new BasicDBObject();
			document.put("ValorMedicao", valorHum);
			document.put("TipoSensor", "HUM");
			document.put("DataHoraMedicao", timestamp);
			ArrayList<DBObject> array = historyHumidade();
			document.put("ValoresAnteriores", array);
			mongocolerros.insert(document);
		}

		if (verify(valorCel, 'c', timestamp) ) {
			m.setMedicaoLuminosidade("" + Integer.parseInt(valorCel));
			BasicDBObject document = new BasicDBObject();// não passar para cima por causa do ObjectId
			document.put("TipoSensor", "CEL");
			document.put("ValorMedicao", Integer.parseInt(valorCel));
			document.put("DataHoraMedicao", timestamp);
			mongocolmedicoes.insert(document);
		} else {
			m.setMedicaoLuminosidade(valorCel);
			BasicDBObject document = new BasicDBObject();
			document.put("ValorMedicao", valorCel);
			document.put("TipoSensor", "CEL");
			document.put("DataHoraMedicao", timestamp);
			ArrayList<DBObject> array = historyLuminosidade();
			document.put("ValoresAnteriores", array);
			mongocolerros.insert(document);
		}

		if (verify(valorMov, 'm', timestamp) ) {
			m.setMedicaoMovimento("" + Integer.parseInt(valorMov));
			BasicDBObject document = new BasicDBObject();// não passar para cima por causa do ObjectId
			document.put("TipoSensor", "MOV");
			document.put("ValorMedicao", Integer.parseInt(valorMov));
			document.put("DataHoraMedicao", timestamp);
			mongocolmedicoes.insert(document);
		} else {
			m.setMedicaoMovimento(valorMov);
			BasicDBObject document = new BasicDBObject();
			document.put("ValorMedicao", valorMov);
			document.put("TipoSensor", "MOV");
			document.put("DataHoraMedicao", timestamp);
			ArrayList<DBObject> array = historyMovimento();
			document.put("ValoresAnteriores", array);
			mongocolerros.insert(document);
		}

		medicoesAnteriores.pop();
		medicoesAnteriores.add(m);
		for (Medicao x : medicoesAnteriores)
			System.out.println(x.getDate() + " || " + x.getMedicaoTemperatura() + " || " + x.getMedicaoHumidade()
					+ " || " + x.getMedicaoLuminosidade() + " || " + x.getMedicaoMovimento());

	}
	
	private String[] parseMessage(String message) {
		String[] parsedMessage = new String[6];
		for(int i = 0; i != parsedMessage.length; i++)
			parsedMessage[i] = 0+"";
		String[] messageArray = message.split("\"*\\s*,\\s*\"*");
		messageArray[0] = messageArray[0].replace("{\"", "");
		messageArray[messageArray.length-1] = messageArray[messageArray.length-1].replace("\"}", "");
		String[][] auxiliar = new String[messageArray.length][2];
		for (int i = 0; i != messageArray.length; i++) {
			//System.out.println(messageArray[i]);
			auxiliar[i] = messageArray[i].split("\":\"");
		}
		System.out.println("-------------------------------");
		for (int i = 0; i != auxiliar.length; i++) {
			System.out.println(auxiliar[i][0] + " " + auxiliar[i][1]);
			if (auxiliar[i][0].contains("tmp"))
				parsedMessage[0] = auxiliar[i][1];
			if (auxiliar[i][0].contains("hum"))
				parsedMessage[1] = auxiliar[i][1];
			if (auxiliar[i][0].contains("dat"))
				parsedMessage[2] = auxiliar[i][1];
			if (auxiliar[i][0].contains("tim"))
				parsedMessage[3] = auxiliar[i][1];
			if (auxiliar[i][0].contains("cell"))
				parsedMessage[4] = auxiliar[i][1];
			if (auxiliar[i][0].contains("mov"))
				parsedMessage[5] = auxiliar[i][1];
		}
		return parsedMessage;
	}
	
	private boolean verify(String aux, char key, String timestamp) {
		double leitura;
		switch (key) {
		case 't':
			try {
				leitura = Double.parseDouble(aux);
			} catch (NumberFormatException e) {// escreve nos erros
				errosRajadaTemp++;
				return false;
			}
			if (checkValidValueTemperatura(leitura)) {// TODO espetáculo regista aí
				ultimaMedicaoTemp = leitura;
				errosRajadaTemp = 0;
				return true;
			} else { //
				errosRajadaTemp++;
				return false;
			}
		case 'h':
			try {
				leitura = Double.parseDouble(aux);
			} catch (NumberFormatException e) {// escreve nos erros
				errosRajadaHum++;
				return false;
			}
			if (checkValidValueHumidade(leitura)) {// TODO espetáculo regista aí
				ultimaMedicaoHum = leitura;
				errosRajadaHum = 0;
				return true;
			} else {//
				errosRajadaHum++;
				return false;
			}
		case 'c':
			try {
				leitura = Double.parseDouble(aux);
			} catch (NumberFormatException e) {// escreve nos erros
				errosRajadaLum++;
				return false;
			}
			if (checkValidValueLuminosidade(leitura)) {// TODO espetáculo regista aí
				ultimaMedicaoLum = leitura;
				errosRajadaLum = 0;
				return true;
			} else {//
				errosRajadaLum++;
				return false;
			}
		case 'm':
			try {
				leitura = Double.parseDouble(aux);
			} catch (NumberFormatException e) {// escreve nos erros
				return false;
			}
			if (leitura == 0 || leitura == 1) {// TODO espetáculo regista aí
				return true;
			} else {//
				return false;
			}
		default:
			return false;

		}
	}

	public class AuxValoresAnteriores implements Serializable {

		private static final long serialVersionUID = 1L;
		String valorMedicao;
		String data;

		public AuxValoresAnteriores(String valorMedicao, String data) {
			this.valorMedicao = valorMedicao;
			this.data = data;
		}

		public DBObject convertBson() {
			BasicDBObject document = new BasicDBObject();
			document.put("ValorMedicao", valorMedicao);
			document.put("DataHoraMedicao", data);
			return document;
		}

	}

	private ArrayList<DBObject> historyTemperatura() {
		ArrayList<DBObject> historico = new ArrayList<DBObject>();
		for (Medicao m : medicoesAnteriores) {
			historico.add(new AuxValoresAnteriores(m.getMedicaoTemperatura(), m.getDate()).convertBson());
		}
		return historico;
	}

	private ArrayList<DBObject> historyHumidade() {
		ArrayList<DBObject> historico = new ArrayList<DBObject>();
		for (Medicao m : medicoesAnteriores) {
			historico.add(new AuxValoresAnteriores(m.getMedicaoHumidade(), m.getDate()).convertBson());
		}
		return historico;
	}

	private ArrayList<DBObject> historyLuminosidade() {
		ArrayList<DBObject> historico = new ArrayList<DBObject>();
		for (Medicao m : medicoesAnteriores) {
			historico.add(new AuxValoresAnteriores(m.getMedicaoLuminosidade(), m.getDate()).convertBson());
		}
		return historico;
	}

	private ArrayList<DBObject> historyMovimento() {
		ArrayList<DBObject> historico = new ArrayList<DBObject>();
		for (Medicao m : medicoesAnteriores) {
			historico.add(new AuxValoresAnteriores(m.getMedicaoMovimento(), m.getDate()).convertBson());
		}
		return historico;
	}

	private boolean checkValidValueTemperatura(Double leitura) {
		if (startup) {
			return true;
		}
		if (Math.abs(leitura - ultimaMedicaoTemp) > VARIACAO_MAXIMA_TEMPERATURA	+ VARIACAO_MAXIMA_TEMPERATURA * errosRajadaTemp) {
			// aconteceu um erro de leitura elevada
			return false;
		} else {
			return true;
		}
	}

	private boolean checkValidValueHumidade(Double leitura) {
		if (startup) {
			return true;
		}
		if (Math.abs(leitura - ultimaMedicaoHum) > VARIACAO_MAXIMA_HUMIDADE	+ VARIACAO_MAXIMA_HUMIDADE * errosRajadaHum) {
			// aconteceu um erro de leitura elevada
			return false;
		} else {
			return true;
		}
	}

	private boolean checkValidValueLuminosidade(Double leitura) {
		if (startup) {
			startup = false; // tá aqui porque a lum é a ultima a ser verificado
			return true;
		}
		if (Math.abs(leitura - ultimaMedicaoLum) > VARIACAO_MAXIMA_LUMINOSIDADE + VARIACAO_MAXIMA_LUMINOSIDADE * errosRajadaLum 
				|| leitura < 0) {
			// aconteceu um erro de leitura elevada ou de valor negativo
			return false;
		} else {
			return true;
		}
	}

}