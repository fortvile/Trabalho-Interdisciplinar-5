package otimizacao;

public class Populacao
{

    private Individuo[] individuos;
    private int indice;
    private int max;

    public Populacao(int tamanho)
    {
        this.indice = 0;
        this.max = tamanho;
        this.individuos = new Individuo[tamanho];
    }

    public Individuo[] getIndividuos()
    {
        return this.individuos;
    }
    
    public int getMax()
    {
        return this.max;
    }

    public void addIndividuo(Individuo i)
    {
        if (this.indice < max)
        {
            this.individuos[this.indice] = i;
            this.indice++;
        } else
        {
            System.out.println("Populacao cheia");
        }
    }
    
    public Individuo getIndividuo(int indice)
    {
        return this.individuos[indice];
    }
}
