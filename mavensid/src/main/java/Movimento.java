import java.util.LinkedList;

public class Movimento {
	LinkedList<Double> valoresRecebidos = new LinkedList<Double>();
	
	double variavel = 10;//quanto maior o valor da variavel mais rapido se vai alertar
	int contador = 0; //contador para nao tar sempre a enviar alertas
	private static final int ESPACAMENTO_ENTRE_ALERTAS=10;
	MongoToMySQL contact;
	
	public Movimento(MongoToMySQL contact) {
		this.contact=contact;
	}
	
		
	public void processar(Double num) {//TODO Falta verificar se não há rondas e cenas em todos estes coisos
		
		if(num >= 1 && valoresRecebidos.removeLast() >= 1 && contador == 0) { // Assim só mete um erro se houverem dois "movimentos" de seguida
			//FIXME temos que mudar para fazer isto no cloud to mongo, para ficar mesmo como "erro"
			System.err.println("Alerta Movimentos para xuxu!!!");
			contact.writeAlertaToMySQL("MOV", num+"", 1+"", "Movimentos a acontecer", 1+"", "");
			contador=ESPACAMENTO_ENTRE_ALERTAS;
		}else {
			if(contador>0)
				contador--;
		}
		if(valoresRecebidos.size() == 5)
			valoresRecebidos.removeFirst();
		valoresRecebidos.addLast(num);
	}
}
