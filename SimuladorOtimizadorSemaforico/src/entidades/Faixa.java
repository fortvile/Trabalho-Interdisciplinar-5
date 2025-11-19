package entidades;

public class Faixa
{
	private int[] direcoes;
	private double ultimaSaida;
	
	public Faixa(int[] direcoes)
	{
		this.direcoes = direcoes;
		this.ultimaSaida = 0;
	}

	public int[] getDirecoes() 
	{
		return direcoes;
	}
	
	public void setDirecoes(int[] direcoes) 
	{
		this.direcoes = direcoes;
	}

	public double getUltimaSaida()
	{
		return ultimaSaida;
	}

	public void setUltimaSaida(double ultimaSaida)
	{
		this.ultimaSaida = ultimaSaida;
	}
}
