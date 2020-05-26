import java.io.FileInputStream;
import java.sql.*;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.mongodb.*;
import java.text.*;

public class MongoToMySQL {

	public LinkedList<Medicao> medicoesSensores = new LinkedList<Medicao>();
	Temperaturas t = new Temperaturas();
	Humidade h = new Humidade();
	Luminosidade l = new Luminosidade();
	Movimento m = new Movimento();

	static String mongo_host = new String();
	static String mongo_database = new String();
	static String mongo_collection = new String();
	DBCollection medicoes;

	static Connection SQLconn;
	static Statement SQLstatement;

	public void connectToMongo() {
		try {
			Properties p = new Properties();
			p.load(new FileInputStream("CloudToMongo.ini"));
			mongo_host = p.getProperty("mongo_host");
			mongo_database = p.getProperty("mongo_database");
			mongo_collection = p.getProperty("mongo_collection1");
		} catch (Exception e) {

			System.out.println("Error reading CloudToMongo.ini file " + e);
			JOptionPane.showMessageDialog(null, "The CloudToMongo.ini file wasn't found.", "CloudToMongo",
					JOptionPane.ERROR_MESSAGE);
		}

		MongoClient mongoClient1 = new MongoClient(new MongoClientURI(mongo_host)); // ?

		DB db = mongoClient1.getDB(mongo_database);
		medicoes = db.getCollection(mongo_collection); // Coleção das Medições
	}

	public void connectToMySQL() {
		String sql_password = new String();
		String sql_user = new String();
		String sql_connection = new String();
		int maxIdCliente = 0;
		sql_password = "pass";
		sql_user = "transporter";
		sql_connection = "jdbc:mysql://localhost/museu2";//mudar aqui o nome da base de dados!

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			SQLconn = DriverManager.getConnection(sql_connection + "?user=" + sql_user + "&password=" + sql_password);
			System.out.println("passou");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Server down, unable to make the connection. ");
		}

	}

	public void readFromMongo() throws InterruptedException {
		DBObject dboobject;
		int i = 0;
		while(true) {
//			DBCursor cursor = medicoes.find();
			
//			while (cursor.hasNext()) {
//				String read = cursor.next().toString();
				dboobject = medicoes.findOne();
				if(dboobject != null) {
					String read = dboobject.toString();
					System.out.println(read);
					String TipoSensor = read.substring(read.indexOf("TipoSensor\": "), read.indexOf(", \"Valor")).split(": ")[1].replace("\"","");
					Double ValorMedicao = Double.parseDouble(read.substring(read.indexOf("ValorMedicao\": "), read.indexOf(", \"Data")).split(":")[1]);
					int id = i;
					i++;
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date = new Date(System.currentTimeMillis());
					String DataHoraMedicao = formatter.format(date);
					System.out.println(id+ "-"+ ValorMedicao+ "-"+  TipoSensor+ "-"+ DataHoraMedicao);
					writeToMySQL(id, ValorMedicao, TipoSensor, DataHoraMedicao);
					medicoes.remove(dboobject);
				}else {
					Thread.sleep(2000);
				}
//			}
		}
	}

	public void writeToMySQL(int id, Double valorMedicao, String tipoSensor, String dataHoraMedicao) {
		ResultSet rs;
		try {
			SQLstatement = SQLconn.createStatement();
			/*rs = SQLstatement.executeQuery("Select * from alerta");
			while (rs.next())
				System.out.println(rs.getString("Descricao"));*/

			SQLstatement.executeUpdate("Insert into medicoessensores (IDMedicao, ValorMedicao, TipoSensor, DataHoraMedicao)"
					+ " values (" + id + ", "+ valorMedicao + ", '" + tipoSensor + "', '" + dataHoraMedicao + "');");
		} catch (Exception e) {
			System.out.println("Error quering  the database . " + e);
		}

	}

	public void separarMedicoes() {
		for (Medicao ms : medicoesSensores) {
			t.processar(Double.parseDouble(ms.getMedicaoTemperatura()));
			h.processar(Double.parseDouble(ms.getMedicaoHumidade()));
			l.processar(Integer.parseInt(ms.getMedicaoLuminosidade()));
			m.processar(Integer.parseInt(ms.getMedicaoMovimento()));

		}
	}

	public static void main(String[] args) throws InterruptedException {
		MongoToMySQL m = new MongoToMySQL();
		m.connectToMongo();
		m.connectToMySQL();
		m.readFromMongo();
	}

}
