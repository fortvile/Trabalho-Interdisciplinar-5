package simulacao;

import entidades.Carro;
import entidades.Cruzamento;
import entidades.Faixa;
import entidades.Rua;
import entidades.RuaLigacao;
import entidades.Semaforo;
import entidades.ViaEntrada;
import entidades.ViaEntradaComSemaforo;
import entidades.ViaEntradaSemSemaforo;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Rede
{

    public static final double TEMPO_SEGURANCA = 5;

    private Cruzamento[] cruzamentos;
    private ArrayList<ArrayList<RuaLigacao>> ligacoes;
    private ArrayList<Chegada> chegadas;
    private static Rede instance = null;
    private long esperaTotalCarros;
    private long numTotalCarros;
    private double tempoSimulacao;

    private Rede()
    {
        this.cruzamentos = null;
        this.ligacoes = new ArrayList<ArrayList<RuaLigacao>>();
        this.chegadas = new ArrayList<Chegada>();
        this.esperaTotalCarros = 0;
        this.numTotalCarros = 0;
        this.tempoSimulacao = 0;
    }

    public synchronized static Rede getInstance()
    {
        if (instance == null)
        {
            instance = new Rede();
        }
        return instance;
    }

    public Cruzamento[] getCruzamentos()
    {
        return cruzamentos;
    }

    public ArrayList<ArrayList<RuaLigacao>> getLigacoes()
    {
        return this.ligacoes;
    }

    public ArrayList<Chegada> getChegadas()
    {
        return this.chegadas;
    }

    public long getEsperaTotalCarros()
    {
        return this.esperaTotalCarros;
    }

    public long getNumTotalCarros()
    {
        return this.numTotalCarros;
    }
    
    public double getTempoSimulacao()
    {
        return this.tempoSimulacao;
    }

    public void setCruzamentos(Cruzamento[] cruzamentos)
    {
        this.cruzamentos = cruzamentos;
    }

    public void setLigacoes(ArrayList<ArrayList<RuaLigacao>> ligacoes)
    {
        this.ligacoes = ligacoes;
    }

    public void addCruzamento(Cruzamento cruzamento, int posicao)
    {
        this.cruzamentos[posicao] = cruzamento;
    }

    public void addLigacao(RuaLigacao ligacao, int posicao)
    {
        this.ligacoes.get(posicao).add(ligacao);
    }

    public void resetar(ArrayList<Chegada> chegadas)
    {
        this.chegadas.clear();
        this.chegadas = chegadas;
        this.esperaTotalCarros = 0;
        this.numTotalCarros = 0;
        for (Cruzamento c : this.cruzamentos)
        {
            for (Rua rua : c.getRuas())
            {
                if (rua.getViaEntrada() != null)
                {
                    rua.getViaEntrada().setNumeroTotalCarros(0);
                    rua.getViaEntrada().setTempoEsperaTotal(0);
                    if(c.getTemSemaforo() == 1)
                    {
                        ViaEntradaComSemaforo via = (ViaEntradaComSemaforo) rua.getViaEntrada();
                        via.setTempoEntreChegadas(0);
                        via.setTotalChegadas(0);
                        via.setUltimaChegada(0);
                        via.setTempoEntreChegadas(0);
                    }
                    for (Faixa f : rua.getViaEntrada().getFaixas())
                    {
                        f.setUltimaSaida(0);
                    }
                }
            }
        }
    }

    public ArrayList<Chegada> cloneChegadas(ArrayList<Chegada> chegadas)
    {
        ArrayList<Chegada> clonadas = new ArrayList<Chegada>();
        for (Chegada c : chegadas)
        {
            try
            {
                clonadas.add(c.clone());
            } catch (CloneNotSupportedException ex)
            {
                Logger.getLogger(Rede.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return clonadas;
    }

    public void determinarTempoSemaforosAutomatico(ArrayList<int[]> configuracao)
    {
        int cont = 0;
        for (Cruzamento cruzamento : this.cruzamentos)
        {
            if (cruzamento.getTemSemaforo() == 1)
            {
                int total = 0;
                for (int i = 0; i < configuracao.get(cont).length; i++)
                {
                    total += configuracao.get(cont)[i];
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
                        temp.setSemaforo(new Semaforo(configuracao.get(cont)[indexGrupo],
                                total - configuracao.get(cont)[indexGrupo]));
                    }
                    indexGrupo++;
                }
                cont++;
            }
        }
    }

    public void atrasarAberturas(int[] atrasos)
    {
        Rua rua;
        int cont = 0;
        for (Cruzamento cruzamento : this.cruzamentos)
        {
            if (cruzamento.getTemSemaforo() == 1)
            {
                int atraso = atrasos[cont];
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
                cont++;
            }
        }
    }

    public void gerarCarrosIniciaisPorNumero(long numCarros)
    {
        ArrayList<Rua> ruas = getRuasFonte();
        int qtdRuas = ruas.size();
        Chegada[] fila = new Chegada[qtdRuas];

        fila = geraChegadasIniciais(ruas);
        int ultimo, cont;
        Chegada min;
        double horaChegada;
        for (long i = 0; i < numCarros; i++)
        {
            min = fila[0];
            ultimo = 0;
            cont = 0;
            for (Chegada c : fila)
            {
                if (c.getHoraChegada() < min.getHoraChegada())
                {
                    min = c;
                    ultimo = cont;
                }
                cont++;
            }
            horaChegada = 0;
            this.chegadas.add(min);
            horaChegada = min.getHoraChegada() + min.getRua().getViaEntrada().exponencial();
            fila[ultimo] = this.geraChegada(min.getRua(), horaChegada);
        }
        this.tempoSimulacao = this.chegadas.get(this.chegadas.size() - 1).getHoraChegada();
    }

    public ArrayList<Rua> getRuasFonte()
    {
        ArrayList<Rua> ruas = new ArrayList<Rua>();
        for (Cruzamento cruzamento : this.cruzamentos)
        {
            for (Rua rua : cruzamento.getRuas())
            {
                if (rua.getFonte() == true)
                {
                    ruas.add(rua);
                }
            }
        }
        return ruas;
    }

    public Chegada geraChegada(Rua rua, double horaChegada)
    {
        Random rand = new Random();
        ViaEntrada via = rua.getViaEntrada();
        int qtdDirecoes = via.getDirecoes().length;
        double random = (rand.nextDouble()) * 100;
        int direcao = 0;
        double soma = 0;
        for (int i = 0; i < qtdDirecoes; i++)
        {
            soma += via.getProbabilidades()[i];
            if (soma >= random)
            {
                direcao = i;
                break;
            }
        }
        direcao = via.getDirecoes()[direcao];
        double reacao = 4.1;
        double comprimento = 3.4;

        Cruzamento cruzamento = retornaCruzamentoPorRua(rua);
        return new Chegada(new Carro(reacao, comprimento), cruzamento, rua.getCodigo(), horaChegada, direcao);
    }

    public Chegada geraChegada(Rua rua, double horaChegada, Carro carro, int codigo)
    {
        Random rand = new Random();
        ViaEntrada via = rua.getViaEntrada();
        int qtdDirecoes = via.getDirecoes().length;
        double random = (rand.nextDouble()) * 100;
        int direcao = 0;
        double soma = 0;
        for (int i = 0; i < qtdDirecoes; i++)
        {
            soma += via.getProbabilidades()[i];
            if (soma >= random)
            {
                direcao = i;
                break;
            }
        }
        direcao = via.getDirecoes()[direcao];
        double reacao = 4.1;
        double comprimento = 3.4;

        Cruzamento cruzamento = retornaCruzamentoPorRua(rua);
        return new Chegada(carro, cruzamento, rua.getCodigo(), horaChegada, direcao, codigo);
    }

    public Cruzamento retornaCruzamentoPorRua(Rua rua)
    {
        for (Cruzamento cruzamento : this.cruzamentos)
        {
            for (Rua ruaAtual : cruzamento.getRuas())
            {
                if (ruaAtual == rua)
                {
                    return cruzamento;
                }
            }
        }
        return null;
    }

    public void gerarCarrosIniciaisPorTempo(long tempo)
    {
        this.tempoSimulacao = tempo;
        ArrayList<Rua> ruas = getRuasFonte();
        int qtdRuas = ruas.size();
        Chegada[] fila = new Chegada[qtdRuas];

        fila = geraChegadasIniciais(ruas);
        int ultimo, cont;
        Chegada min;
        double horaChegada;
        boolean fim;
        do
        {
            fim = true;
            min = fila[0];
            ultimo = 0;
            cont = 0;
            for (Chegada c : fila)
            {
                if (c.getHoraChegada() < min.getHoraChegada())
                {
                    min = c;
                    ultimo = cont;
                }
                cont++;
            }
            horaChegada = 0;
            if (min.getHoraChegada() <= tempo)
            {
                fim = false;
                this.chegadas.add(min);
                horaChegada = min.getHoraChegada() + min.getRua().getViaEntrada().exponencial();
                fila[ultimo] = this.geraChegada(min.getRua(), horaChegada);
            }
        } while (fim == false);
        /*double t = 0;
        Chegada anterior = null;
        int contador = 0;
        for(Chegada c: this.chegadas)
        {
            if(anterior == null)
            {
                t = c.getHoraChegada();
                anterior = c;
            }
            else
            {
                t += (c.getHoraChegada() - anterior.getHoraChegada());
                anterior = c;
            }
            contador++;
        }
        System.out.println("Tempo entre chegadas: " + (t/contador));*/
    }

    public Chegada[] geraChegadasIniciais(ArrayList<Rua> ruas)
    {
        Chegada[] chegadas = new Chegada[ruas.size()];
        int cont = 0;
        double exp;
        for (Rua rua : ruas)
        {
            exp = rua.getViaEntrada().exponencial();
            Chegada chegada = this.geraChegada(rua, exp);
            chegadas[cont] = chegada;
            cont++;
        }
        return chegadas;
    }

    public int getIndiceCruzamento(int codigo)
    {
        int cont = 0;
        for (Cruzamento c : this.cruzamentos)
        {
            if (codigo == c.getCodigo())
            {
                return cont;
            } else if (c.getTemSemaforo() == 2)
            {
                cont++;
            }
        }
        return cont;
    }

    public void simular()
    {
        while (this.chegadas.size() > 0)
        {
            Chegada chegada = this.chegadas.get(0);
            this.chegadas.remove(0);
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
                if (via instanceof ViaEntradaComSemaforo)
                {
                    ViaEntradaComSemaforo entrada = (ViaEntradaComSemaforo) via;
                    entrada.addTempoEntreChegadas(chegada.getHoraChegada() - entrada.getUltimaChegada());
                    entrada.setUltimaChegada(chegada.getHoraChegada());
                    double tempoEspera = this.calculaEsperaChegada(chegada, entrada, faixaEscolhida);
                    chegada.setHoraSaida(chegada.getHoraChegada() + tempoEspera);
                    chegada.getCarro().aumentaTempoEspera(tempoEspera);
                    //System.out.println("Tempo espera atual: " + tempoEspera + " | Saida: " + chegada.getHoraSaida());
                    //System.out.println("Tempo espera total: " + chegada.getCarro().getTempoEspera());
                    faixaEscolhida.setUltimaSaida(chegada.getHoraSaida() + chegada.getCarro().getComprimento());
                    chegada = processaChegada(chegada);
                    if (chegada != null)
                    {
                        //System.out.println("Nova chegada: " + chegada.getHoraChegada() + " | Cruzamento: " + (chegada.getCruzamento().getCodigo() + 1) + " | Rua: " + chegada.getRua().getCodigo() + " | Direcao: " + chegada.getDirecao());
                        this.insereChegada(chegada);
                    }
                } else if (via instanceof ViaEntradaSemSemaforo)
                {
                    if (chegada.getHoraChegada() < faixaEscolhida.getUltimaSaida())
                    {
                        //System.out.println("Chegada antes da ultima saida.");
                        chegada.getRua().getViaEntrada().aumentaTempoEsperaTotal(faixaEscolhida.getUltimaSaida() - chegada.getHoraChegada());
                        this.atrasarChegada(chegada, faixaEscolhida.getUltimaSaida());
                        chegada.getCarro().aumentaTempoEspera(faixaEscolhida.getUltimaSaida() - chegada.getHoraChegada());
                        this.insereChegada(chegada);
                    } else
                    {
                        //System.out.println("Chegada depois da ultima saida");
                        ViaEntradaSemSemaforo viaSemSemaforo = (ViaEntradaSemSemaforo) via;
                        int prioridadeBase = viaSemSemaforo.getPrioridadeMovimento(chegada.getDirecao());
                        //System.out.println("Prioridade do movimento: " + prioridadeBase);
                        double atraso = 0;
                        //System.out.println("Buscando chegada prioritaria na fila");
                        for (Chegada proxima : this.chegadas)
                        {
                            if (proxima.getHoraChegada() > chegada.getHoraChegada() + TEMPO_SEGURANCA)
                            {
                                //System.out.println("Nada encontrado");
                                break;
                            }
                            if (proxima.getCruzamento().getCodigo() == chegada.getCruzamento().getCodigo())
                            {
                                if (proxima.getRua().getCodigo() != chegada.getRua().getCodigo())
                                {
                                    viaSemSemaforo = (ViaEntradaSemSemaforo) proxima.getRua().getViaEntrada();
                                    int prioridadeAtual = viaSemSemaforo.getPrioridadeMovimento(proxima.getDirecao());
                                    if (prioridadeAtual < prioridadeBase)
                                    {
                                        //System.out.println("Prioridade encontrada: " + prioridadeAtual);
                                        atraso = proxima.getHoraChegada() + proxima.getCarro().getComprimento();
                                    }
                                }
                            }
                        }
                        if (atraso == 0)
                        {
                            //Carro pode passar no momento da chegada
                            //System.out.println("Carro nao foi atrasado");
                            chegada.setHoraSaida(chegada.getHoraChegada());
                            //System.out.println("Tempo espera atual: 0 | Saida: " + chegada.getHoraSaida());
                            //System.out.println("Tempo espera total: " + chegada.getCarro().getTempoEspera());
                            faixaEscolhida.setUltimaSaida(chegada.getHoraSaida() + chegada.getCarro().getComprimento());
                            chegada = processaChegada(chegada);
                            if (chegada != null)
                            {
                                //System.out.println("Nova chegada: " + chegada.getHoraChegada()
                                //        + " | Cruzamento: " + (chegada.getCruzamento().getCodigo() + 1)
                                //        + " | Rua: " + chegada.getRua().getCodigo() + " | Direcao: " + chegada.getDirecao());
                                this.insereChegada(chegada);
                            }
                        } else
                        {
                            //System.out.println("Carro atrasado");
                            //Carro deve ser atrasado
                            this.atrasarChegada(chegada, atraso);
                            faixaEscolhida.setUltimaSaida(atraso);
                            this.insereChegada(chegada);
                        }
                    }
                }
            } else
            {
                System.out.println("Faixa escolhida null");
            }
        }
    }

    public void atrasarChegada(Chegada chegada, double atraso)
    {
        chegada.getCarro().aumentaTempoEspera(atraso - chegada.getHoraChegada());
        chegada.getRua().getViaEntrada().aumentaTempoEsperaTotal(atraso - chegada.getHoraChegada());
        chegada.atrasarChegada(atraso);
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
            if (cruzamento.descobreEstadoSinal(horaChegada, semaforo) == true)
            {
                //Nesse caso, o carro nao precisou parar
                horaSaida = horaChegada;
            } else
            {
                //o carro eh obrigado a parar e eh o primeiro da fila
                horaSaida = horaChegada + chegada.getCarro().getReacao() + (cruzamento.fimVermelho(horaChegada, semaforo));
            }
        } else //o carro chegou antes do anterior sair, ou seja, nao eh o primeiro na fila
        {
            horaSaida = saidaAnterior;
            if (cruzamento.descobreEstadoSinal(horaSaida, semaforo) == false)
            {
                horaSaida = horaSaida + chegada.getCarro().getReacao() + cruzamento.fimVermelho(horaSaida, semaforo);
            }
        }
        return horaSaida - horaChegada;
    }

    public Chegada processaChegada(Chegada chegada)
    {
        Cruzamento origem = chegada.getCruzamento();
        chegada.getRua().getViaEntrada().aumentaNumeroTotalCarros();
        chegada.getRua().getViaEntrada().aumentaTempoEsperaTotal(chegada.getHoraSaida() - chegada.getHoraChegada());
        Rua ruaSaida = origem.retornaRuaPorCodigo(chegada.getDirecao());
        if (ruaSaida.getSaida())
        {
            this.contabilizaCarro(chegada.getCarro());
            //System.out.println("Chegada finalizada");
            return null;
        }
        if (ruaSaida.getViaSaida() != null)
        {
            RuaLigacao ligacao = encontraLigacaoPorOrigem(ruaSaida);
            if (ligacao != null)
            {
                Chegada nova = this.geraChegada(ligacao.getDestino(), chegada.getHoraSaida() + ligacao.getTempoTravessia(), chegada.getCarro(), chegada.getCodigo());
                return nova;
            }
        }
        return null;
    }

    public void contabilizaCarro(Carro carro)
    {
        this.esperaTotalCarros += carro.getTempoEspera();
        this.numTotalCarros++;
    }

    public RuaLigacao encontraLigacaoPorOrigem(Rua origem)
    {
        for (ArrayList<RuaLigacao> ligacoesCruzamento : this.ligacoes)
        {
            for (RuaLigacao ligacao : ligacoesCruzamento)
            {
                if (ligacao.getOrigem() == origem)
                {
                    return ligacao;
                }
            }
        }
        return null;
    }

    public void insereChegada(Chegada chegada)
    {
        int tam = this.chegadas.size();
        if (tam == 0)
        {
            this.chegadas.add(chegada);
            return;
        }
        for (int i = 0; i < tam; i++)
        {
            if (this.chegadas.get(i).getHoraChegada() > chegada.getHoraChegada())
            {
                this.chegadas.add(i, chegada);
                return;
            }
        }
        this.chegadas.add(chegada);
    }
    /*public void insereChegada(Chegada chegada)
     {
     int tam = this.chegadas.size();
     if(tam == 0)
     {
     this.chegadas.add(chegada);
     return;
     }
     int inicio = 0, fim = tam - 1;
     int meio = 0;
     while(inicio < fim && tam > 1)
     {
     meio = (inicio + fim)/2;
     System.out.println("Inicio: "+inicio+"\nFim: "+fim+"\nMeio "+meio);
     if(this.chegadas.get(meio).getHoraChegada() == chegada.getHoraChegada())
     {
     System.out.println("INSERIR NA POSICAO "+meio);
     this.chegadas.add(meio, chegada);
     return;
     }
     if(this.chegadas.get(meio).getHoraChegada() > chegada.getHoraChegada())
     {
     fim = meio - 1;
     }
     else
     {
     inicio = meio + 1;
     }
     }
     if(meio == (tam-1) && this.chegadas.get(meio).getHoraChegada() < chegada.getHoraChegada())
     {
     System.out.println("INSERIR NA POSICAO "+meio);
     this.chegadas.add(chegada);
     }
     else
     {
     System.out.println("INSERIR NA POSICAO "+meio);
     this.chegadas.add(meio, chegada);
     }
     }*/
}
