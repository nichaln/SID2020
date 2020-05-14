import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
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

	private double ultimaMedicaoTemp = Double.POSITIVE_INFINITY;
	private double ultimaMedicaoHum = Double.POSITIVE_INFINITY;
	private double ultimaMedicaoLum = Double.POSITIVE_INFINITY;

	private int errosRajadaTemp;
	private int errosRajadaHum;
	private int errosRajadaLum;

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
		new CloudToMongo().connecCloud();
		new CloudToMongo().connectMongo();
	}

	public void connecCloud() {
		inicializarVetorMedicoes();
		int i;
		try {
			i = new Random().nextInt(100000);
			mqttclient = new MqttClient(cloud_server, "CloudToMongo_" + String.valueOf(i) + "_" + cloud_topic);
			mqttclient.connect();
			mqttclient.setCallback(this);
			mqttclient.subscribe(cloud_topic);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void connectMongo() {
		mongoClient = new MongoClient(new MongoClientURI(mongo_host));
		db = mongoClient.getDB(mongo_database);
		mongocolmedicoes = db.getCollection(mongo_collection_medicoes);
		mongocolerros = db.getCollection(mongo_collection_erros);
	}

	public void messageArrived(String topic, MqttMessage c) throws Exception {

		try {
			DBObject document_json;

//			document_json = (DBObject) JSON.parse(clean(c.toString()));
			System.out.println(c.toString());
			receberMensagem(c.toString());
//			mongocol.insert(document_json);
		} catch (Exception e) {
			System.out.println(e);
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
		String timestamp = auxTime[2] + "-" + auxTime[1] + "-" + auxTime[0] + " " + parsed[3]; // TODO valor
																								// correspondente do
																								// timestamp
		System.out.println(timestamp);

		
		Medicao m = new Medicao();
		m.setDate(timestamp);

		int i = 0;
		for (String s : parsed) {
			if (i == 0) {// temperatura
				if (verify(s, 't', timestamp)) {// se correu tudo bem, escreve na medicoessensores e atualiza o vetor
					m.setMedicaoTemperatura("" + Double.parseDouble(s));
					// TODO Escrever na coleçao mongoDB
					BasicDBObject document = new BasicDBObject();
					document.put("TipoSensor", "TEM");
					document.put("ValorMedicao",Double.parseDouble(s));
					document.put("DataHoraMedicao", timestamp);
					mongocolmedicoes.insert(document);

				} else { // ser string ou valor elevado
					m.setMedicaoTemperatura(s);
				}
			}
			if (i == 1) {// humidade
				if (verify(s, 'h', timestamp)) {
					m.setMedicaoHumidade("" + Double.parseDouble(s));
					// TODO escrever na colecao mongoDB
				} else {
					m.setMedicaoHumidade(s);
				}

			}
			// TODO Parse data
			if (i == 4) {// luminosidade
				if (verify(s, 'c', timestamp)) {
					m.setMedicaoLuminosidade("" + Integer.parseInt(s));
					// TODO escrever na colecao mongoDB
				} else {
					m.setMedicaoLuminosidade(s);
				}

			}
			/*
			 * if (i == 3) { if(verify(s, 't', timestamp))
			 * m.setMedicaoTemperatura(Double.parseDouble(s)); }
			 */

			i++;

		}
		medicoesAnteriores.pop();
		medicoesAnteriores.add(m);
		for (Medicao x : medicoesAnteriores)
			System.out.println(x.getDate() + " || " + x.getMedicaoTemperatura() + " || " + x.getMedicaoHumidade()
					+ " || " + x.getMedicaoLuminosidade());

	}

	private boolean verify(String aux, char key, String timestamp) {
		switch (key) {
		case 't':
			double leitura;
			try {
				leitura = Double.parseDouble(aux);
			} catch (NumberFormatException e) {// escreve nos erros
				/* ESCREVER A CENA NA COLEÇAO ERROSMEDICOESSENSORES DO MONGODB */
				BasicDBObject document = new BasicDBObject();
				document.put("ValorMedicao", aux);
				document.put("TipoSensor", "TEM");
				document.put("DataHoraMedicao", timestamp);
				ArrayList< DBObject > array = historyTemperatura();
				document.put("ValoresAnteriores", array);
				
				mongocolerros.insert(document);

				return false;
			}
			if (checkValidValueTemperatura(leitura)) {// TODO espetáculo regista aí
				ultimaMedicaoTemp = leitura;
				return true;
			} else {// fora da graça de deus
//				MedicaoErro me = new MedicaoErro(aux, timestamp, historyTemperatura());

				/*
				 * TODO mandar esta me para o spot de guardar
				 */
				return false;
			}
//				
		case 'h':
		case 'c':
		default:
			return false;

		}
	}
	
	public class AuxValoresAnteriores implements Serializable{

		private static final long serialVersionUID = 1L;
		String valorMedicao;
		String data;
		
		public AuxValoresAnteriores(String valorMedicao,String data) {
			this.valorMedicao=valorMedicao;
			this.data=data;
		}
		
		public DBObject convertBson() {
			BasicDBObject document = new BasicDBObject();
			document.put( "ValorMedicao",   valorMedicao );
			document.put( "DataHoraMedicao", data );
			return document;
		}
		
	}

	private ArrayList<DBObject> historyTemperatura() {
		ArrayList<DBObject> historico = new ArrayList<DBObject>();
		for (Medicao m : medicoesAnteriores) {
			historico.add(new AuxValoresAnteriores(m.getMedicaoTemperatura(),m.getDate()).convertBson());
		}
		return historico;
	}

	private ArrayList<DBObject> historyHumidade() {
		ArrayList<DBObject> historico = new ArrayList<DBObject>();
		for (Medicao m : medicoesAnteriores) {
			historico.add(new AuxValoresAnteriores(m.getMedicaoHumidade(),m.getDate()).convertBson());
		}
		return historico;
	}

	private ArrayList<DBObject> historyLuminosidade() {
		ArrayList<DBObject> historico = new ArrayList<DBObject>();
		for (Medicao m : medicoesAnteriores) {
			historico.add(new AuxValoresAnteriores(m.getMedicaoLuminosidade(),m.getDate()).convertBson());
		}
		return historico;
	}

	private boolean checkValidValueTemperatura(Double leitura) {
		if (leitura - ultimaMedicaoTemp > 5) {// este 5 tem de ser falado
			// aconteceu um erro de leitura elevada
			return false;
		} else {
			return true;
		}
	}

}