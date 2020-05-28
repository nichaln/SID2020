import java.util.LinkedList;

public class Movimento {
	LinkedList<Double> valoresRecebidos = new LinkedList<Double>();
	
	double variavel = 10;//quanto maior o valor da variavel mais rapido se vai alertar
	
	MongoToMySQL contact;
	
	public Movimento(MongoToMySQL contact) {
		this.contact=contact;
	}
	
		
	public void processar(Double num) {//TODO Falta verificar se não há rondas e cenas em todos estes coisos
		
		if(num >= 1 && valoresRecebidos.removeLast() >= 1) { // Assim só mete um erro se houverem dois "movimentos" de seguida
			//FIXME temos que mudar para fazer isto no cloud to mongo, para ficar mesmo como "erro"
			System.err.println("Alerta Movimentos para xuxu!!!");
			
			contact.writeAlertaToMySQL("MOV", num+"", 1+"", "Movimentos a acontecer", 1+"", "");
		}
		if(valoresRecebidos.size() == 5)
			valoresRecebidos.removeFirst();
		valoresRecebidos.addLast(num);
	}
}
