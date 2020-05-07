import java.util.LinkedList;

public class SensorToMongo {

	private LinkedList<Medicao> medicoesAnteriores = new LinkedList<Medicao>();
	private LinkedList<MedicaoErro> errosTemperatura = new LinkedList<MedicaoErro>();
	private LinkedList<MedicaoErro> errosHumidade = new LinkedList<MedicaoErro>();
	private LinkedList<MedicaoErro> errosLuminosidade = new LinkedList<MedicaoErro>();
	private LinkedList<MedicaoErro> errosMovimento = new LinkedList<MedicaoErro>();

	private LinkedList<Double> mediasTemperatura = new LinkedList<Double>();
	private LinkedList<Double> mediasHumidade = new LinkedList<Double>();
	private LinkedList<Double> mediasLuminosidade = new LinkedList<Double>();

	public void receberMensagem(String mensagem) {
		String[] vetor = mensagem.split(",");
		String[] parsed = new String[vetor.length];
		for (int i = 0; i != vetor.length; i++) {
			parsed[i] = vetor[i].split(":")[1].replaceAll("\"", "");
		}
		String timestamp = parsed[3]; // TODO valor correspondente do timestamp
		Medicao m = new Medicao();
		double leitura;
		int i = 0;
		for (String s : parsed) {
			if (i == 0) {
				if(verify(s, 't', timestamp))
					m.setMedicaoTemperatura(Double.parseDouble(s));
			}
			if (i == 1) {
				if(verify(s, 'h', timestamp))
					m.setMedicaoTemperatura(Double.parseDouble(s));
			}
			// TODO Parse data
			if (i == 3) {
				if(verify(s, 'c', timestamp))
					m.setMedicaoTemperatura(Double.parseDouble(s));
			}
			/*if (i == 3) {
				if(verify(s, 't', timestamp))
					m.setMedicaoTemperatura(Double.parseDouble(s));
			}*/

			i++;

		}

	}

	private boolean verify(String aux, char key, String timestamp) {
		switch (key) {
		case 't':
			double leitura;
			try {
				leitura = Double.parseDouble(aux);
			} catch (NumberFormatException e) {// escreve nos erros
				MedicaoErro me = new MedicaoErro(aux, timestamp, history());

				/*
				 * TODO mandar esta me para o spot de guardar
				 */

				return false;
			}
			if (checkValues(leitura)) {// espetáculo regista aí
				return true;
			} else {// fora da graça de deus
				MedicaoErro me = new MedicaoErro(aux, timestamp, history());
				
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

	private LinkedList<MedicaoErroValores> history() {
		LinkedList<MedicaoErroValores> historico = new LinkedList<MedicaoErroValores>();
		for (Medicao m : medicoesAnteriores) {
			historico.add(new MedicaoErroValores(m.getMedicaoTemperatura(), m.getDate()));
		}
		return historico;
	}

	private boolean checkValues(Double leitura) {
		if (leitura > 20564) { // Comparação do demónio
			return true;
		} else {
			return false;
		}

	}

	public static void main(String[] args) {
		String a = "a";
		try {
			System.out.println("" + Double.parseDouble(a));
		} catch (NumberFormatException e) {

		}
	}

}
