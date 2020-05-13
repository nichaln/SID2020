import java.util.LinkedList;

public class SensorToMongo { // este gajo é o primeiro java!

	private LinkedList<Medicao> medicoesAnteriores = new LinkedList<Medicao>(); //leituras todas mesmo
	
	private double ultimaMedicaoTemp=Double.POSITIVE_INFINITY;
	private double ultimaMedicaoHum=Double.POSITIVE_INFINITY;
	private double ultimaMedicaoLum=Double.POSITIVE_INFINITY;
	
	public SensorToMongo() {
		for(int i =0;i<5;i++) {
			medicoesAnteriores.add(new Medicao());
		}
	}
	
	public void receberMensagem(String mensagem) {
		String[] vetor = mensagem.split(",");
		String[] parsed = new String[vetor.length];
		for (int i = 0; i != vetor.length; i++) {
			parsed[i] = vetor[i].split("\":\"")[1].replaceAll("\"", "");
		}
		String [] auxTime = parsed[2].split("/");
		String timestamp = auxTime[2]+"-"+auxTime[1]+"-"+auxTime[0]+" "+parsed[3]; // TODO valor correspondente do timestamp
		System.out.println(timestamp);
		
		medicoesAnteriores.pop();
		Medicao m = new Medicao();
		m.setDate(timestamp);
		
		int i = 0;
		for (String s : parsed) {
			if (i == 0) {//temperatura
				if(verify(s, 't', timestamp)) {// se correu tudo bem, escreve na medicoessensores e atualiza o vetor
					m.setMedicaoTemperatura(""+Double.parseDouble(s));
					//TODO Escrever na coleçao mongoDB
					
				}else { //ser string ou valor elevado
					m.setMedicaoTemperatura(s);
				}
			}
			if (i == 1) {//humidade
				if(verify(s, 'h', timestamp)) {
					m.setMedicaoHumidade(""+Double.parseDouble(s));
					//TODO escrever na colecao mongoDB
				}else {
					m.setMedicaoHumidade(s);
				}
					
			}
			// TODO Parse data
			if (i == 4) {//luminosidade
				if(verify(s, 'c', timestamp)) {
					m.setMedicaoLuminosidade(""+Integer.parseInt(s));
					//TODO escrever na colecao mongoDB
				}else {
					m.setMedicaoLuminosidade(s);
				}
					
			}
			/*if (i == 3) {
				if(verify(s, 't', timestamp))
					m.setMedicaoTemperatura(Double.parseDouble(s));
			}*/

			i++;

		}
		medicoesAnteriores.add(m);
		for(Medicao x :medicoesAnteriores)
			System.out.println(x.getDate()+" || "+x.getMedicaoTemperatura()+" || "+x.getMedicaoHumidade()+" || "+x.getMedicaoLuminosidade());

	}

	private boolean verify(String aux, char key, String timestamp) {
		switch (key) {
		case 't':
			double leitura;
			try {
				leitura = Double.parseDouble(aux);
			} catch (NumberFormatException e) {// escreve nos erros
				/*ESCREVER A CENA NA COLEÇAO ERROSMEDICOESSENSORES DO MONGODB*/

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

	private LinkedList<MedicaoErroValores> historyTemperatura() {
		LinkedList<MedicaoErroValores> historico = new LinkedList<MedicaoErroValores>();
		for (Medicao m : medicoesAnteriores) {
//			historico.add(new MedicaoErroValores(m.getMedicaoTemperatura(), m.getDate()));
		}
		return historico;
	}
	
	private LinkedList<MedicaoErroValores> historyHumidade() {
		LinkedList<MedicaoErroValores> historico = new LinkedList<MedicaoErroValores>();
		for (Medicao m : medicoesAnteriores) {
//			historico.add(new MedicaoErroValores(m.getMedicaoHumidade(), m.getDate()));
		}
		return historico;
	}
	
	private LinkedList<MedicaoErroValores> historyLuminosidade() {
		LinkedList<MedicaoErroValores> historico = new LinkedList<MedicaoErroValores>();
		for (Medicao m : medicoesAnteriores) {
//			historico.add(new MedicaoErroValores(m.getMedicaoLuminosidade(), m.getDate()));
		}
		return historico;
	}

	private boolean checkValidValueTemperatura(Double leitura) {
		if(leitura-ultimaMedicaoTemp > 5) {// este 5 tem de ser falado
			// aconteceu um erro de leitura elevada
			return false;
		}
		else {
			return true;
		}
	}

	public static void main(String[] args) {
		LinkedList<Integer> aa = new LinkedList<Integer>();
		System.out.println(aa);
		aa.add(1);
		System.out.println(aa);
		aa.add(2);
		System.out.println(aa);
		int a =aa.pop();
		System.out.println(aa);
		
	}

}
