package otimizacao;

import entidades.Cruzamento;
import entidades.Rua;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import simulacao.Chegada;
import simulacao.Rede;

public class GeneticoConcretoCruzamento implements Genetico
{

    public static int TAMANHO_POPULACAO = 100;
    public static int TAXA_MUTACAO = 5;
    public ArrayList<ArrayList<Chegada>> chegadas;
    public Cruzamento cruzamento;
    public double[] tempoEntreChegadas;

    public GeneticoConcretoCruzamento(Cruzamento cruzamento, double[] tempoEntreChegadas, double tempoSimulacao)
    {
        this.cruzamento = cruzamento;
        this.tempoEntreChegadas = tempoEntreChegadas;
        this.chegadas = new ArrayList<ArrayList<Chegada>>();
        this.gerarChegadas(tempoSimulacao);
    }

    public double exponencial(double mediaCarros)
    {
        Random rand = new Random();
        double r = rand.nextDouble();
        return (-mediaCarros * Math.log(1.0 - r));
    }

    private void gerarChegadas(double tempoSimulacao)
    {
        double horaChegada;
        int cont = 0;
        for (Rua rua : this.cruzamento.getRuas())
        {
            if (rua.getViaEntrada() != null)
            {
                horaChegada = 0;
                this.chegadas.add(new ArrayList<Chegada>());
                do
                {
                    horaChegada += exponencial(this.tempoEntreChegadas[cont]);
                    if (horaChegada <= tempoSimulacao)
                    {
                        this.chegadas.get(cont).add(Rede.getInstance().geraChegada(rua, horaChegada));
                    }
                } while (horaChegada <= tempoSimulacao);
                cont++;
            }
        }
    }

    @Override
    public Populacao gerarNovaPopulacao(Individuo[][] individuosAux, int max)
    {
        IndividuoCruzamento[][] individuos = (IndividuoCruzamento[][]) individuosAux;
        Populacao nova = new Populacao(max);
        //System.out.println("NOVA POPULACAO");
        for (int i = 0; i < max; i++)
        {
            //System.out.println("CRUZA PARA GERAR INDIVIDUO " + i);
            nova.addIndividuo(cruzarIndividuos(individuos[i][0], individuos[i][1]));
        }
        return nova;
    }

    @Override
    public IndividuoCruzamento cruzarIndividuos(Individuo ind1, Individuo ind2)
    {
        IndividuoCruzamento i1 = (IndividuoCruzamento) ind1;
        IndividuoCruzamento i2 = (IndividuoCruzamento) ind2;
        //System.out.println("---------------------");
        //System.out.println("ESTOU CRUZANDO: ");
        //System.out.println(i1.toString());
        //System.out.println(i2.toString());
        //System.out.println("---------------------");
        ArrayList<int[]> configuracao = new ArrayList<int[]>();
        int temposAbertos[] = new int[i1.getConfiguracoes().get(0).length];
        Random rand = new Random();
        boolean solucaoEscolhida;
        int somatorio = 0;
        for (int i = 0; i < i1.getConfiguracoes().get(0).length; i++)
        {
            solucaoEscolhida = rand.nextBoolean();
            if (solucaoEscolhida)
            {
                temposAbertos[i] = i1.getConfiguracoes().get(0)[i];
            } else
            {
                temposAbertos[i] = i2.getConfiguracoes().get(0)[i];
            }
            somatorio += temposAbertos[i];
        }
        int tam = temposAbertos.length;
        if(somatorio > 120)
        {
            int pos = 0;
            do
            {
                if(temposAbertos[pos] > 10)
                {
                    temposAbertos[pos]--;
                    somatorio--;
                }
                pos = (pos + 1) % tam;
            }while(somatorio > 120);
        }
        configuracao.add(temposAbertos);
        return new IndividuoCruzamento(configuracao, cruzamento);
    }

    @Override
    public void mutacao(Populacao populacao)
    {
        Random rand = new Random();
        int probabilidade;
        for (Individuo ind : populacao.getIndividuos())
        {
            IndividuoCruzamento i = (IndividuoCruzamento) ind;
            probabilidade = rand.nextInt(100);
            if (probabilidade >= 0 && probabilidade <= TAXA_MUTACAO)
            {
                //System.out.println("MUTEI INDIVIDUO " + cont);
                this.mutar(i);
            }
        }
    }

    @Override
    public void mutar(Individuo ind)
    {
        IndividuoCruzamento i = (IndividuoCruzamento) ind;
        Random rand = new Random();
        int tam = i.getConfiguracoes().get(0).length;
        int semaforoMutado = rand.nextInt(tam);
        int somatorio = 0;
        for(int j=0; j<tam; j++)
        {
            if(j != semaforoMutado)
            {
                somatorio += i.getConfiguracoes().get(0)[j];
            }
        }
        i.getConfiguracoes().get(0)[semaforoMutado] = rand.nextInt(111 - somatorio) + 10;
    }

    private IndividuoCruzamento[][] torneio(Populacao populacao)
    {
        Random rand = new Random();
        int i1, i2;
        int max = populacao.getMax();
        IndividuoCruzamento[][] vencedores = new IndividuoCruzamento[max][2];
        for (int i = 0; i < max; i++)
        {
            i1 = rand.nextInt(max);
            do
            {
                i2 = rand.nextInt(max);
            } while (i1 == i2);
            vencedores[i][0] = this.compara((IndividuoCruzamento) populacao.getIndividuo(i1),
                    (IndividuoCruzamento) populacao.getIndividuo(i2));

            i1 = rand.nextInt(max);
            do
            {
                i2 = rand.nextInt(max);
            } while (i1 == i2);
            vencedores[i][1] = this.compara((IndividuoCruzamento) populacao.getIndividuo(i1),
                    (IndividuoCruzamento) populacao.getIndividuo(i2));
        }
        return vencedores;
    }

    private IndividuoCruzamento compara(IndividuoCruzamento i1, IndividuoCruzamento i2)
    {
        if (i1.getSolucao() < i2.getSolucao())
        {
            return i1;
        } else
        {
            return i2;
        }
    }

    @Override
    public Populacao geraPopulacaoInicial()
    {
        int tam = TAMANHO_POPULACAO;
        Populacao inicial = new Populacao(tam);
        for (int i = 0; i < tam; i++)
        {
            inicial.addIndividuo(criaIndividuo());
        }
        return inicial;
    }

    @Override
    public IndividuoCruzamento criaIndividuo()
    {
        ArrayList<int[]> config = new ArrayList<int[]>();
        config.add(this.geraConfiguracaoAleatoria(this.tempoEntreChegadas.length));
        return new IndividuoCruzamento(config, this.cruzamento);
    }

    @Override
    public int[] geraConfiguracaoAleatoria(int tam)
    {
        int[] configuracao = new int[tam];
        int max = 120 - (10*(tam-1));
        for (int i = 0; i < tam; i++)
        {
            Random rand = new Random();
            configuracao[i] = rand.nextInt(max + 1 - 10) + 10;
            max -= (configuracao[i] - 10);
        }
        return configuracao;
    }

    @Override
    public IndividuoCruzamento encontraMelhorSolucao(Populacao populacao)
    {
        IndividuoCruzamento melhor = null;
        for (Individuo individuo : populacao.getIndividuos())
        {
            IndividuoCruzamento i = (IndividuoCruzamento) individuo;
            if (melhor == null)
            {
                melhor = i;
            } else if (melhor.getSolucao() > i.getSolucao())
            {
                melhor = i;
            }
        }
        return melhor;
    }

    public ArrayList<ArrayList<Chegada>> cloneChegadas()
    {
        ArrayList<ArrayList<Chegada>> clonadas = new ArrayList<ArrayList<Chegada>>();
        int cont = 0;
        for (ArrayList<Chegada> lista : this.chegadas)
        {
            clonadas.add(new ArrayList<Chegada>());
            for (Chegada c : lista)
            {
                try
                {
                    clonadas.get(cont).add(c.clone());
                } catch (CloneNotSupportedException ex)
                {
                    Logger.getLogger(Rede.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            cont++;
        }
        return clonadas;
    }

    @Override
    public void simularIndividuos(Populacao populacao)
    {
        int cont = 0;
        for (Individuo individuo : populacao.getIndividuos())
        {
            //System.out.println("Simulando individuo " + cont);
            IndividuoCruzamento i = (IndividuoCruzamento) individuo;
            i.simular(this.cloneChegadas());
            cont++;
        }
    }

    @Override
    public Populacao novaGeracao(Populacao populacao)
    {
        IndividuoCruzamento[][] vencedores = this.torneio(populacao);
        Populacao nova = this.gerarNovaPopulacao(vencedores, populacao.getMax());
        this.mutacao(nova);
        return nova;
    }

    @Override
    public IndividuoCruzamento otimizar(Individuo inicialAbstrato)
    {
        System.out.println("Iniciando Algoritmo Genetico para o cruzamento " + (this.cruzamento.getCodigo() + 1));
        Populacao populacao = this.geraPopulacaoInicial();
        this.simularIndividuos(populacao);
        IndividuoCruzamento melhorDaGeracao;
        IndividuoCruzamento melhorSolucao;
        melhorSolucao = this.encontraMelhorSolucao(populacao);
        for (int geracao = 0; geracao < 10; geracao++)
        {
            //System.out.println("\nGeracao " + (geracao + 1) + ": ");
            populacao = this.novaGeracao(populacao);
            this.simularIndividuos(populacao);
            melhorDaGeracao = this.encontraMelhorSolucao(populacao);
            if (melhorSolucao.getSolucao() > melhorDaGeracao.getSolucao())
            {
                melhorSolucao = melhorDaGeracao;
            }
        }
        return melhorSolucao;
    }
}
