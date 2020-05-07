public class Medicao {

	private double medicaoTemperatura;
	private double medicaoHumidade;
	private int medicaoLuminosidade;
	private int medicaoMovimento;
	private String date;

	public double getMedicaoTemperatura() {
		return medicaoTemperatura;
	}

	public double getMedicaoHumidade() {
		return medicaoHumidade;
	}

	public int getMedicaoLuminosidade() {
		return medicaoLuminosidade;
	}

	public int getMedicaoMovimento() {
		return medicaoMovimento;
	}

	public String getDate() {
		return date;
	}

	public void setMedicaoTemperatura(double medicaoTemperatura) {
		this.medicaoTemperatura = medicaoTemperatura;
	}

	public void setMedicaoHumidade(double medicaoHumidade) {
		this.medicaoHumidade = medicaoHumidade;
	}

	public void setMedicaoLuminosidade(int medicaoLuminosidade) {
		this.medicaoLuminosidade = medicaoLuminosidade;
	}

	public void setMedicaoMovimento(int medicaoMovimento) {
		this.medicaoMovimento = medicaoMovimento;
	}

	public void setDate(String date) {
		this.date = date;
	}

}
