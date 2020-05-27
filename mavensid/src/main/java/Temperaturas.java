import java.util.LinkedList;
import java.util.Scanner;

public class Temperaturas {
	LinkedList<Double> valoresRecebidos = new LinkedList<Double>();
	LinkedList<Double> mediasAnteriores = new LinkedList<Double>();
	
	double variavel = 10; //quanto maior o valor da variavel mais rapido se vai alertar
	double limiteTempSup = 45;
	double limiteTempInf = 0; /**ACABAR ISTO COM UMA FORMULA MANHOSA**/
	int contador = 0;
	MongoToMySQL contact;
	
	public Temperaturas(MongoToMySQL contact) {
		this.contact=contact;
	}
	
	private double calcularMediaAnterior() {
		double sum=0;
		for(int i=0;i<mediasAnteriores.size();i++) {
			sum+=valoresRecebidos.get(i);
		}
		//System.out.println("media valores recebidos anterior:" + sum/mediasAnteriores.size());
		return sum/mediasAnteriores.size();
	}
	
	public void processar(double num) {
		
		double mediaAnterior = calcularMediaAnterior(); //Media que vai entrar
		double media5InstantesAntes = Double.NaN; //É preciso inicializar a NaN para termos uma condição em baixo para não prever
												  // antes de ter 5 elementos na lista
		
		 if (mediasAnteriores.size() == 5) {
			 media5InstantesAntes = mediasAnteriores.poll(); //Media que vai sair da lista, isto só acontece depois de 5 ciclos 
		 }
		/*
		 * Aqui vemos para o quente 
		 */
		System.out.println("vou comparar "+num+" com "+limiteTempSup);
		if(num >= limiteTempSup) {
			System.err.println("Alerta HOT HOT HOT!!!");
			contact.writeAlertaToMySQL("TEM", num+"", limiteTempSup+"", "Santarém", 0+"", ""); // Este vai ser a VERMELHO
		} else if (!Double.isNaN(media5InstantesAntes)){ //prever
			double calc = (mediaAnterior - media5InstantesAntes) * variavel + num;
			if(calc >= limiteTempSup) {
				if(contador==0) {
					System.err.println("Alerta Temperatura a aumentar!!!");
					contador=8;
					// TODO Alerta Temp alta
				contact.writeAlertaToMySQL("TEM", num+"", limiteTempSup+"", "Temperatura a aumentar", 0+"", ""); // Este vai ser a AMARELO
				}
				
			}
		}
		/*
		 * Aqui vemos para o frio 
		 */
		if (num <= limiteTempInf) {
			System.err.println("Alerta COLD COLD COLD!!!");
			contact.writeAlertaToMySQL("TEM", num+"", limiteTempInf+"", "Fresquinho", 0+"", ""); // Este vai ser a VERMELHO
		} else if (!Double.isNaN(media5InstantesAntes)) {
			double calcneg = (media5InstantesAntes - mediaAnterior) * variavel + num;
			if (calcneg <= limiteTempInf) {
				System.err.println("Alerta Temperatura a diminuir!!!");
				// TODO Alerta Temp baixa
				contact.writeAlertaToMySQL("TEM", num + "", limiteTempInf + "", "Temperatura a diminuir", 0 + "", "");// Este vai ser a AMARELO
			}
		}
		
		if (contador > 0)
			contador--;
		valoresRecebidos.removeFirst();
		valoresRecebidos.addLast(num);
		mediasAnteriores.addLast(mediaAnterior);
		
	}
	
	public void updateLimite(double limit) {
		this.limiteTempSup = limit;
		this.limiteTempInf = limit - limit;
		System.out.println("mudei o limite para "+limiteTempSup);
	}
}
