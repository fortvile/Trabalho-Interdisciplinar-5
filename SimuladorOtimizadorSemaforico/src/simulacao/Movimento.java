package simulacao;

public class Movimento {
	private int origem;
	private int destino;
	private char nome;
	
	public Movimento(int a, int b)
	{
		this.origem = a;
		this.destino = b;
		this.nome = ' ';
	}
	
	public int getOrigem()
	{
		return this.origem;
	}
	
	public int getDestino()
	{
		return this.destino;
	}
	
	public char getNome()
	{
		return this.nome;
	}
	
	public void setNome(char nome)
	{
		this.nome = nome;
	}
	
	@Override
	public String toString()
	{
		return nome + ": " + origem + " " + destino;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Movimento)
		{
			Movimento p = (Movimento) o;
			return p.getNome() == this.nome && p.getDestino() == this.destino && p.getOrigem() == this.origem;
		}
		else
		{
			return false;
		}
	}
}
