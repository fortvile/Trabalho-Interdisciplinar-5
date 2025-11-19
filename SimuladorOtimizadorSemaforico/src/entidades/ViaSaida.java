package entidades;

public class ViaSaida
{
	private int codigo;
	
	public ViaSaida(Integer saida)
	{
		this.codigo = saida;
	}
	
    public int getCodigo()
    {
    	return this.codigo;
    }
    
    public void setCodigo(int codigo)
    {
    	this.codigo = codigo;
    }
}
