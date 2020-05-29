import java.util.LinkedList;

public class Humidade {

	LinkedList<Double> valoresRecebidos = new LinkedList<Double>();
	LinkedList<Double> mediasAnteriores = new LinkedList<Double>();

	double limiteHumidade = 130;
	double variavel = 10; // quanto maior o valor da variavel mais rapido se vai alertar
	int contadorVermelho = 0;
	int contadorAmarelo = 0;
	private static final int ESPACAMENTO_ENTRE_ALERTAS = 10;

	MongoToMySQL contact;

	public Humidade(MongoToMySQL contact) {
		this.contact = contact;
	}

	private double calcularMediaAnterior() {
		double sum = 0;
		for (int i = 0; i != mediasAnteriores.size(); i++) {
			sum += valoresRecebidos.get(i);
		}
		System.out.println("media valores recebidos anterior:" + sum / mediasAnteriores.size());
		return sum / mediasAnteriores.size();
	}

	public void processar(double num) {
		double mediaAnterior = calcularMediaAnterior();
		if (num >= limiteHumidade && contadorVermelho == 0) {
			System.err.println("Alerta Vermelho - Muita húmido!!!");
			contact.writeAlertaToMySQL("HUM", num + "", limiteHumidade + "", "esta mega humido", 1 + "", "");
			contadorVermelho = ESPACAMENTO_ENTRE_ALERTAS;
		} else {
			if(contadorVermelho > 0) {
				contadorVermelho--;
			}
			if (mediasAnteriores.size() == 5) { // prever apenas se a lista tiver 5 elementos, não vale a pena antes disso
				double media5InstantesAntes = mediasAnteriores.poll();
				double calc = (mediaAnterior - media5InstantesAntes) * variavel + num;
				if (calc >= limiteHumidade && contadorAmarelo==0) {
					System.err.println("Alerta amarelo - vai ficar húmido!!!");
					// TODO Alerta da humidade
					contact.writeAlertaToMySQL("HUM", num + "", limiteHumidade + "", "Vai ficar muita húmido", 0 + "","");
					contadorAmarelo =  ESPACAMENTO_ENTRE_ALERTAS;
				}else {
					if (contadorAmarelo > 0) {
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
		this.limiteHumidade = limit;
	}

}
