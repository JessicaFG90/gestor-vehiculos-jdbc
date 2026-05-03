package model.domain;

public class Vehiculo {
	
	private int id;
	private String marca;
	private String modelo;
	private String matricula;
	private double consumoHomologado;

	public Vehiculo() {

	}

	public Vehiculo(int id, String marca, String modelo, String matricula, double consumoHomologado) {
		super();
		this.id = id;
		this.marca = marca;
		this.modelo = modelo;
		this.matricula = matricula;
		this.consumoHomologado = consumoHomologado;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public String getMatricula() {
		return matricula;
	}
	
	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}
	
	public double getConsumoHomologado() {
		return consumoHomologado;
	}

	public void setConsumoHomologado(double consumoHomologado) {
		this.consumoHomologado = consumoHomologado;
	}

	@Override
	public String toString() {
		return "Vehiculo [id=" + id + ", marca=" + marca + ", modelo=" + modelo + ", matricula=" + matricula
				+ ", consumoHomologado=" + consumoHomologado + "]";
	}

}
