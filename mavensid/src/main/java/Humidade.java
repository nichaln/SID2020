import java.util.LinkedList;
import java.util.Scanner;

public class Humidade {
	
	LinkedList<Double> valoresRecebidos = new LinkedList<Double>();
	LinkedList<Double> mediasAnteriores = new LinkedList<Double>();
	
	double limiteHumidade=130;
	double variavel = 10;//quanto maior o valor da variavel mais rapido se vai alertar
	
	private double calcularMediaAnterior() {
		double sum=0;
		for(int i=0;i<mediasAnteriores.size();i++) {
			sum+=valoresRecebidos.get(i);
		}
		System.out.println("media valores recebidos anterior:" + sum/mediasAnteriores.size());
		return sum/mediasAnteriores.size();
	}
	
	public void processar(double num) {
		double mediaAnterior = calcularMediaAnterior();
		double media5InstantesAntes = mediasAnteriores.poll();
		double calc = (mediaAnterior - media5InstantesAntes) * variavel + num;
		if(calc >= limiteHumidade) {
			System.err.println("Alerta Vermelho - Muita húmido!!!");
			
			// TODO Alerta da humidade
		}
		valoresRecebidos.removeFirst();
		valoresRecebidos.addLast(num);
		mediasAnteriores.addLast(mediaAnterior);
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
	
	public static void main(String[] args) {
		Humidade t = new Humidade();
		t.start();
	}

}
