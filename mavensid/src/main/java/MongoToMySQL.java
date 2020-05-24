import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.Properties;

import com.mongodb.*;

public class MongoToMySQL {

	public LinkedList<Medicao> medicoesSensores = new LinkedList<Medicao>();
	Temperaturas t = new Temperaturas();
	Humidade h = new Humidade();
	Luminosidade l = new Luminosidade();
	Movimento m = new Movimento();

	public void readFromMongo() {

		MongoClient mongoClient1 = new MongoClient(
				new MongoClientURI("mongodb://localhost:27017")); // ?

		DB db = mongoClient1.getDB("dadosSensores");
		DBCollection medicoes = db.getCollection("medicoesSensores"); // Coleção das Medições
		DBCursor cursor = medicoes.find();
		while (cursor.hasNext()) {
			DBObject read = cursor.next();
			System.out.println(read);
			/*
			 *  TODO Lidar com medições - enfiar os gajos no medicoesSensores
			 *  Dar parse ao gajo para o tornar uma cena que se enfie lá
			 *  
			 *  https://stackoverflow.com/questions/20901837/converting-dbobject-to-java-object-while-retrieve-values-from-mongodb/20902402
			 *  
			 */
			medicoesSensores.add((Medicao) read); // Não é assim
			medicoesSensores.pop();
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
	
	public static void main(String[] args) {
		new MongoToMySQL().readFromMongo();
	}

}
