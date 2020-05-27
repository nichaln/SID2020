import java.io.FileInputStream;
import java.sql.*;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.mongodb.*;
import java.text.*;

public class MongoToMySQL {

	public LinkedList<Medicao> medicoesSensores = new LinkedList<Medicao>();
	Temperaturas t = new Temperaturas(this);
	Humidade h = new Humidade(this);
	Luminosidade l = new Luminosidade(this);
	Movimento m = new Movimento(this);

	static String mongo_host = new String();
	static String mongo_database = new String();
	static String mongo_collection = new String();
	DBCollection medicoes;
	int idAlertas = 0;

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

		MongoClient mongoClient1 = new MongoClient(new MongoClientURI(mongo_host));

		DB db = mongoClient1.getDB(mongo_database);
		medicoes = db.getCollection(mongo_collection); // Coleção das Medições
	}

	public void connectToMySQL() {
		String sql_password = new String();
		String sql_user = new String();
		String sql_connection = new String();
		sql_user = "transporter";
		sql_password = "pass";
		sql_connection = "jdbc:mysql://localhost/museu2";// mudar aqui o nome da base de dados!

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			SQLconn = DriverManager.getConnection(sql_connection + "?user=" + sql_user + "&password=" + sql_password);
			System.out.println("passou");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Server down, unable to make the connection.");
		}

	}

	public void readFromMongo() throws InterruptedException {
		DBObject dboobject;
		int i = 0;
		while (true) {
//			DBCursor cursor = medicoes.find();

//			while (cursor.hasNext()) {
//				String read = cursor.next().toString();
			dboobject = medicoes.findOne();
			if (dboobject != null) {
				String read = dboobject.toString();
				System.out.println(read);
				String TipoSensor = read.substring(read.indexOf("TipoSensor\": "), read.indexOf(", \"Valor"))
						.split(": ")[1].replace("\"", "");
				Double ValorMedicao = Double.parseDouble(
						read.substring(read.indexOf("ValorMedicao\": "), read.indexOf(", \"Data")).split(":")[1]);
				int id = i;
				i++;
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = new Date(System.currentTimeMillis());
				String DataHoraMedicao = formatter.format(date);
				System.out.println(id + "-" + ValorMedicao + "-" + TipoSensor + "-" + DataHoraMedicao);
				writeMedicaoToMySQL(id, ValorMedicao, TipoSensor, DataHoraMedicao);
				if (TipoSensor.equals("TEM")) {
					t.processar(ValorMedicao);
				}
				if (TipoSensor.equals("HUM")) {
					h.processar(ValorMedicao);
				}
				if (TipoSensor.equals("CEL")) {
					l.processar(ValorMedicao);
				}
				if (TipoSensor.equals("MOV")) {
					m.processar(ValorMedicao);
				}
				medicoes.remove(dboobject);
			} else {
				Thread.sleep(2000);
			}
//			}
		}
	}

	private void writeMedicaoToMySQL(int id, Double valorMedicao, String tipoSensor, String dataHoraMedicao) {
		try {
			SQLstatement = SQLconn.createStatement();
			SQLstatement.executeUpdate(
					"Insert into medicoessensores (IDMedicao, ValorMedicao, TipoSensor, DataHoraMedicao)" + " values ("
							+ id + ", " + valorMedicao + ", '" + tipoSensor + "', '" + dataHoraMedicao + "');");
		} catch (Exception e) {
			System.out.println("Erro a escrever uma medição. " + e);
		}
	}

	public void writeAlertaToMySQL(String tipoSensor, String valorMedicao, String limite, String descricao,
			String controlo, String extra) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		String dataHoraMedicao = formatter.format(date);
		try {
			SQLstatement = SQLconn.createStatement();
			SQLstatement.executeUpdate(
					"Insert into alerta (ID, DataHoraMedicao, TipoSensor, ValorMedicao, Limite, Descricao, Controlo, Extra)"
							+ " values (" + idAlertas++ + ", '" + dataHoraMedicao + "', '" + tipoSensor + "', "
							+ valorMedicao + ", " + limite + ", " + null + ", " + 0 + ", " + null + ");");

			// ID DataHoraMedicao TipoSensor ValorMedicao Limite Descricao Controlo Extra
		} catch (Exception e) {
			System.out.println("Erro a escrever um alerta. " + e);
		}
	}

	public void separarMedicoes() {
		for (Medicao ms : medicoesSensores) {
			t.processar(Double.parseDouble(ms.getMedicaoTemperatura()));
			h.processar(Double.parseDouble(ms.getMedicaoHumidade()));
			l.processar(Double.parseDouble(ms.getMedicaoLuminosidade()));
			m.processar(Double.parseDouble(ms.getMedicaoMovimento()));

		}
	}

	private void updateLimites() {
		new Thread(new Runnable() {
			public void run() {
				String SqlCommando = new String();
				ResultSet rs;
				int id = 0;
				double maxTemp = 0.0;
				double maxHum = 0.0;
				double maxLum = 0.0;
				SqlCommando = "Select max(Sistema_ID) as MaxID from sistema;";
				try {
					SQLstatement = SQLconn.createStatement();
					rs = SQLstatement.executeQuery(SqlCommando);
					while(rs.next())
						id = rs.getInt("MaxID");
				} catch (Exception e) {
					System.out.println("Erro a ler o ID Máximo. " + e);
				}
				SqlCommando = "Select LimiteTemperatura, LimiteHumidade, LimiteLuminosidade from sistema where Sistema_ID = "+id+";";
				try {
					SQLstatement = SQLconn.createStatement();
					rs = SQLstatement.executeQuery(SqlCommando);
					while(rs.next()) {
						maxTemp = rs.getDouble("LimiteTemperatura");
						maxHum = rs.getDouble("LimiteHumidade");
						maxLum = rs.getDouble("LimiteLuminosidade");
						//System.out.println("Li os valores bro "+maxTemp+" "+maxHum+" "+maxLum);
					}
				} catch (Exception e) {
					System.out.println("Erro a ler os limites máximos. " + e);
				}
				/*SqlCommando = "Select LimiteHumidade as MaximoHum from sistema;";
				try {
					SQLstatement = SQLconn.createStatement();
					rs = SQLstatement.executeQuery(SqlCommando);
					maxHum = rs.getInt("id"); // does this work?
				} catch (Exception e) {
					System.out.println("Erro a ler o limite máximo da Humidade. " + e);
				}
				SqlCommando = "Select LimiteLuminosidade as MaximoLum from sistema;";
				try {
					SQLstatement = SQLconn.createStatement();
					rs = SQLstatement.executeQuery(SqlCommando);
					maxLum = rs.getInt("id"); // does this work?
				} catch (Exception e) {
					System.out.println("Erro a ler o limite máximo da Luminosidade. " + e);
				}*/
				t.updateLimite(maxTemp);
				l.updateLimite(maxLum);
				h.updateLimite(maxHum);
				try { // Vou dormir um dia :)
					Thread.sleep(86400);
				} catch (InterruptedException e) {
					System.err.println("Alguém acordou o verificador dos limites");
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void main(String[] args) throws InterruptedException {
		MongoToMySQL m = new MongoToMySQL();
		m.connectToMongo();
		m.connectToMySQL();
		m.updateLimites();
		m.readFromMongo();
	}

}
