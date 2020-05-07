
public class MedicaoErroValores {
	
	private double valorLeitura;
	private String timestamp;
	
	public MedicaoErroValores(double valorLeitura, String timestamp) {
		super();
		this.valorLeitura = valorLeitura;
		this.timestamp = timestamp;
	}

	public void setValorLeitura(double valorLeitura) {
		this.valorLeitura = valorLeitura;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public double getValorLeitura() {
		return valorLeitura;
	}

	public String getTimestamp() {
		return timestamp;
	}
}
