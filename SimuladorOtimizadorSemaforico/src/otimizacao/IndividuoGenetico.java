package otimizacao;

import entidades.Cruzamento;
import entidades.Rua;
import java.util.ArrayList;
import simulacao.Chegada;
import simulacao.Rede;

public class IndividuoGenetico extends Individuo
{
    private int[] abertura;

    public IndividuoGenetico(ArrayList<int[]> configuracoes, int[] abertura)
    {
        super(configuracoes);
        this.abertura = abertura;
    }

    public int[] getAbertura()
    {
        return this.abertura;
    }

    public void alteraConfiguracao(int indice, int[] novaConfiguracao)
    {
        if (novaConfiguracao.length == this.configuracoes.get(indice).length)
        {
            for (int i = 0; i < novaConfiguracao.length; i++)
            {
                this.configuracoes.get(indice)[i] = novaConfiguracao[i];
            }
        } else
        {
            System.out.println("ERRO Mutacao com vetores de tamanho diferentes");
        }
    }

    public void simular(ArrayList<Chegada> chegadas)
    {
        //System.out.println("Resetando chegadas...");
        Rede.getInstance().resetar(chegadas);
        //System.out.println("Determinando tempos dos semáforos");
        Rede.getInstance().determinarTempoSemaforosAutomatico(this.configuracoes);
        //System.out.println("Atrasando aberturas");
        Rede.getInstance().atrasarAberturas(this.abertura);
        /*for (Chegada c : Rede.getInstance().getChegadas())
         {
         System.out.println("\tRua " + c.getRua().getCodigo() + " | Chegada: " + c.getHoraChegada());
         }*/
        //System.out.println("Iniciando simulacao");
        //System.out.println("============================");
        //System.out.println(this.toString());
        Rede.getInstance().simular();
        //System.out.println("Calculando solucao");
        this.calculaSolucao();
        //System.out.println(this.toString());
        //System.out.println("============================");
    }

    @Override
    public void calculaSolucao()
    {
        Rede rede = Rede.getInstance();
        double somatorio = 0;
        for (Cruzamento c : rede.getCruzamentos())
        {
            for (Rua rua : c.getRuas())
            {
                if (rua.getViaEntrada() != null)
                {
                    //System.out.println("\nCruzamento: " + (c.getCodigo() + 1) + ", Rua: " + rua.getCodigo());
                    //System.out.println("TempoEsperaTotal: " + rua.getViaEntrada().getTempoEsperaTotal());
                    //System.out.println("Total carros: " + rua.getViaEntrada().getNumeroTotalCarros());
                    //System.out.println("Media atual: " + rua.getViaEntrada().getTempoEsperaTotal()/rua.getViaEntrada().getNumeroTotalCarros());
                    if (rua.getViaEntrada().getNumeroTotalCarros() > 0)
                    {
                        somatorio += ((rua.getViaEntrada().getTempoEsperaTotal()
                                / rua.getViaEntrada().getNumeroTotalCarros())
                                * rua.getViaEntrada().getPeso());
                        
                    }else
                    {
                        //System.out.println("NUMERO DE CARROS IGUAL A ZERO");
                    }
                    //System.out.println("somatorio atual: " + somatorio);
                }

            }
        }
        this.solucao = somatorio;
    }
    
    public void imprimirResultados()
    {
        Rede rede = Rede.getInstance();
        for (Cruzamento c : rede.getCruzamentos())
        {
            for (Rua rua : c.getRuas())
            {
                if (rua.getViaEntrada() != null)
                {
                    System.out.println("\nCruzamento: " + (c.getCodigo() + 1) + ", Rua: " + rua.getCodigo());
                    System.out.println("TempoEsperaTotal: " + rua.getViaEntrada().getTempoEsperaTotal());
                    System.out.println("Total carros: " + rua.getViaEntrada().getNumeroTotalCarros());
                    System.out.println("Media: " + rua.getViaEntrada().getTempoEsperaTotal()/rua.getViaEntrada().getNumeroTotalCarros());
                }

            }
        }
        System.out.println("Tempo espera carros: " + rede.getEsperaTotalCarros()/rede.getNumTotalCarros());
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("****************************\n");
        builder.append("Configuracoes e abertura: \n");
        for (int i = 0; i < this.abertura.length; i++)
        {
            int tam = this.configuracoes.get(i).length;
            for (int j = 0; j < tam; j++)
            {
                int aux = this.configuracoes.get(i)[j];
                builder.append(aux);
                builder.append(" ");
            }
            builder.append(" / ");
            builder.append(this.abertura[i]);
            builder.append("\n");
        }
        builder.append("Solucao: ");
        builder.append(this.solucao);
        builder.append("\n");
        builder.append("****************************\n");
        return builder.toString();
    }
}
