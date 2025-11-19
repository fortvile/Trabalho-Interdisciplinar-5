package aplicacao;

import entidades.Cruzamento;
import entidades.Faixa;
import entidades.Rua;
import entidades.RuaLigacao;
import entidades.ViaEntradaComSemaforo;
import entidades.ViaEntradaSemSemaforo;
import entidades.ViaSaida;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import simulacao.Rede;

public class LeituraArquivo
{

    private String nome;

    public LeituraArquivo(String nome)
    {
        this.nome = nome;
    }

    public void gerarRede()
    {
        Rede rede = Rede.getInstance();
        try
        {
            FileReader arq = new FileReader(this.nome);
            BufferedReader leitura = new BufferedReader(arq);

            int numCruzamentos = Integer.parseInt(leitura.readLine());
            rede.setCruzamentos(new Cruzamento[numCruzamentos]);
            for (int i = 0; i < numCruzamentos; i++)
            {
                Cruzamento cruzamento = criaCruzamento(leitura);
                rede.addCruzamento(cruzamento, i);
            }

            int qtdLigacoes;
            for (int i = 0; i < numCruzamentos; i++)
            {
                rede.getLigacoes().add(new ArrayList<RuaLigacao>());
                qtdLigacoes = Integer.parseInt(leitura.readLine());
                for (int j = 0; j < qtdLigacoes; j++)
                {
                    RuaLigacao ligacao = criarLigacao(i, leitura);
                    rede.addLigacao(ligacao, i);
                }
            }
            
             arq.close();
        } catch (FileNotFoundException e)
        {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
        } catch (IOException ex)
        {
            Logger.getLogger(LeituraArquivo.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private Cruzamento criaCruzamento(BufferedReader leitura)
    {
        int temSemaforo;
        try
        {
            temSemaforo = Integer.parseInt(leitura.readLine());
            int numeroRuas = Integer.parseInt(leitura.readLine());
            int temEntrada = 0;
            int temSaida = 0;
            Integer codigo1 = 0, codigo2 = 0, codigo3 = 1;
            Rua[] ruas = new Rua[numeroRuas];
            for (int i = 0; i < numeroRuas; i++)
            {
                temEntrada = Integer.parseInt(leitura.readLine());
                temSaida = Integer.parseInt(leitura.readLine());
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
                ruas[i] = criarRua(i + 1, codigo1, codigo2, temSemaforo, leitura);
            }
            Cruzamento cruzamento = new Cruzamento(ruas, temSemaforo);
            cruzamento.criaMovimentos();
            return cruzamento;
        } catch (IOException ex)
        {
            Logger.getLogger(LeituraArquivo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Rua criarRua(int codigo, Integer codigoEntrada, Integer codigoSaida, int temSemaforo, BufferedReader leitura)
    {
        try
        {
            if (codigoEntrada != null)
            {
                int peso = Integer.parseInt(leitura.readLine());
                int qtdDirecoes = Integer.parseInt(leitura.readLine());
                
                ArrayList<int[]> prioridades = new ArrayList<int[]>();
                int direcoes[] = new int[qtdDirecoes];
                double probabilidade[] = new double[qtdDirecoes];
                for (int i = 0; i < qtdDirecoes; i++)
                {
                    direcoes[i] = Integer.parseInt(leitura.readLine());
                    probabilidade[i] = Integer.parseInt(leitura.readLine());
                    if (temSemaforo == 2)
                    {
                        int[] prioridade = new int[2];
                        prioridade[0] = direcoes[i];
                        prioridade[1] = Integer.parseInt(leitura.readLine());
                        prioridades.add(prioridade);
                    }
                }
                int numeroFaixas = Integer.parseInt(leitura.readLine());
                Faixa[] faixas = new Faixa[numeroFaixas];
                for (int i = 0; i < numeroFaixas; i++)
                {
                    faixas[i] = criarFaixa(direcoes, leitura);
                }
                double mediaCarros;
                int isFonte = Integer.parseInt(leitura.readLine());
                int isSaida = 0;
                if (codigoSaida != null)
                {
                    isSaida = Integer.parseInt(leitura.readLine());
                }
                if (isFonte == 1)
                {
                    mediaCarros = Double.parseDouble(leitura.readLine());
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
                    isSaida = Integer.parseInt(leitura.readLine());
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
        } catch (IOException ex)
        {
            Logger.getLogger(LeituraArquivo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Faixa criarFaixa(int[] direcoes, BufferedReader leitura)
    {
        try
        {
            int qtdDirecoes = Integer.parseInt(leitura.readLine());
            int direcoesFaixa[] = new int[qtdDirecoes];
            for (int i = 0; i < qtdDirecoes; i++)
            {
                int direcao;

                direcao = Integer.parseInt(leitura.readLine());

                direcoesFaixa[i] = direcao;
            }
            return new Faixa(direcoesFaixa);
        } catch (IOException ex)
        {
            Logger.getLogger(LeituraArquivo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private RuaLigacao criarLigacao(int cruzamentoOrigem, BufferedReader leitura)
    {
        try
        {
            Rede rede = Rede.getInstance();
            int qtdCruzamentos = rede.getCruzamentos().length;
            int qtdRuas;

            int ruaSaida;
            qtdRuas = rede.getCruzamentos()[cruzamentoOrigem].getRuas().length;

            ruaSaida = Integer.parseInt(leitura.readLine());

            int cruzamentoDestino = Integer.parseInt(leitura.readLine());

            int ruaEntrada;
            qtdRuas = rede.getCruzamentos()[cruzamentoDestino - 1].getRuas().length;
            ruaEntrada = Integer.parseInt(leitura.readLine());

            Rua origem = rede.getCruzamentos()[cruzamentoOrigem].getRuas()[ruaSaida - 1];
            Rua destino = rede.getCruzamentos()[cruzamentoDestino - 1].getRuas()[ruaEntrada - 1];

            double tempoTravessia;
            tempoTravessia = Double.parseDouble(leitura.readLine());
            RuaLigacao ligacao = new RuaLigacao(origem, destino, tempoTravessia);
            return ligacao;
        } catch (IOException ex)
        {
            Logger.getLogger(LeituraArquivo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
