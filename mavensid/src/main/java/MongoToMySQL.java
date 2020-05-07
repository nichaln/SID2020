import java.util.LinkedList;

public class MongoToMySQL {
	
	public LinkedList<Medicao> medicoesSensores = new LinkedList<Medicao>();
	Temperaturas t = new Temperaturas();
	Humidade h = new Humidade();
	Luminosidade l = new Luminosidade();
	Movimento m = new Movimento();
	
	public void separarMedicoes() {
		for(Medicao ms : medicoesSensores) {
			t.processar(ms.getMedicaoTemperatura());
			h.processar(ms.getMedicaoTemperatura());
			l.processar(ms.getMedicaoTemperatura());
//			m.processar(ms.getMedicaoTemperatura());

		}
	}
	
	
	

}
