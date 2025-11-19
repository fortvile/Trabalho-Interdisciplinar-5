package entidades;

public class ViaEntradaComSemaforo extends ViaEntrada
{
    private Semaforo semaforo;
    private double tempoEntreChegadas;
    private long totalChegadas;
    private double ultimaChegada;
    
    public ViaEntradaComSemaforo(int codigo, int numeroFaixas, Faixa[] faixas, 
            Double mediaCarros, int[] direcoes, double[] probabilidades, int peso)
    {
        super(codigo, numeroFaixas, faixas, mediaCarros, direcoes, probabilidades, peso);
        this.semaforo = null;
        this.tempoEntreChegadas = 0;
        this.totalChegadas = 0;
        this.ultimaChegada = 0;
    }

    public Semaforo getSemaforo() 
    {
            return semaforo;
    }

    public void setSemaforo(Semaforo semaforo)
    {
            this.semaforo = semaforo;
    }
    
    public double getTempoEntreChegadas()
    {
        return this.tempoEntreChegadas;
    }
    
    public double getTotalChegadas()
    {
        return this.totalChegadas;
    }
    
    public double getUltimaChegada()
    {
        return this.ultimaChegada;
    }
    
    public void setTempoEntreChegadas(double valor)
    {
        this.tempoEntreChegadas = valor;
    }
    
    public void setTotalChegadas(long valor)
    {
        this.totalChegadas = valor;
    }
    
    public void setUltimaChegada(double valor)
    {
        this.ultimaChegada = valor;
    }
    
    public double getMediaEntreChegadas()
    {
        return this.tempoEntreChegadas/this.totalChegadas;
    }
    
    public void addTempoEntreChegadas(double tempo)
    {
        this.tempoEntreChegadas += tempo;
        this.totalChegadas++;
    }

    public void atrasarSemaforo(int atraso)
    {
            this.semaforo.setAtraso(atraso);
    }
}
