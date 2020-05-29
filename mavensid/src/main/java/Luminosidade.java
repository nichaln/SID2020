import java.util.LinkedList;

public class Luminosidade {

	LinkedList<Double> valoresRecebidos = new LinkedList<Double>();
	LinkedList<Double> mediasAnteriores = new LinkedList<Double>();

	double limiteLuminosidade = 1000;
	double variavel = 10;// quanto maior o valor da variavel mais rapido se vai alertar
	int contadorVermelho = 0;
	int contadorAmarelo = 0;

	private static final int ESPACAMENTO_ENTRE_ALERTAS = 10;

	MongoToMySQL contact;

	public Luminosidade(MongoToMySQL contact) {
		this.contact = contact;
	}

	private double calcularMediaAnterior() {
		double sum = 0;
		for (int i = 0; i != mediasAnteriores.size(); i++) {
			sum += valoresRecebidos.get(i);
		}
		// System.out.println("media valores recebidos anterior:" + sum /
		// mediasAnteriores.size());
		return sum / mediasAnteriores.size();
	}

	public void processar(double num) { // TODO Falta verificar se não há rondas e cenas em todos estes coisos
		double mediaAnterior = calcularMediaAnterior();
		if (num >= limiteLuminosidade && contadorVermelho == 0) {
			System.err.println("Alerta Vermelho - Bué da luz!!!");
			contact.writeAlertaToMySQL("CEL", num + "", limiteLuminosidade + "", " Bué da luz", 1 + "", ""); // Este vai ser a vermelho
			contadorVermelho = ESPACAMENTO_ENTRE_ALERTAS;
		} else {
			if(contadorVermelho > 0) {
				contadorVermelho--;
			}
			if (mediasAnteriores.size() == 5) { // prever apenas se a lista de medias tiver 5 elementos, não vale apena antes disso
				double media5InstantesAntes = mediasAnteriores.poll();
				double calc = (mediaAnterior - media5InstantesAntes) * variavel + num;
				if (calc >= limiteLuminosidade && contadorAmarelo==0) {
					System.err.println("Alerta Amarelo - Vai ficar bué luz!!!");
					contact.writeAlertaToMySQL("CEL", num + "", limiteLuminosidade + "", "Vai ficar bué luz", 0 + "",""); // Este vai ser a AMARELO
					contadorAmarelo = ESPACAMENTO_ENTRE_ALERTAS;
				}
				else {
					if(contadorAmarelo > 0) {
						contadorAmarelo--;
					}
				}
			}
		}
		if (valoresRecebidos.size() == 5)
			valoresRecebidos.removeFirst();
		valoresRecebidos.addLast(num);
		if (mediasAnteriores.size() == 5)
			mediasAnteriores.removeFirst();
		mediasAnteriores.addLast(mediaAnterior);
	}

	public void updateLimite(double limit) {
		this.limiteLuminosidade = limit;
	}
}
