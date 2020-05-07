import java.util.LinkedList;

public class MongoToMySQL {
	
	public LinkedList<Medicao> medicoesSensores = new LinkedList<Medicao>();
	Temperaturas t = new Temperaturas();
	Humidade h = new Humidade();
	Luminosidade l = new Luminosidade();
	Movimento m = new Movimento();
	
	public void separarMedicoes() {
		for(Medicao ms : medicoesSensores) {
			t.processar(Double.parseDouble(ms.getMedicaoTemperatura()));
			h.processar(Double.parseDouble(ms.getMedicaoHumidade()));
			l.processar(Integer.parseInt(ms.getMedicaoTemperatura()));
//			m.processar(ms.getMedicaoTemperatura());

		}
	}
	
	
	

}
