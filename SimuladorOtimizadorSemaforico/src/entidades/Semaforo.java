package entidades;

public class Semaforo
{

    private int tempoVerde;
    private int tempoVermelho;
    private int atraso;

    public Semaforo(int tempoVerde, int tempoVermelho)
    {
        this.tempoVerde = tempoVerde;
        this.tempoVermelho = tempoVermelho;
        this.atraso = 0;
    }

    public int getTempoVerde()
    {
        return tempoVerde;
    }

    public int getTempoVermelho()
    {
        return tempoVermelho;
    }

    public int getAtraso()
    {
        return atraso;
    }

    public void setAtraso(int atraso)
    {
        this.atraso = atraso;
    }

    public boolean descobreEstadoSinal(Double h)
    {
        int hora = h.intValue();
        
        if ((hora - this.atraso) % (this.tempoVerde + this.tempoVermelho) < this.tempoVerde && hora - this.atraso >= 0)
        {
            return true;
        } else
        {
            return false;
        }
    }

    //Descobre quanto tempo falta para o sinal abrir
    public double fimVermelho(double hora)
    {
        if (hora - this.atraso < 0)
        {
            return (this.atraso - hora);
        } else
        {
            Double c = (hora - this.atraso) / (this.tempoVerde + this.tempoVermelho);
            int ciclos = c.intValue();
            return ((((this.tempoVerde + this.tempoVermelho) * (ciclos + 1)) + this.atraso - hora));
        }
    }
    /*
    //Descobre ha quanto tempo o sinal esta verde a partir de uma horaChegada
    public double tempoAndadoVerde(double hora)
    {
        Double c = (hora - this.atraso) / (this.tempoVerde + this.tempoVermelho);
        int ciclos = c.intValue();
        return (hora - this.atraso - ((this.tempoVerde + this.tempoVermelho) * ciclos));
    }*/
}
