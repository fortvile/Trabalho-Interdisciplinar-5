package otimizacao;

public class Contexto
{
    public Otimizador algoritmo;
    
    public Contexto()
    {
        this.algoritmo = null;
    }
    
    public Otimizador getAlgoritmo()
    {
        return this.algoritmo;
    }
    
    public void setAlgoritmo(Otimizador genetico)
    {
        this.algoritmo = genetico;
    }
    
    public IndividuoGenetico otimizarGenetico(IndividuoGenetico inicial)
    {
        if(this.algoritmo != null) 
            return (IndividuoGenetico) algoritmo.otimizar(inicial);
        else return null;
    }
    
    public IndividuoCruzamento otimizarCruzamento(IndividuoCruzamento inicial)
    {
        if(this.algoritmo != null) 
            return (IndividuoCruzamento) algoritmo.otimizar(inicial);
        else return null;
    }
}
