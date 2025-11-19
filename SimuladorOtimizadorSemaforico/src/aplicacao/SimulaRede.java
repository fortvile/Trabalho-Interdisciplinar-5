package aplicacao;

import java.util.ArrayList;
import java.util.Scanner;

import simulacao.Chegada;
import entidades.Cruzamento;
import entidades.Faixa;
import simulacao.Movimento;
import simulacao.Rede;
import entidades.Rua;
import entidades.RuaLigacao;
import entidades.Semaforo;
import entidades.ViaEntradaComSemaforo;
import entidades.ViaEntradaSemSemaforo;
import entidades.ViaSaida;
import otimizacao.Contexto;
import otimizacao.GeneticoConcreto1;
import otimizacao.GeneticoConcreto3;
import otimizacao.GeneticoConcretoCruzamento;
import otimizacao.IndividuoCruzamento;
import otimizacao.IndividuoGenetico;
import otimizacao.OtimizadorConcreto1;

public class SimulaRede
{

    private static Scanner leitura = new Scanner(System.in);

    public static void main(String[] args)
    {
        menu();
        leitura.close();
    }

    public static void menu()
    {
        int op;
        do
        {
            System.out.println("*******************************");
            System.out.println("1) Criar rede de trafego");
            System.out.println("2) Gerar chegadas");
            System.out.println("3) Simular");
            System.out.println("4) Otimizar");
            System.out.println("9) Imprimir informacoes");
            System.out.println("0) Sair");
            System.out.println("*******************************");
            System.out.print("Entre com a opcao: ");
            op = leitura.nextInt();
            switch (op)
            {
                case 1:
                    criaRede();
                    determinarAgrupamentos();
                    break;
                case 2:
                    gerarChegadas();
                    break;
                case 3:
                    simular();
                    break;
                case 4:
                    otimizar();
                    break;
                case 9:
                    imprimirInformacoes();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcaoo invalida");
            }
        } while (op != 0);
    }

    public static void criaRede()
    {
        System.out.print("Criar manualmente (1) ou por arquivo(2)?: ");
        int op = leitura.nextInt();
        if (op == 1)
        {
            criaRedeManual();
        } else if (op == 2)
        {
            criaRedeArquivo();
        } else
        {
            System.out.println("Opcao invalida");
        }
    }

    public static void criaRedeArquivo()
    {
        System.out.print("Informe o nome do arquivo:");
        String nome = leitura.nextLine();
        nome = leitura.nextLine();
        LeituraArquivo arquivo = new LeituraArquivo(nome);
        arquivo.gerarRede();
    }

    public static void criaRedeManual()
    {
        Rede rede = Rede.getInstance();

        System.out.print("Entre com o numero de cruzamentos: ");
        int qtdCruzamentos = leitura.nextInt();
        rede.setCruzamentos(new Cruzamento[qtdCruzamentos]);
        for (int i = 0; i < qtdCruzamentos; i++)
        {
            System.out.println("\nCriacao do " + (i + 1) + "o cruzamento");
            Cruzamento cruzamento = criaCruzamento();
            rede.addCruzamento(cruzamento, i);
        }

        int qtdLigacoes;
        for (int i = 0; i < qtdCruzamentos; i++)
        {
            rede.getLigacoes().add(new ArrayList<RuaLigacao>());
            System.out.println("\nNumero de ligacoes a partir do cruzamento " + (i + 1) + ": ");
            qtdLigacoes = leitura.nextInt();
            for (int j = 0; j < qtdLigacoes; j++)
            {
                RuaLigacao ligacao = criarLigacao(i);
                rede.addLigacao(ligacao, i);
            }
        }

    }

    public static Cruzamento criaCruzamento()
    {
        System.out.print("O cruzamento tem semaforo? (1-Sim, 2-Nao): ");
        int temSemaforo = leitura.nextInt();
        System.out.print("Numero de ruas do cruzamento: ");
        int numeroRuas = leitura.nextInt();
        int temEntrada = 0;
        int temSaida = 0;
        Integer codigo1 = 0, codigo2 = 0, codigo3 = 1;
        Rua[] ruas = new Rua[numeroRuas];
        for (int i = 0; i < numeroRuas; i++)
        {
            System.out.print("Tem entrada a rua " + (i + 1) + "? (1 - Sim, 2 - Nao): ");
            temEntrada = leitura.nextInt();
            System.out.print("Tem saida a rua " + (i + 1) + "? (1 - Sim, 2 - Nao): ");
            temSaida = leitura.nextInt();
            if (temEntrada == 1)
            {
                codigo1 = codigo3++;
            } else
            {
                codigo1 = null;
            }
            if (temSaida == 1)
            {
                codigo2 = codigo3++;
            } else
            {
                codigo2 = null;
            }
            ruas[i] = criarRua(i + 1, codigo1, codigo2, temSemaforo);
        }
        Cruzamento cruzamento = new Cruzamento(ruas, temSemaforo);
        cruzamento.criaMovimentos();
        return cruzamento;
    }

    public static Rua criarRua(int codigo, Integer codigoEntrada, Integer codigoSaida, int temSemaforo)
    {
        if (codigoEntrada != null)
        {
            System.out.println("Entre com o peso desta rua: ");
            int peso = leitura.nextInt();
            System.out.print("Entre com o numero de direcoes possiveis dessa rua: ");
            int qtdDirecoes = leitura.nextInt();
            ArrayList<int[]> prioridades = new ArrayList<int[]>();
            int direcoes[] = new int[qtdDirecoes];
            double probabilidade[] = new double[qtdDirecoes];
            for (int i = 0; i < qtdDirecoes; i++)
            {
                System.out.print("Entre com a " + (i + 1) + "a direcao: ");
                direcoes[i] = leitura.nextInt();
                System.out.print("Entre com a probabilidade de seguir esta direcao: ");
                probabilidade[i] = leitura.nextDouble();
                if (temSemaforo == 2)
                {
                    int[] prioridade = new int[2];
                    System.out.print("Entre com a prioridade de movimento para esta direcao: ");
                    prioridade[0] = direcoes[i];
                    prioridade[1] = leitura.nextInt();
                    prioridades.add(prioridade);
                }
            }
            System.out.print("Numero de faixas da rua " + codigo + ": ");
            int numeroFaixas = leitura.nextInt();
            Faixa[] faixas = new Faixa[numeroFaixas];
            for (int i = 0; i < numeroFaixas; i++)
            {
                faixas[i] = criarFaixa(direcoes);
            }
            double mediaCarros;
            System.out.print("Esta rua eh fonte de entrada para a rede? (1 - Sim, 2 - Nao): ");
            int isFonte = leitura.nextInt();
            int isSaida = 0;
            if (codigoSaida != null)
            {
                System.out.print("Esta rua eh de saida para a rede? (1 - Sim, 2 - Nao): ");
                isSaida = leitura.nextInt();
            }
            if (isFonte == 1)
            {
                System.out.println("Media de tempo de chegada entre os carros da rua: ");
                mediaCarros = leitura.nextDouble();
            } else
            {
                mediaCarros = 0;
            }
            boolean isF, isS;
            isF = isFonte == 1;
            isS = isSaida == 1;
            if (temSemaforo == 1)
            {
                ViaSaida viaSaida = null;
                ViaEntradaComSemaforo viaEntrada = new ViaEntradaComSemaforo(codigoEntrada,
                        numeroFaixas, faixas, mediaCarros, direcoes, probabilidade, peso);
                if (codigoSaida != null)
                {
                    viaSaida = new ViaSaida(codigoSaida);
                }
                return new Rua(codigo, viaEntrada, viaSaida, isF, isS);
            } else
            {
                ViaSaida viaSaida = null;
                ViaEntradaSemSemaforo viaEntrada = new ViaEntradaSemSemaforo(codigoEntrada,
                        numeroFaixas, faixas, mediaCarros, direcoes, probabilidade, prioridades, peso);
                if (codigoSaida != null)
                {
                    viaSaida = new ViaSaida(codigoSaida);
                }
                return new Rua(codigo, viaEntrada, viaSaida, isF, isS);
            }
        } else
        {
            int isSaida = 0;
            if (codigoSaida != null)
            {
                System.out.print("Esta rua e de saida para a rede? (1 - Sim, 2 - Nao): ");
                isSaida = leitura.nextInt();
            }
            boolean isS;
            isS = isSaida == 1;
            ViaSaida viaSaida = null;
            if (codigoSaida != null)
            {
                viaSaida = new ViaSaida(codigoSaida);
            }
            return new Rua(codigo, null, viaSaida, false, isS);
        }
    }

    public static Faixa criarFaixa(int[] direcoes)
    {
        System.out.print("Numero de direcoes que a faixa pode seguir: ");
        int qtdDirecoes = leitura.nextInt();
        int direcoesFaixa[] = new int[qtdDirecoes];
        boolean existe;
        for (int i = 0; i < qtdDirecoes; i++)
        {
            int direcao;
            do
            {
                System.out.print("Entre com a " + (i + 1) + "a direcao: ");
                direcao = leitura.nextInt();
                existe = false;
                for (int d : direcoes)
                {
                    if (d == direcao)
                    {
                        existe = true;
                        break;
                    }
                }
                if (existe == false)
                {
                    System.out.println("Direcao invalida");
                }
            } while (existe == false);
            direcoesFaixa[i] = direcao;
        }
        return new Faixa(direcoesFaixa);
    }

    public static RuaLigacao criarLigacao(int cruzamentoOrigem)
    {
        Rede rede = Rede.getInstance();
        int qtdCruzamentos = rede.getCruzamentos().length;
        int qtdRuas;

        int ruaSaida;
        qtdRuas = rede.getCruzamentos()[cruzamentoOrigem].getRuas().length;
        do
        {
            System.out.print("Entre com a rua de saida do cruzamento de origem: ");
            ruaSaida = leitura.nextInt();
        } while (ruaSaida > qtdRuas || ruaSaida < 1 || rede.getCruzamentos()[cruzamentoOrigem].getRuas()[ruaSaida - 1].getViaSaida() == null);

        int cruzamentoDestino;
        do
        {
            System.out.print("Entre com o codigo do cruzamento de destino: ");
            cruzamentoDestino = leitura.nextInt();
        } while (cruzamentoDestino > qtdCruzamentos || cruzamentoDestino < 1);

        int ruaEntrada;
        qtdRuas = rede.getCruzamentos()[cruzamentoDestino - 1].getRuas().length;
        do
        {
            System.out.print("Entre com a rua de entrada do cruzamento de destino: ");
            ruaEntrada = leitura.nextInt();
        } while (ruaEntrada > qtdRuas || ruaEntrada < 1 || rede.getCruzamentos()[cruzamentoDestino - 1].getRuas()[ruaEntrada - 1].getViaEntrada() == null);

        Rua origem = rede.getCruzamentos()[cruzamentoOrigem].getRuas()[ruaSaida - 1];
        Rua destino = rede.getCruzamentos()[cruzamentoDestino - 1].getRuas()[ruaEntrada - 1];

        double tempoTravessia;
        System.out.print("Entre com o tempo de travessia para o cruzamento: ");
        tempoTravessia = leitura.nextDouble();
        RuaLigacao ligacao = new RuaLigacao(origem, destino, tempoTravessia);
        return ligacao;
    }

    public static void gerarChegadas()
    {
        Rede rede = Rede.getInstance();
        System.out.print("Gerar carros por tempo (1) ou por numero (2)? ");
        int op = leitura.nextInt();
        if (op == 1)
        {
            System.out.print("Entre com o tempo de simula��o: ");
            long tempo = leitura.nextLong();
            rede.gerarCarrosIniciaisPorTempo(tempo);
        } else if (op == 2)
        {
            System.out.print("Entre com o numero de carros gerados: ");
            long numCarros = leitura.nextLong();
            rede.gerarCarrosIniciaisPorNumero(numCarros);
        } else
        {
            System.out.println("Opcao invalida");
        }

        System.out.print("Imprimir chegadas? (1 - Sim, 2 - Nao): ");
        op = leitura.nextInt();
        if (op == 1)
        {
            for (Chegada c : rede.getChegadas())
            {
                System.out.println("\t Cruzamento " + (c.getCruzamento().getCodigo()+1) 
                        + " | Rua " + c.getRua().getCodigo() + " | Chegada: " + c.getHoraChegada());
            }
        }
    }

    public static void simular()
    {
        determinarTempoSemaforos();
        atrasarAberturas();
        Rede rede = Rede.getInstance();
        rede.simular();
        double mediaVias = 0;
        int cont = 0;
        System.out.println("\n\n");
        System.out.println("Tempos entre chegadas");
        for(Cruzamento c: rede.getCruzamentos())
        {
            if(c.getTemSemaforo() == 1)
            {
                for(Rua r: c.getRuas())
                {
                    if(r.getViaEntrada() != null)
                    {
                        ViaEntradaComSemaforo via = (ViaEntradaComSemaforo) r.getViaEntrada();
                        System.out.println("Cruzamento " + (c.getCodigo()+1) 
                                + " Rua " + r.getCodigo() 
                                + " Media " + via.getMediaEntreChegadas());
                    }
                }
            }
        }
        System.out.println("\n\n");
        for(Cruzamento c: rede.getCruzamentos())
        {
            System.out.println("Medias Cruzamento " + (c.getCodigo()+1));
            for(Rua r: c.getRuas())
            {
                if(r.getViaEntrada() != null)
                {
                    double mediaAtual = r.getViaEntrada().getTempoEsperaTotal()/r.getViaEntrada().getNumeroTotalCarros(); 
                    mediaVias += mediaAtual;
                    System.out.println("Rua " + r.getCodigo() + " Total carros: " + r.getViaEntrada().getNumeroTotalCarros() + ": " + mediaAtual);
                    cont++;
                }
            }
        }
        mediaVias /= cont;
        double media = ((double) rede.getEsperaTotalCarros()) / ((double) rede.getNumTotalCarros());
        System.out.println("Espera media vias: " + mediaVias);
        System.out.println("Espera media carros: " + media);
    }

    public static void otimizar()
    {
        ArrayList<Chegada> chegadas = Rede.getInstance().cloneChegadas(Rede.getInstance().getChegadas());
        Contexto contexto = new Contexto();
        contexto.setAlgoritmo(new OtimizadorConcreto1(chegadas));
        IndividuoGenetico melhor = contexto.otimizarGenetico(null);
        System.out.println("MELHOR SOLUCAO FINAL: ");
        System.out.println(melhor.toString());
        
        /*double[] teste = {8,12};
        System.out.println("Tempo simulacao: " + Rede.getInstance().getTempoSimulacao());
        contexto.setAlgoritmo(new GeneticoConcretoCruzamento(Rede.getInstance().getCruzamentos()[0], teste, Rede.getInstance().getTempoSimulacao()));
        IndividuoCruzamento melhor = contexto.otimizarCruzamento(null);
        System.out.println("MELHOR SOLUCAO FINAL: ");
        System.out.println(melhor.toString());*/
        
        /*contexto.setAlgoritmo(new GeneticoConcreto1(chegadas));
        IndividuoGenetico melhor = contexto.otimizarGenetico(null);
        System.out.println("MELHOR SOLUCAO INICIAL: ");
        System.out.println(melhor.toString());
        contexto.setAlgoritmo(new GeneticoConcreto3(chegadas));
        melhor = contexto.otimizarGenetico(melhor);
        System.out.println("MELHOR SOLUCAO FINAL: ");
        System.out.println(melhor.toString());*/
    }

    public static void determinarAgrupamentos()
    {
        Rede rede = Rede.getInstance();
        for (Cruzamento cruzamento : rede.getCruzamentos())
        {
            cruzamento.agruparPorOrigem();
            if (cruzamento.getTemSemaforo() == 1)
            {
                System.out.println("\n\nCruzamento " + cruzamento.getCodigo());
                int op;
                System.out.print("Deseja agrupar? (1-Sim, 2-Nao): ");
                op = leitura.nextInt();
                //cruzamento.agruparPorOrigem();
                if (op == 1)
                {
                    do
                    {
                        int r1, r2;
                        System.out.println("Entre com o codigo das duas ruas a serem agrupadas");
                        System.out.print("Primeira rua: ");
                        r1 = leitura.nextInt();
                        System.out.print("Segunda rua: ");
                        r2 = leitura.nextInt();
                        Rua rua1 = cruzamento.retornaRuaPorCodigo(r1);
                        Rua rua2 = cruzamento.retornaRuaPorCodigo(r2);
                        if (rua1 != null && rua2 != null)
                        {
                            ArrayList<Movimento> grupo1 = cruzamento.retornaGrupoPorRua(rua1.getCodigo());
                            ArrayList<Movimento> grupo2 = cruzamento.retornaGrupoPorRua(rua2.getCodigo());
                            if (cruzamento.isIndependente(grupo1, grupo2))
                            {
                                if (grupo1 != null && grupo2 != null)
                                {
                                    cruzamento.juntaGrupos(grupo1, grupo2);
                                    cruzamento.getGrupos().remove(grupo2);
                                    System.out.println("Agrupamento realizado");
                                } else
                                {
                                    System.out.println("Grupo null");
                                }
                            } else
                            {
                                System.out.println("Grupos nao independentes");
                            }
                        } else
                        {
                            System.out.println("Rua nao encontrada");
                        }
                        System.out.print("Deseja fazer outro agrupamento? (1-Sim, 2-Nao): ");
                        op = leitura.nextInt();
                    } while (op == 1);
                }
            }
        }
    }

    public static void determinarTempoSemaforos()
    {
        //TODO adaptar para utilizar o metodo da classe Rede
        Rede rede = Rede.getInstance();
        for (Cruzamento cruzamento : rede.getCruzamentos())
        {
            if (cruzamento.getTemSemaforo() == 1)
            {
                System.out.println("\n\nCruzamento " + (cruzamento.getCodigo() + 1) + ": ");
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
                    int verde, vermelho;
                    System.out.println("Grupo da rua " + ruas.get(0).getCodigo());
                    System.out.print("Tempo verde: ");
                    verde = leitura.nextInt();
                    System.out.print("Tempo vermelho: ");
                    vermelho = leitura.nextInt();
                    for (Rua rua : ruas)
                    {
                        ViaEntradaComSemaforo temp = (ViaEntradaComSemaforo) rua.getViaEntrada();
                        temp.setSemaforo(new Semaforo(verde, vermelho));
                    }
                }
            }
        }
    }

    public static void atrasarAberturas()
    {
        //TODO adaptar para utilizar o metodo da classe Rede
        Rede rede = Rede.getInstance();
        int atraso = 0;
        Rua rua;
        for (Cruzamento cruzamento : rede.getCruzamentos())
        {
            if (cruzamento.getTemSemaforo() == 1)
            {
                System.out.println("Entre com o atraso do cruzamento " + cruzamento.getCodigo()+1);
                atraso = leitura.nextInt();
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
        }
    }

    public static void imprimirInformacoes()
    {
        Rede rede = Rede.getInstance();
        System.out.println("\nCruzamentos:");
        for (Cruzamento cruzamento : rede.getCruzamentos())
        {
            System.out.println("\nCruzamento " + (cruzamento.getCodigo() + 1) + ": ");
            if (cruzamento.getTemSemaforo() == 1)
            {
                System.out.println("Tem semaforo");
            } else
            {
                System.out.println("Nao tem semaforo");
            }
            System.out.println("\tGrupos: ");
            for (ArrayList<Movimento> movimentos : cruzamento.getGrupos())
            {
                System.out.print("\t\t{");
                for (Movimento m : movimentos)
                {
                    System.out.print("(" + m.getOrigem() + "," + m.getDestino() + "), ");
                }
                System.out.println("}");
            }
            System.out.println("\tRuas:");
            for (Rua rua : cruzamento.getRuas())
            {
                System.out.println("\t\tRua: " + rua.getCodigo());

                if (rua.getFonte())
                {
                    System.out.println("\t\tEh rua fonte");
                } else
                {
                    System.out.println("\t\tNao eh rua fonte");
                }

                if (rua.getSaida())
                {
                    System.out.println("\t\tEh rua de sa�da");
                } else
                {
                    System.out.println("\t\tNao eh rua de saida");
                }

                if (rua.getViaEntrada() == null)
                {
                    System.out.println("\t\tNao tem via de entrada");
                } else
                {
                    System.out.println("\t\tVia de entrada:");
                    /*if (cruzamento.getTemSemaforo() == 1)
                     {
                     ViaEntradaComSemaforo via = (ViaEntradaComSemaforo) rua.getViaEntrada();
                     System.out.println("\t\t\tTempo verde: " + via.getSemaforo().getTempoVerde());
                     System.out.println("\t\t\tTempo vermelho: " + via.getSemaforo().getTempoVermelho());
                     System.out.println("\t\t\tAtraso: " + via.getSemaforo().getAtraso());
                     }*/
                    if (cruzamento.getTemSemaforo() == 2)
                    {
                        ViaEntradaSemSemaforo semSemaforo = (ViaEntradaSemSemaforo) rua.getViaEntrada();
                        for (int[] p : semSemaforo.getPrioridades())
                        {
                            System.out.println(p[0] + ":" + p[1]);
                        }
                    }
                    System.out.println("\t\t\tM�dia entre chegadas: " + rua.getViaEntrada().getMediaCarros());
                    for (Faixa f : rua.getViaEntrada().getFaixas())
                    {
                        System.out.print("\t\t\tDirecoes de faixa: ");
                        for (int dir : f.getDirecoes())
                        {
                            System.out.print(dir + " ");
                        }
                    }
                }

                if (rua.getViaSaida() == null)
                {
                    System.out.println("\t\tNao tem via de saida");
                }
            }
            System.out.println("\tPares de movimentos:");
            for (Movimento movimento : cruzamento.getMovimentos())
            {
                System.out.println("\t\t" + movimento.getNome() + "=(" + movimento.getOrigem() + "," + movimento.getDestino() + ")");
            }
        }

        System.out.println("\nLigacoes:");
        int cont = 1;
        for (ArrayList<RuaLigacao> ligacoes : rede.getLigacoes())
        {
            System.out.println("\tLigacoes a partir do cruzamento " + cont + ": ");
            for (RuaLigacao ligacao : ligacoes)
            {
                System.out.println("\t\t" + ligacao.getOrigem().getCodigo()
                        + " -> " + (rede.retornaCruzamentoPorRua(ligacao.getDestino()).getCodigo() + 1)
                        + "/" + ligacao.getDestino().getCodigo()
                        + " = " + ligacao.getTempoTravessia());
            }
            cont++;
        }
    }

}
