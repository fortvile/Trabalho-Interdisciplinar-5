package entidades;

public class Rua
{
    protected int codigo;
    protected ViaEntrada viaEntrada;
    protected ViaSaida viaSaida;
    protected boolean fonte;
    protected boolean saida;
	
    public Rua(int codigo, ViaEntrada entrada, ViaSaida saida, boolean isFonte, boolean isSaida)
    {
        this.fonte = isFonte;
        this.saida = isSaida;
        this.codigo = codigo;
        this.viaEntrada = entrada;
        this.viaSaida = saida;
    }

    public boolean getFonte()
    {
        return this.fonte;
    }

    public boolean getSaida()
    {
        return this.saida;
    }

    public int getCodigo() 
    {
        return codigo;
    }

    public ViaEntrada getViaEntrada()
    {
        return viaEntrada;
    }

    public ViaSaida getViaSaida() 
    {
        return viaSaida;
    }

    public String destinos()
    {
        if(this.viaEntrada == null)
        {
            return "";
        }
        else
        {
            String destinos = "";
            for(int destino: this.viaEntrada.getDirecoes())
            {
                destinos += destino + " ";
            }
            return destinos;
        }
    }

    public void addPares(String pares)
    {
        if(this.viaEntrada != null)
        {
            this.viaEntrada.addPares(pares);
        }
    }
}