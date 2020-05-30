import java.io.FileInputStream;
import java.sql.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.mongodb.*;
import java.text.*;

public class MongoToMySQL {

	public LinkedList<Medicao> medicoesSensores = new LinkedList<Medicao>();
	LinkedList<Date> rondas = new LinkedList<Date>();
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
	
	private static final int LIMITE_RONDA = 15;

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
		
//		MongoClient mongoClient1 = new MongoClient(new MongoClientURI(mongo_host));
		MongoClient mongoClient1 = new MongoClient(Arrays.asList(new ServerAddress("localhost",27017),new ServerAddress("localhost",25017), new ServerAddress("localhost",23017)));
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
		String SqlCommando = new String();
		ResultSet rs;
		int i=0;
		SqlCommando = "Select max(IDMedicao) as maxID from medicoessensores;";
		try {
			SQLstatement = SQLconn.createStatement();
			rs = SQLstatement.executeQuery(SqlCommando);
			while(rs.next()) {
				i = rs.getInt("maxID") +1;
			}
		} catch (Exception e) {
			System.out.println("Erro ao obter id maximo da tabela medicoessensores " + e);
		}
		DBObject dboobject;
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
		String SqlCommando = new String();
		ResultSet rs;
		int i=0;
		SqlCommando = "Select max(ID) as maxID from alerta;";
		try {
			SQLstatement = SQLconn.createStatement();
			rs = SQLstatement.executeQuery(SqlCommando);
			while(rs.next()) {
				i = rs.getInt("maxID") +1;
			}
		} catch (Exception e) {
			System.out.println("Erro ao obter id maximo da tabela alerta " + e);
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		String dataHoraMedicao = formatter.format(date);
		try {
			SQLstatement = SQLconn.createStatement();
			SQLstatement.executeUpdate(
					"Insert into alerta (ID, DataHoraMedicao, TipoSensor, ValorMedicao, Limite, Descricao, Controlo, Extra)"
							+ " values (" + i++ + ", '" + dataHoraMedicao + "', '" + tipoSensor + "', "
							+ valorMedicao + ", " + limite + ", \"" + descricao + "\", " + controlo + ", " + null + ");");

			// ID DataHoraMedicao TipoSensor ValorMedicao Limite Descricao Controlo Extra
		} catch (Exception e) {
			System.out.println("Erro a escrever um alerta. " + e);
		}
	}
	
	public boolean verRondas() {
		rondas.clear();
		Date dataHoraMedicao = new Date(System.currentTimeMillis());
		Date datafinal = new Date(dataHoraMedicao.getTime() + LIMITE_RONDA * 6000);
		obterRondasPlaneadas();
		obterRondasExtras();
		for(Date dh : rondas) {
			if( !(dh.before(dataHoraMedicao) || dh.after(datafinal)))
				return true;
		}
		return false;
	}
	
	
	// Já funciona bué broooo
	public void obterRondasPlaneadas() {
		String SqlCommando = new String();
		ResultSet rs;
		try {
			// descobrir o dia da semana de "hoje"
			SQLstatement = SQLconn.createStatement();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date(System.currentTimeMillis());
			String dataFormatada = formatter.format(date);
			SqlCommando = "Select weekday (" + dataFormatada + ") as diaSemana;";
			SQLstatement = SQLconn.createStatement();
			rs = SQLstatement.executeQuery(SqlCommando);
			String nomeDoDia = null; // String a comparar com o dia retirado da tabela SQL
			while (rs.next()) {
				int diaSemana = rs.getInt("diaSemana");
				switch (diaSemana) {
				case 0:
					nomeDoDia = "segunda";
					break;
				case 1:
					nomeDoDia = "terca";
					break;
				case 2:
					nomeDoDia = "quarta";
					break;
				case 3:
					nomeDoDia = "quinta";
					break;
				case 4:
					nomeDoDia = "sexta";
					break;
				case 5:
					nomeDoDia = "sabado";
					break;
				case 6:
					nomeDoDia = "domingo";
					break;
				default:
					break;

				}
				
			}
			SqlCommando = "Select DiaSemana_HoraRonda_ as hora, DiaSemana_DiaSemana_ as dia from rondaplaneada;"; //Ir buscar a data da ronda planeada para comparar
			//Ver se ha ronda planeada no dia de semana de hoje.
			rs = SQLstatement.executeQuery(SqlCommando);
			while (rs.next()) {
				String diaDaSemana = rs.getString("dia"); //Variavel dia apanhada do SQL
				Time horas = rs.getTime("hora");
				if (diaDaSemana == nomeDoDia) { // apartir desta comparação para ver se o diaDaSemana retirado da tabela SQL é igual ao nomeDoDia descoberto em cima
					Date aux = Date.valueOf(formatter.format(new Date(System.currentTimeMillis())));
					Date datahora = new Date(aux.getTime()+horas.getTime());
					System.out.println("Adicionei " + datahora + " às rondas from planeadas");
					rondas.add(datahora);
					}
			}
		} catch (Exception e) {
			System.out.println("Erro a verificar se há ronda." + e);
		}
	}
	
	public void obterRondasExtras() {
		String SqlCommando = new String();
		ResultSet rs;
		SqlCommando = "Select DataHora from rondaextra;";
		try {
			SQLstatement = SQLconn.createStatement();
			rs = SQLstatement.executeQuery(SqlCommando);
			while (rs.next()) {
				Date datahora = new Date(rs.getTimestamp("DataHora").getTime());
				System.out.println("Adicionei " + datahora + " às rondas from extras");
				rondas.add(datahora);
			}
		} catch (Exception e) {
			System.out.println("Erro a verificar se há ronda. " + e);
		}
		
	}

	private void updateLimites() {
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
			while (rs.next())
				id = rs.getInt("MaxID");
		} catch (Exception e) {
			System.out.println("Erro a ler o ID Máximo. " + e);
		}
		SqlCommando = "Select LimiteTemperatura, LimiteHumidade, LimiteLuminosidade from sistema where Sistema_ID = "
				+ id + ";";
		try {
			SQLstatement = SQLconn.createStatement();
			rs = SQLstatement.executeQuery(SqlCommando);
			while (rs.next()) {
				maxTemp = rs.getDouble("LimiteTemperatura");
				maxHum = rs.getDouble("LimiteHumidade");
				maxLum = rs.getDouble("LimiteLuminosidade");
			}
		} catch (Exception e) {
			System.out.println("Erro a ler os limites máximos. " + e);
		}
		t.updateLimite(maxTemp);
		l.updateLimite(maxLum);
		h.updateLimite(maxHum);
	}
	
	private void startUpdates() {
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					updateLimites();
					obterRondasPlaneadas();
					try { // Vou dormir um dia :)
						Thread.sleep(8640000);
					} catch (InterruptedException e) {
						System.err.println("Alguém acordou o leitor");
						e.printStackTrace();
					}
				}
			}
		}).start();
	}


	public static void main(String[] args) throws InterruptedException {
		MongoToMySQL m = new MongoToMySQL();
		m.connectToMongo();
		m.connectToMySQL();
		m.readFromMongo();
		m.startUpdates();
	}

}
