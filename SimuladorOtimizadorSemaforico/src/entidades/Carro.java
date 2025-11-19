package entidades;

public class Carro
{

    private double reacao;
    private double comprimento;
    private double tempoEspera;

    public Carro(double reacao, double comprimento)
    {
        this.reacao = reacao;
        this.comprimento = comprimento;
        this.tempoEspera = 0;
    }

    public double getReacao()
    {
        return this.reacao;
    }

    public double getComprimento()
    {
        return this.comprimento;
    }

    public double getTempoEspera()
    {
        return this.tempoEspera;
    }

    public void aumentaTempoEspera(double tempo)
    {
        this.tempoEspera += tempo;
    }
}
