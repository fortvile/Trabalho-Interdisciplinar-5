package otimizacao;

import entidades.Cruzamento;
import entidades.Faixa;
import entidades.Rua;
import entidades.Semaforo;
import entidades.ViaEntrada;
import entidades.ViaEntradaComSemaforo;
import java.util.ArrayList;
import simulacao.Chegada;
import simulacao.Movimento;
import simulacao.Rede;

public class IndividuoCruzamento extends Individuo
{

    private Cruzamento cruzamento;

    public IndividuoCruzamento(ArrayList<int[]> configuracoes, Cruzamento cruzamento)
    {
        super(configuracoes);
        this.cruzamento = cruzamento;
    }

    public void resetarCruzamento()
    {
        cruzamento.setAtrasoInicial(0);
        for (Rua rua : cruzamento.getRuas())
        {
            if (rua.getViaEntrada() != null)
            {
                rua.getViaEntrada().setNumeroTotalCarros(0);
                rua.getViaEntrada().setTempoEsperaTotal(0);
                ViaEntradaComSemaforo via = (ViaEntradaComSemaforo) rua.getViaEntrada();
                via.setTempoEntreChegadas(0);
                via.setTotalChegadas(0);
                via.setUltimaChegada(0);
                for (Faixa f : rua.getViaEntrada().getFaixas())
                {
                    f.setUltimaSaida(0);
                }
            }
        }
    }

    public void determinarTempoSemaforosAutomatico()
    {
        int total = 0;
        for (int i = 0; i < this.configuracoes.get(0).length; i++)
        {
            total += this.configuracoes.get(0)[i];
        }
        int indexGrupo = 0;
        for (ArrayList<Movimento> grupo : cruzamento.getGrupos())
        {
            ArrayList<Rua> ruas = new ArrayList<Rua>();
            Rua atual;
            int aux = 0;
            for (Movimento movimento : grupo)
            {
                atual = cruzamento.retornaRuaPorMovimento(movimento);
                if (aux != atual.getCodigo())
                {
                    ruas.add(atual);
                    aux = atual.getCodigo();
                }
            }
            for (Rua rua : ruas)
            {
                ViaEntradaComSemaforo temp = (ViaEntradaComSemaforo) rua.getViaEntrada();
                temp.setSemaforo(new Semaforo(configuracoes.get(0)[indexGrupo],
                        total - configuracoes.get(0)[indexGrupo]));
            }
            indexGrupo++;
        }
    }

    public void atrasarAberturas()
    {
        Rua rua;
        int atraso = 0;
        cruzamento.setAtrasoInicial(atraso);
        for (ArrayList<Movimento> movimentos : cruzamento.getGrupos())
        {
            rua = null;
            for (Movimento movimento : movimentos)
            {
                Rua aux = cruzamento.retornaRuaPorMovimento(movimento);
                if (aux != null)
                {
                    if (rua == null || aux.getCodigo() != rua.getCodigo())
                    {
                        rua = aux;
                        ViaEntradaComSemaforo temp = (ViaEntradaComSemaforo) rua.getViaEntrada();
                        temp.atrasarSemaforo(atraso);
                    }
                } else
                {
                    System.out.println("Retornou null no atraso");
                }
            }
            ViaEntradaComSemaforo temp = (ViaEntradaComSemaforo) rua.getViaEntrada();
            atraso += temp.getSemaforo().getTempoVerde();
        }
    }

    public double calculaEsperaChegada(Chegada chegada, ViaEntradaComSemaforo via, Faixa faixa)
    {
        Double horaSaida;
        Double horaChegada = chegada.getHoraChegada();
        Semaforo semaforo = via.getSemaforo();
        Cruzamento cruzamento = chegada.getCruzamento();
        Double saidaAnterior = faixa.getUltimaSaida();
        if (horaChegada >= saidaAnterior) //carro atual chegou depois do anterior sair
        {
            if (semaforo.descobreEstadoSinal(horaChegada) == true)
            {
                //Nesse caso, o carro nao precisou parar
                horaSaida = horaChegada;
            } else
            {
                //o carro eh obrigado a parar e eh o primeiro da fila
                horaSaida = horaChegada + chegada.getCarro().getReacao() + (semaforo.fimVermelho(horaChegada));
            }
        } else //o carro chegou antes do anterior sair, ou seja, nao eh o primeiro na fila
        {
            horaSaida = saidaAnterior;
            if (semaforo.descobreEstadoSinal(horaSaida) == false)
            {
                horaSaida = horaSaida + chegada.getCarro().getReacao() + semaforo.fimVermelho(horaSaida);
            }
        }
        return horaSaida - horaChegada;
    }

    public void processaChegada(Chegada chegada)
    {
        chegada.getRua().getViaEntrada().aumentaNumeroTotalCarros();
        chegada.getRua().getViaEntrada().aumentaTempoEsperaTotal(chegada.getHoraSaida() - chegada.getHoraChegada());
    }

    public void simularCruzamento(ArrayList<ArrayList<Chegada>> lista)
    {
        for (ArrayList<Chegada> chegadas : lista)
        {
            while (chegadas.size() > 0)
            {
                Chegada chegada = chegadas.get(0);
                chegadas.remove(0);
                ViaEntrada via = chegada.getRua().getViaEntrada();
                int direcaoCarro = chegada.getDirecao();
                Faixa faixaEscolhida = via.determinaFaixa(chegada.getCarro(), direcaoCarro);
                //System.out.println("\nCodigo: " + chegada.getCodigo() + "\nChegada: " + chegada.getHoraChegada());
                //System.out.println("Cruzamento: " + (chegada.getCruzamento().getCodigo() + 1));
                //System.out.println("Rua: " + chegada.getRua().getCodigo());
                //System.out.println("Direcao: " + chegada.getDirecao());
                if (faixaEscolhida != null)
                {
                    //System.out.println("Ultima saida da faixa escolhida: " + faixaEscolhida.getUltimaSaida());
                    ViaEntradaComSemaforo entrada = (ViaEntradaComSemaforo) via;
                    double tempoEspera = this.calculaEsperaChegada(chegada, entrada, faixaEscolhida);
                    chegada.setHoraSaida(chegada.getHoraChegada() + tempoEspera);
                    chegada.getCarro().aumentaTempoEspera(tempoEspera);
                    //System.out.println("Tempo espera atual: " + tempoEspera + " | Saida: " + chegada.getHoraSaida());
                    //System.out.println("Tempo espera total: " + chegada.getCarro().getTempoEspera());
                    faixaEscolhida.setUltimaSaida(chegada.getHoraSaida() + chegada.getCarro().getComprimento());
                    this.processaChegada(chegada);
                } else
                {
                    System.out.println("Faixa escolhida null");
                }
            }
        }
    }

    public void simular(ArrayList<ArrayList<Chegada>> chegadas)
    {
        this.resetarCruzamento();
        this.determinarTempoSemaforosAutomatico();
        this.atrasarAberturas();
        this.simularCruzamento(chegadas);
        this.calculaSolucao();
        //System.out.println(this.toString());
    }

    @Override
    public void calculaSolucao()
    {
        double somatorio = 0;
        for (Rua rua : cruzamento.getRuas())
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
                } else
                {
                    //System.out.println("NUMERO DE CARROS IGUAL A ZERO");
                }
                //System.out.println("somatorio atual: " + somatorio);
            }

        }
        this.solucao = somatorio;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("****************************\n");
        builder.append("Configuracoes: \n");
        int tam = this.configuracoes.get(0).length;
        for (int j = 0; j < tam; j++)
        {
            int aux = this.configuracoes.get(0)[j];
            builder.append(aux);
            builder.append(" ");
        }
        builder.append("\n");
        builder.append("Solucao: ");
        builder.append(this.solucao);
        builder.append("\n");
        builder.append("****************************\n");
        return builder.toString();
    }
}
