import java.util.LinkedList;
import java.util.Scanner;

public class Temperaturas {
	LinkedList<Double> valoresRecebidos = new LinkedList<Double>();
	LinkedList<Double> mediasAnteriores = new LinkedList<Double>();
	
	double variavel = 10;//quanto maior o valor da variavel mais rapido se vai alertar
	double limiteTempSup=45;
	double limiteTempInf=0;/**ACABAR ISTO COM UMA FORMULA MANHOSA**/
	int contador=0;
	
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
		double media3InstantesAntes = mediasAnteriores.poll();
		double calc = (mediaAnterior - media3InstantesAntes) * variavel + num;
		double calcneg = (media3InstantesAntes-mediaAnterior) * variavel + num;
		if(calc >= limiteTempSup) {
			if(contador==0) {
				System.err.println("Alerta Temperatura a aumentar!!!");
				contador=8;
			}
			// TODO Alerta Temp alta
		}
		if(calcneg <= limiteTempInf) {
			System.err.println("Alerta Temperatura a diminuir!!!");
			
			// TODO Alerta Temp baixa
		}
		if(contador>0)
			contador--;
		valoresRecebidos.removeFirst();
		valoresRecebidos.addLast(num);
		mediasAnteriores.addLast(mediaAnterior);
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
	
	public static void main(String[] args) {
		Temperaturas t = new Temperaturas();
		t.start();
	}
}
