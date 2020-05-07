import java.util.LinkedList;
import java.util.Scanner;

public class MainTeste {
	LinkedList<Double> temperaturas_2s = new LinkedList<Double>();
	LinkedList<Double> temperaturas_1m = new LinkedList<Double>();
	int temperature;
	int limSup = 5;
	int limInf = 2;

	private double calcAverage(LinkedList<Double> temperaturas_2s2) {
		double sum = 0;
		for (int i = 0; i < temperaturas_2s2.size(); i++) {
			sum = sum + temperaturas_2s2.get(i);
		}
		System.out.println("A média é de :" + sum/temperaturas_2s2.size());
		return sum / temperaturas_2s2.size();
	}

	private boolean aux = false;

	private void calcularAviso_2s(double newValue) {
		if (newValue - calcAverage(temperaturas_2s) < 5 && newValue - calcAverage(temperaturas_2s) > 2 && !aux) {
			aux = true;
			temperaturas_2s.pop();
			temperaturas_2s.add(newValue);
			return;
		}
		if (newValue - calcAverage(temperaturas_2s) < 5 && newValue - calcAverage(temperaturas_2s) > 2 && aux) {
			temperaturas_2s.pop();
			temperaturas_2s.add(newValue);
			sendWarning();
			aux = false;
			printWarning();
		}
		if (newValue - calcAverage(temperaturas_2s) < 2 && aux) {
			aux = false;
			temperaturas_2s.pop();
			temperaturas_2s.add(newValue);
		}

	}

	private void printWarning() {
		System.out.println("Something is happening...");
	}

	private void sendWarning() {
		System.out.println("ALERTA ENVIADO!!!!!");
	}

	private void start() {
		Scanner in = new Scanner(System.in);
		for(int i=0;i<5;i++) {
			System.out.println("Escreva um numero:\n");
			temperaturas_2s.add(in.nextDouble());
		}
		while (true) {
			System.out.println("Proxima medicao sensor\n");
			double num = in.nextDouble();
			calcularAviso_2s(num);
		}
		
	}

	public static void main(String[] args) {
		MainTeste m = new MainTeste();
		m.start();
	}

}
