package otimizacao;

import java.util.ArrayList;

public abstract class Individuo
{
    protected ArrayList<int[]> configuracoes;
    protected double solucao;
    
    public Individuo(ArrayList<int[]> configuracoes)
    {
        this.configuracoes = configuracoes;
        solucao = -1;
    }
    
    public ArrayList<int[]> getConfiguracoes()
    {
        return this.configuracoes;
    }
    
    public double getSolucao()
    {
        return this.solucao;
    }
    
    public abstract void calculaSolucao();
}
