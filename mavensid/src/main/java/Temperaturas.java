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
		double mediaAnterior = calcularMediaAnterior();
		double media3InstantesAntes = mediasAnteriores.poll();
		
		/*
		 * Aqui vemos para o quente 
		 */
		if(num >= limiteTempSup) {
			System.err.println("Alerta HOT HOT HOT!!!");
			contact.writeAlertaToMySQL("TEM", num+"", limiteTempSup+"", "Santarém", 0+"", ""); // Este vai ser a VERMELHO
		} else { //prever
			double calc = (mediaAnterior - media3InstantesAntes) * variavel + num;
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
		if (num <= limiteTempSup) {
			System.err.println("Alerta COLD COLD COLD!!!");
			contact.writeAlertaToMySQL("TEM", num+"", limiteTempInf+"", "Fresquinho", 0+"", ""); // Este vai ser a VERMELHO
		} else {
			double calcneg = (media3InstantesAntes - mediaAnterior) * variavel + num;
			if (calcneg <= limiteTempInf) {
				System.err.println("Alerta Temperatura a diminuir!!!");
				// TODO Alerta Temp baixa
				contact.writeAlertaToMySQL("TEM", num + "", limiteTempInf + "", "Temperatura a diminuir", 0 + "", "");// Este vai ser a AMARELO
			}
		}
		
		if(contador>0)
			contador--;
		valoresRecebidos.removeFirst();
		valoresRecebidos.addLast(num);
		mediasAnteriores.addLast(mediaAnterior);
	}
	
	public void updateLimite(double limit) {
		this.limiteTempSup = limit;
		this.limiteTempInf = limit - limit;
	}
		
	private void start() {
		Scanner in = new Scanner(System.in);
		for(int i=0;i<3;i++) {
			System.out.println("Escreva a medicao:\n");
			double a = in.nextDouble();
			System.out.println("Medicao lida:"+  a + "\n");
			valoresRecebidos.addLast(a);
			System.out.println("Escreva a media:\n");
			a = in.nextDouble();
			System.out.println("Media lida:"+ a + "\n");
			mediasAnteriores.addLast(a);
		}
		while (true) {
			System.out.println("Proxima medicao sensor\n");
			double num = in.nextDouble();
			processar(num);
		}
	}
	
	/*public static void main(String[] args) {
		Temperaturas t = new Temperaturas();
		t.start();
	}*/
}
