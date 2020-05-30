import java.sql.Date;
import java.text.SimpleDateFormat;

public class Movimento {
	//LinkedList<Double> valoresRecebidos = new LinkedList<Double>();
	
	double variavel = 10;//quanto maior o valor da variavel mais rapido se vai alertar
	int contador = 0; //contador para nao tar sempre a enviar alertas
	private static final int ESPACAMENTO_ENTRE_ALERTAS=10;
	MongoToMySQL contact;
	
	public Movimento(MongoToMySQL contact) {
		this.contact=contact;
	}
	
		
	public void processar(Double num) {//TODO Falta verificar se não há rondas e cenas em todos estes coisos
		
		Date dataHoraMedicao = new Date(System.currentTimeMillis());
		
		if(num >= 1 && contador == 0 && !contact.verRondas(dataHoraMedicao)) {//ahhhhhhh não temos datahora aqui, será que já temos? 
			System.err.println("Alerta Movimentos para xuxu!!!");
			contact.writeAlertaToMySQL("MOV", num+"", 1+"", "Movimentos a acontecer", 1+"", "");
			contador=ESPACAMENTO_ENTRE_ALERTAS;
		}else {
			if(contador>0)
				contador--;
		}
		/*if(valoresRecebidos.size() == 3)
			valoresRecebidos.removeFirst();
		valoresRecebidos.addLast(num);*/
	}
}
