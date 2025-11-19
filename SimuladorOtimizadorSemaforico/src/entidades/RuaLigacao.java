package entidades;

public class RuaLigacao
{

    private Rua origem;
    private Rua destino;
    private double tempoTravessia;

    public RuaLigacao(Rua origem, Rua destino, double tempoTravessia)
    {
        this.origem = origem;
        this.destino = destino;
        this.tempoTravessia = tempoTravessia;
    }

    public Rua getOrigem()
    {
        return this.origem;
    }

    public Rua getDestino()
    {
        return this.destino;
    }

    public double getTempoTravessia()
    {
        return this.tempoTravessia;
    }
}
