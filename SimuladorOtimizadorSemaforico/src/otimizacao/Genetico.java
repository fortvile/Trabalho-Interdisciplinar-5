package otimizacao;

public interface Genetico extends Otimizador
{
    abstract Populacao gerarNovaPopulacao(Individuo[][] individuos, int max);

    abstract Individuo cruzarIndividuos(Individuo i1, Individuo i2);
    
    abstract void mutacao(Populacao populacao);

    abstract void mutar(Individuo i);
    
    abstract Populacao geraPopulacaoInicial();
    
    abstract Individuo criaIndividuo();
    
    abstract int[] geraConfiguracaoAleatoria(int tam);
    
    abstract Individuo encontraMelhorSolucao(Populacao populacao);
    
    abstract void simularIndividuos(Populacao populacao);
    
    abstract Populacao novaGeracao(Populacao populacao);
}
