package otimizacao;

import entidades.Cruzamento;
import entidades.Rua;
import entidades.ViaEntradaComSemaforo;
import java.util.ArrayList;
import java.util.Random;
import simulacao.Chegada;
import simulacao.Rede;

public class OtimizadorConcreto1 implements Otimizador
{

    public ArrayList<Chegada> chegadas;

    public OtimizadorConcreto1(ArrayList<Chegada> chegadas)
    {
        this.chegadas = chegadas;
    }

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
                int somatorio = 0;
                for (int j = 1; j < configuracao.length; j++)
                {
                    somatorio += configuracao[j];
                }
                abertura[i] = rand.nextInt(somatorio + 1);
                i++;
            }
        }
        return new IndividuoGenetico(configuracoes, abertura);
    }

    private ArrayList<double[]> getTemposEntreChegadas()
    {
        ArrayList<double[]> temposEntreChegadas = new ArrayList<double[]>();
        for (Cruzamento c : Rede.getInstance().getCruzamentos())
        {
            if (c.getTemSemaforo() == 1)
            {
                double[] tempos = new double[c.getNumViasEntrada()];
                int cont = 0;
                for (Rua r : c.getRuas())
                {
                    if (r.getViaEntrada() != null)
                    {
                        ViaEntradaComSemaforo via = (ViaEntradaComSemaforo) r.getViaEntrada();
                        tempos[cont] = via.getMediaEntreChegadas();
                        cont++;
                    }
                }
                temposEntreChegadas.add(tempos);
            }
        }
        return temposEntreChegadas;
    }
    
    public ArrayList<int[]> otimizarCruzamentosIndividuais(ArrayList<double[]> temposEntreChegadas)
    {
        ArrayList<int[]> configuracoes = new ArrayList<int[]>();
        Contexto contexto = new Contexto();
        int cont = 0;
        for(Cruzamento c: Rede.getInstance().getCruzamentos())
        {
            if(c.getTemSemaforo() == 1)
            {
                contexto.setAlgoritmo(new GeneticoConcretoCruzamento(c, 
                        temposEntreChegadas.get(cont), Rede.getInstance().getTempoSimulacao()));
                IndividuoCruzamento individuo = contexto.otimizarCruzamento(null);
                configuracoes.add(individuo.getConfiguracoes().get(0));
                cont++;
            }
        }
        return configuracoes;
    }

    @Override
    public IndividuoGenetico otimizar(Individuo base)
    {
        IndividuoGenetico individuoGenetico = this.criaIndividuo();
        individuoGenetico.simular(Rede.getInstance().cloneChegadas(this.chegadas));
        System.out.println("Individuo Aleatorio Inicial: ");
        System.out.println(individuoGenetico.toString());
        ArrayList<double[]> temposEntreChegadas;
        ArrayList<int[]> configuracoes;
        Contexto contexto = new Contexto();
        for (int i = 0; i < 3; i++)
        {
            temposEntreChegadas = this.getTemposEntreChegadas();
            System.out.println("Tempos entre chegadas:");
            for(int j=0; j<temposEntreChegadas.size(); j++)
            {
                for(int k=0; k<temposEntreChegadas.get(j).length; k++)
                {
                    System.out.printf("%.2f ",temposEntreChegadas.get(j)[k]);
                }
                System.out.println("");
            }
            configuracoes = this.otimizarCruzamentosIndividuais(temposEntreChegadas);
            System.out.println("Configuracoes individuais otimizadas");
            for(int j=0; j<configuracoes.size(); j++)
            {
                for(int k=0; k<configuracoes.get(j).length; k++)
                {
                    System.out.printf("%d ",configuracoes.get(j)[k]);
                }
                System.out.println("");
            }
            contexto.setAlgoritmo(new GeneticoOtimizadorAtraso(Rede.getInstance().cloneChegadas(this.chegadas), configuracoes));
            individuoGenetico = contexto.otimizarGenetico(null);
            //individuoGenetico.simular(Rede.getInstance().cloneChegadas(this.chegadas));
            System.out.println("Individuo otimizado: ");
            System.out.println(individuoGenetico.toString());
        }

        return individuoGenetico;
    }

}
