import java.util.LinkedList;

public class MedicaoErro {
	
	private String leitura;
	private String timestamp;
	private LinkedList<MedicaoErroValores> leiturasAnteriores= new LinkedList<MedicaoErroValores>();
	
	public MedicaoErro(String leitura, String timestamp, LinkedList<MedicaoErroValores> leiturasAnteriores) {
		super();
		this.leitura = leitura;
		this.timestamp = timestamp;
		this.leiturasAnteriores = leiturasAnteriores;
	}

	public String getLeitura() {
		return leitura;
	}

	public void setLeitura(String leitura) {
		this.leitura = leitura;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public LinkedList<MedicaoErroValores> getLeiturasAnteriores() {
		return leiturasAnteriores;
	}

	public void setLeiturasAnteriores(LinkedList<MedicaoErroValores> leiturasAnteriores) {
		this.leiturasAnteriores = leiturasAnteriores;
	}
	
	
	

}
