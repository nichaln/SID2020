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
		String[] aux;
		String timestamp;
		Medicao m = new Medicao();
		double leitura;
		int i = 0;
		for (String s : vetor) {
			aux = s.split(":");
			aux[1].replaceAll("\"", "");
			if (i == 0) {
				verifyTmp(aux[1], 't');
//				m.setMedicaoTemperatura(aux[1]);
			}

			i++;

		}

	}

	private void verifyTmp(String aux, char key,) {
		switch (key) {
		case 't':
			try {
				double leitura = Double.parseDouble(aux);
			} catch (NumberFormatException e) {//escreve nos erros
				LinkedList<MedicaoErroValores> historico = new LinkedList<MedicaoErroValores>();
				for(Medicao m: medicoesAnteriores) {
					historico.add(new MedicaoErroValores(m.getMedicaoTemperatura(),m.getDate()));
				}
				MedicaoErro me = new MedicaoErro(aux,historico);
				
				return;
			}
			if() {
				
			}
//				
		case 'h':
		case 'c':
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
