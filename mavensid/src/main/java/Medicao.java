public class Medicao {

	private String medicaoTemperatura;
	private String medicaoHumidade;
	private String medicaoLuminosidade;
	private int medicaoMovimento;
	private String date;

	public String getMedicaoTemperatura() {
		return medicaoTemperatura;
	}

	public String getMedicaoHumidade() {
		return medicaoHumidade;
	}

	public String getMedicaoLuminosidade() {
		return medicaoLuminosidade;
	}

	public int getMedicaoMovimento() {
		return medicaoMovimento;
	}

	public String getDate() {
		return date;
	}

	public void setMedicaoTemperatura(String medicaoTemperatura) {
		this.medicaoTemperatura = medicaoTemperatura;
	}

	public void setMedicaoHumidade(String medicaoHumidade) {
		this.medicaoHumidade = medicaoHumidade;
	}

	public void setMedicaoLuminosidade(String medicaoLuminosidade) {
		this.medicaoLuminosidade = medicaoLuminosidade;
	}

	public void setMedicaoMovimento(int medicaoMovimento) {
		this.medicaoMovimento = medicaoMovimento;
	}

	public void setDate(String date) {
		this.date = date;
	}

}
