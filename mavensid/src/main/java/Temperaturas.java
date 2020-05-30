import java.util.LinkedList;

public class Temperaturas {
	LinkedList<Double> valoresRecebidos = new LinkedList<Double>();
	LinkedList<Double> mediasAnteriores = new LinkedList<Double>();

	double variavel = 10; // quanto maior o valor da variavel mais rapido se vai alertar
	double limiteTempSup = 45;
	double limiteTempInf = 0;
	/** ACABAR ISTO COM UMA FORMULA MANHOSA **/
	int contadorVermelho = 0;
	int contadorAmarelo = 0;
	private static final int ESPACAMENTO_ENTRE_ALERTAS=10;
	MongoToMySQL contact;

	public Temperaturas(MongoToMySQL contact) {
		this.contact = contact;
	}

	private double calcularMediaAnterior() {
		double sum = 0;
		for (int i = 0; i != mediasAnteriores.size(); i++) {
			sum += valoresRecebidos.get(i);
		}
		// System.out.println("media valores recebidos anterior:"
		// +sum/mediasAnteriores.size());
		return sum / mediasAnteriores.size();
	}

	public void processar(double num) {

		double mediaAnterior = calcularMediaAnterior(); // Media que vai entrar
		double media3InstantesAntes = Double.NaN; // É preciso inicializar a NaN para termos uma condição em baixo para
													// não prever antes de ter 3 elementos na lista

		if (mediasAnteriores.size() == 3) 
			media3InstantesAntes = mediasAnteriores.poll(); // Media que vai sair da lista, isto só acontece depois de 3
		/*
		 * Aqui vemos para o quente
		 */
		// System.out.println("vou comparar " + num + " com " + limiteTempSup);
		if (num >= limiteTempSup && contadorVermelho == 0) {
			System.err.println("Alerta HOT HOT HOT!!!");
			contact.writeAlertaToMySQL("TEM", num + "", limiteTempSup + "", "Temperatura acima do limite", 1 + "", ""); // Este vai ser a
																										// vermelho
			contadorVermelho = ESPACAMENTO_ENTRE_ALERTAS;
		} else {
			if (contadorVermelho > 0) {
				contadorVermelho--;
			}
			if (!Double.isNaN(media3InstantesAntes)) { // prever aumento
				double calc = (mediaAnterior - media3InstantesAntes) * variavel + num;
				if (calc >= limiteTempSup && contadorAmarelo == 0 && num < limiteTempSup) {
					System.err.println("Alerta Temperatura a aumentar!!!");
					contadorAmarelo = ESPACAMENTO_ENTRE_ALERTAS;
					// TODO Alerta Temp alta
					contact.writeAlertaToMySQL("TEM", num + "", limiteTempSup + "", "Temperatura a aumentar", 0 + "",
							""); // Este vai ser a AMARELO
				} else {
					if (contadorAmarelo > 0) {
						contadorAmarelo--;
					}
				}
			}
		}
		/*
		 * Aqui vemos para o frio
		 */
		if (num <= limiteTempInf && contadorVermelho == 0) {
			System.err.println("Alerta COLD COLD COLD!!!");
			contact.writeAlertaToMySQL("TEM", num + "", limiteTempInf + "", "Temperatura demasiado baixa", 1 + "", ""); // Este vai ser a
																										// vermelho
			contadorVermelho = ESPACAMENTO_ENTRE_ALERTAS;
		} else {
			if(contadorVermelho > 0) {
				contadorVermelho--;
			}
			if (!Double.isNaN(media3InstantesAntes)) {
				double calcneg = (media3InstantesAntes - mediaAnterior) * variavel + num;
				if (calcneg <= limiteTempInf && contadorAmarelo == 0) {
					System.err.println("Alerta Temperatura a diminuir!!!");
					// TODO Alerta Temp baixa
					contact.writeAlertaToMySQL("TEM", num + "", limiteTempInf + "", "Temperatura a diminuir", 0 + "","");// Este vai ser a amarelo
					contadorAmarelo = ESPACAMENTO_ENTRE_ALERTAS;
				} else {
					if (contadorAmarelo > 0) {
						contadorAmarelo--;
					}
				}
			}
		}
		if (valoresRecebidos.size() == 3)
			valoresRecebidos.removeFirst();
		valoresRecebidos.addLast(num);
		if (mediasAnteriores.size() == 3)
			mediasAnteriores.removeFirst();
		mediasAnteriores.addLast(mediaAnterior);

	}

	public void updateLimite(double limit) {
		this.limiteTempSup = limit;
		this.limiteTempInf = limit - limit;
		System.out.println("mudei o limite para " + limiteTempSup);
	}
}
