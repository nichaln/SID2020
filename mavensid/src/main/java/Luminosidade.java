import java.util.LinkedList;
import java.util.Scanner;

public class Luminosidade {
	
	LinkedList<Double> valoresRecebidos = new LinkedList<Double>();
	LinkedList<Double> mediasAnteriores = new LinkedList<Double>();
	
	double limiteLuminosidade=1000;
	double variavel = 10;//quanto maior o valor da variavel mais rapido se vai alertar
	
	MongoToMySQL contact;
	
	public Luminosidade(MongoToMySQL contact) {
		this.contact=contact;
	}
	
	private double calcularMediaAnterior() {
		double sum=0;
		for(int i=0;i<mediasAnteriores.size();i++) {
			sum+=valoresRecebidos.get(i);
		}
		System.out.println("media valores recebidos anterior:" + sum/mediasAnteriores.size());
		return sum/mediasAnteriores.size();
	}
	
	public void processar(double num) {//TODO Falta verificar se não há rondas e cenas em todos estes coisos
		double mediaAnterior = calcularMediaAnterior();
		if(num >= limiteLuminosidade) {
			System.err.println("Alerta Vermelho - Bué da luz!!!");
			contact.writeAlertaToMySQL("CELL", num+"", limiteLuminosidade+"", " Bué da luz", 0+"", ""); // Este vai ser a VERMELHO
		}else {
			double media5InstantesAntes = mediasAnteriores.poll();
			double calc = (mediaAnterior - media5InstantesAntes) * variavel + num;
			if(calc >= limiteLuminosidade) {
				System.err.println("Alerta Amarelo - Vai ficar bué luz!!!");
				contact.writeAlertaToMySQL("CELL", num+"", limiteLuminosidade+"", "Vai ficar bué luz", 0+"", ""); // Este vai ser a AMARELO
			}
		}
		valoresRecebidos.removeFirst();
		valoresRecebidos.addLast(num);
		mediasAnteriores.addLast(mediaAnterior);
	}
	
	public void updateLimite(double limit) {
		this.limiteLuminosidade = limit;
	}
		
	
	
	private void start() {
		Scanner in = new Scanner(System.in);
		for(int i=0;i<5;i++) {
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
		Luminosidade t = new Luminosidade();
		t.start();
	}*/

}
