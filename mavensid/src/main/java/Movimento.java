import java.util.LinkedList;

public class Movimento {
	LinkedList<Integer> valoresRecebidos = new LinkedList<Integer>();
	
	double variavel = 10;//quanto maior o valor da variavel mais rapido se vai alertar
	
	MongoToMySQL contact;
	
	public Movimento(MongoToMySQL contact) {
		this.contact=contact;
	}
	
		
	public void processar(int num) {//TODO Falta verificar se não há rondas e cenas em todos estes coisos
		if(num >= 1) {
			System.err.println("Alerta Movimentos para xuxu!!!");
			
			contact.writeAlertaToMySQL("MOV", num+"", 1+"", "Movimentos a acontecer", 0+"", ""); // Este vai ser a VERMELHO
		}
		valoresRecebidos.removeFirst();
		valoresRecebidos.addLast(num);
	}
}
