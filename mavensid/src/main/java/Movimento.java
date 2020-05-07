import java.util.LinkedList;

public class Movimento {
	LinkedList<Integer> valoresRecebidos = new LinkedList<Integer>();
	
	double variavel = 10;//quanto maior o valor da variavel mais rapido se vai alertar
	
		
	public void processar(int num) {
		if(num >= 1) {
			System.err.println("Alerta Movimentos para xuxu!!!");
		}
		valoresRecebidos.removeFirst();
		valoresRecebidos.addLast(num);
	}
}
