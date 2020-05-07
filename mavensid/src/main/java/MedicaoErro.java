import java.util.LinkedList;

public class MedicaoErro {
	
	private String leitura;
	private String timestamp;
	private LinkedList<MedicaoErroValores> leiturasAnteriores= new LinkedList<MedicaoErroValores>();
	
	public MedicaoErro() {
		super();
		this.leitura = null;
		this.timestamp = null;
		this.leiturasAnteriores = null;
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
