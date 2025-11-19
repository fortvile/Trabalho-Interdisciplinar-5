package otimizacao;

import entidades.Cruzamento;
import java.util.ArrayList;
import java.util.Random;
import simulacao.Chegada;
import simulacao.Rede;

public class GeneticoConcreto2 implements Genetico
{

    public static int TAMANHO_POPULACAO = 100;
    public static int TAXA_MUTACAO = 15;
    public ArrayList<Chegada> chegadas;

    public GeneticoConcreto2(ArrayList<Chegada> chegadas)
    {
        this.chegadas = chegadas;
    }

    @Override
    public Populacao gerarNovaPopulacao(Individuo[][] individuosAux, int max)
    {
        IndividuoGenetico[][] individuos = (IndividuoGenetico[][]) individuosAux;
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
    public IndividuoGenetico cruzarIndividuos(Individuo individuo1, Individuo individuo2)
    {
        IndividuoGenetico i1 = (IndividuoGenetico) individuo1;
        IndividuoGenetico i2 = (IndividuoGenetico) individuo2;
        int numCruzamentos = i1.getConfiguracoes().size();
        ArrayList<int[]> configuracoes = new ArrayList<int[]>();
        int[] atrasos = new int[numCruzamentos];
        Random rand = new Random();
        boolean solucaoEscolhida;
        int somatorio;
        for (int i = 0; i < numCruzamentos; i++)
        {
            int[] configuracao = new int[i1.getConfiguracoes().get(i).length];
            somatorio = 0;
            for (int cont = 0; cont < i1.getConfiguracoes().get(i).length; cont++)
            {
                solucaoEscolhida = rand.nextBoolean();
                if (solucaoEscolhida)
                {
                    configuracao[cont] = i1.getConfiguracoes().get(i)[cont];
                } else
                {
                    configuracao[cont] = i2.getConfiguracoes().get(i)[cont];
                }
                if (cont > 0)
                {
                    somatorio += configuracao[cont];
                }
            }
            configuracoes.add(configuracao);
            atrasos[i] = rand.nextInt(somatorio + 1);
        }
        return new IndividuoGenetico(configuracoes, atrasos);
    }

    @Override
    public void mutacao(Populacao populacao)
    {
        Random rand = new Random();
        int probabilidade;
        int cont = 0;
        for (Individuo i : populacao.getIndividuos())
        {
            probabilidade = rand.nextInt(100);
            if (probabilidade >= 0 && probabilidade <= TAXA_MUTACAO)
            {
                //System.out.println("MUTEI INDIVIDUO " + cont);
                this.mutar((IndividuoGenetico) i);
            }
            cont++;
        }
    }

    @Override
    public void mutar(Individuo individuo)
    {
        IndividuoGenetico i = (IndividuoGenetico) individuo;
        Random rand = new Random();
        int cruzamentoMutado = rand.nextInt(i.getConfiguracoes().size());
        int tam = i.getConfiguracoes().get(cruzamentoMutado).length;
        i.alteraConfiguracao(cruzamentoMutado, this.geraConfiguracaoAleatoria(tam));
        int somatorio = 0;
        for (int j = 1; j < tam; j++)
        {
            somatorio += i.getConfiguracoes().get(cruzamentoMutado)[j];
        }
        i.getAbertura()[cruzamentoMutado] = rand.nextInt(somatorio + 1);
    }

    private IndividuoGenetico[][] torneio(Populacao populacao)
    {
        Random rand = new Random();
        int i1, i2;
        int max = populacao.getMax();
        IndividuoGenetico[][] vencedores = new IndividuoGenetico[max][2];
        for (int i = 0; i < max; i++)
        {
            i1 = rand.nextInt(max);
            do
            {
                i2 = rand.nextInt(max);
            } while (i1 == i2);
            vencedores[i][0] = this.compara((IndividuoGenetico) populacao.getIndividuo(i1), 
                    (IndividuoGenetico) populacao.getIndividuo(i2));

            i1 = rand.nextInt(max);
            do
            {
                i2 = rand.nextInt(max);
            } while (i1 == i2);
            vencedores[i][1] = this.compara((IndividuoGenetico) populacao.getIndividuo(i1), 
                    (IndividuoGenetico) populacao.getIndividuo(i2));
        }
        return vencedores;
    }

    private IndividuoGenetico compara(IndividuoGenetico i1, IndividuoGenetico i2)
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
        Rede rede = Rede.getInstance();
        int tam = TAMANHO_POPULACAO;
        Populacao inicial = new Populacao(tam);
        for (int i = 0; i < tam; i++)
        {
            inicial.addIndividuo(criaIndividuo());
        }
        return inicial;
    }

    @Override
    public IndividuoGenetico criaIndividuo()
    {
        ArrayList<int[]> configuracoes = new ArrayList<>();
        int numCruzamentosSinalizados = 0;
        for (Cruzamento c : Rede.getInstance().getCruzamentos())
        {
            if (c.getTemSemaforo() == 1)
            {
                numCruzamentosSinalizados++;
            }
        }
        int[] abertura = new int[numCruzamentosSinalizados];
        Random rand = new Random();
        int i = 0;
        for (Cruzamento c : Rede.getInstance().getCruzamentos())
        {
            if (c.getTemSemaforo() == 1)
            {
                int[] configuracao = geraConfiguracaoAleatoria(c.getGrupos().size());
                configuracoes.add(configuracao);
                //System.out.print("\nConfiguracao: ");
                //for (int j = 0; j < configuracao.length; j++) System.out.print(configuracao[j] + " ");
                //System.out.println(" ");
                int somatorio = 0;
                for (int j = 1; j < configuracao.length; j++)
                {
                    somatorio += configuracao[j];
                    //System.out.println("Somatorio: " + somatorio);
                }
                abertura[i] = rand.nextInt(somatorio + 1);
                //System.out.println("Abertura: " + abertura[i]);
                i++;
            }
        }
        return new IndividuoGenetico(configuracoes, abertura);
    }

    @Override
    public int[] geraConfiguracaoAleatoria(int tam)
    {
        int[] configuracao = new int[tam];
        for (int i = 0; i < tam; i++)
        {
            Random rand = new Random();
            configuracao[i] = rand.nextInt(51) + 10;
        }
        return configuracao;
    }

    @Override
    public IndividuoGenetico encontraMelhorSolucao(Populacao populacao)
    {
        IndividuoGenetico melhor = null;
        for (Individuo i : populacao.getIndividuos())
        {
            if (melhor == null)
            {
                melhor = (IndividuoGenetico) i;
            } else if (melhor.getSolucao() > i.getSolucao())
            {
                melhor = (IndividuoGenetico) i;
            }
        }
        return melhor;
    }

    @Override
    public void simularIndividuos(Populacao populacao)
    {
        int cont = 0;
        for (Individuo individuo : populacao.getIndividuos())
        {
            IndividuoGenetico i = (IndividuoGenetico) individuo;
            System.out.println("Simulando individuo " + cont);
            i.simular(Rede.getInstance().cloneChegadas(this.chegadas));
            cont++;
        }
    }

    @Override
    public Populacao novaGeracao(Populacao populacao)
    {
        IndividuoGenetico[][] vencedores = this.torneio(populacao);
        Populacao nova = this.gerarNovaPopulacao(vencedores, populacao.getMax());
        this.mutacao(nova);
        return nova;
    }

    @Override
    public IndividuoGenetico otimizar(Individuo inicial)
    {
        IndividuoGenetico melhorSolucao;
        System.out.println("Iniciando Algoritmo Genetico...");
        Populacao populacao = this.geraPopulacaoInicial();
        this.simularIndividuos(populacao);
        melhorSolucao = this.encontraMelhorSolucao(populacao);
        IndividuoGenetico melhorDaGeracao;
        for (int geracao = 0; geracao < 10; geracao++)
        {
            System.out.println("\n\nGeracao " + (geracao + 1) + ": ");
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
