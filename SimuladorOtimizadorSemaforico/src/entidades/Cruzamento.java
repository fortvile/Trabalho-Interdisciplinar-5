package entidades;

import java.io.IOException;
import simulacao.Movimento;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cruzamento
{

    static int count = 0;
    private int codigo;
    private ArrayList<Movimento> movimentos;
    private ArrayList<ArrayList<Movimento>> grupos;
    private Rua[] ruas;
    private int temSemaforo;
    private int atrasoInicial;

    public Cruzamento(Rua[] ruas, int temSemaforo)
    {
        this.ruas = ruas;
        this.movimentos = new ArrayList<Movimento>();
        this.grupos = new ArrayList<ArrayList<Movimento>>();
        this.codigo = Cruzamento.count;
        this.temSemaforo = temSemaforo;
        this.atrasoInicial = 0;
        Cruzamento.count++;
    }

    public void setAtrasoInicial(int atraso)
    {
        this.atrasoInicial = atraso;
    }

    public int getCodigo()
    {
        return this.codigo;
    }

    public Rua[] getRuas()
    {
        return this.ruas;
    }

    public ArrayList<Movimento> getMovimentos()
    {
        return this.movimentos;
    }

    public ArrayList<ArrayList<Movimento>> getGrupos()
    {
        return this.grupos;
    }

    public int getTemSemaforo()
    {
        return this.temSemaforo;
    }

    public boolean descobreEstadoSinal(double tempo, Semaforo semaforo)
    {
        //System.out.println("Descobre estado no tempo " + tempo);
        if (this.temSemaforo == 1)
        {
            if (tempo < this.atrasoInicial)
            {
                int qtdRuas = this.ruas.length;
                int somatorioVerde = 0;
                double atrasoRelativo = this.atrasoInicial - tempo;
                for (int i = qtdRuas - 1; i >= 0; i--)
                {
                    if (this.ruas[i].getViaEntrada() != null)
                    {
                        ViaEntradaComSemaforo via = (ViaEntradaComSemaforo) this.ruas[i].getViaEntrada();
                        somatorioVerde += via.getSemaforo().getTempoVerde();
                        if (somatorioVerde >= atrasoRelativo)
                        {
                            //System.out.println("Somatorio verde: " + somatorioVerde + " / AR: " + atrasoRelativo);
                            Semaforo aberto = via.getSemaforo();
                            if (semaforo == aberto)
                            {
                                //System.out.println("true");
                                return true;
                            } else
                            {
                                //System.out.println("false");
                                return false;
                            }
                        }
                    }
                }
            } else
            {
                //System.out.println("Passou atraso inicial");
                return semaforo.descobreEstadoSinal(tempo);
            }
        } else
        {
            System.out.println("NAO TEM SEMAFORO");
            try
            {
                System.in.read();
            } catch (IOException ex)
            {
                Logger.getLogger(Cruzamento.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        System.out.println("ERRO AO DESCOBRIR ESTADO");
        try
        {
            System.in.read();
        } catch (IOException ex)
        {
            Logger.getLogger(Cruzamento.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public double fimVermelho(double tempo, Semaforo semaforo)
    {
        //System.out.println("Descobrindo fimvermelho para " + tempo);
        if (this.temSemaforo == 1)
        {
            if (tempo < this.atrasoInicial)
            {
                int qtdRuas = this.ruas.length;
                int somatorioVerde = 0;
                int somatorioRelativo = 0;
                boolean passou = false;
                double atrasoRelativo = this.atrasoInicial - tempo;
                Semaforo aberto = null;
                for (int i = qtdRuas - 1; i >= 0; i--)
                {
                    if (this.ruas[i].getViaEntrada() != null)
                    {
                        ViaEntradaComSemaforo via = (ViaEntradaComSemaforo) this.ruas[i].getViaEntrada();
                        somatorioVerde += via.getSemaforo().getTempoVerde();
                        //System.out.println("SV: " + somatorioVerde);
                        if (somatorioVerde >= atrasoRelativo)
                        {
                            aberto = via.getSemaforo();
                            if (semaforo == aberto)
                            {
                                //System.out.println("Retornou 0");
                                return 0;
                            } else if (passou)
                            {
                                somatorioRelativo += via.getSemaforo().getTempoVerde();
                                //System.out.println("Ja passou, entao retornou " + (somatorioRelativo - somatorioVerde + atrasoRelativo));
                                return somatorioRelativo - (somatorioVerde - atrasoRelativo);
                            }
                            //System.out.println("Nao passou");
                            somatorioRelativo = 0;
                            for (int j = 0; j < qtdRuas; j++)
                            {
                                if (this.ruas[j].getViaEntrada() != null)
                                {
                                    ViaEntradaComSemaforo atual = (ViaEntradaComSemaforo) this.ruas[j].getViaEntrada();
                                    if (atual.getSemaforo() != semaforo)
                                    {
                                        somatorioRelativo += atual.getSemaforo().getTempoVerde();
                                        //System.out.println("SR: " + somatorioRelativo);
                                    } else
                                    {
                                        //System.out.println("Retornou " + (somatorioRelativo + atrasoRelativo));
                                        return somatorioRelativo + atrasoRelativo;
                                    }
                                }
                            }
                        }
                        if (passou)
                        {
                            somatorioRelativo += via.getSemaforo().getTempoVerde();
                        }
                        if (via.getSemaforo() == semaforo && aberto == null)
                        {
                            passou = true;
                        }

                    }
                }
            } else
            {
                return semaforo.fimVermelho(tempo);
            }
        }
        System.out.println("ERRO NO FIM VERMELHO");
        try
        {
            System.in.read();
        } catch (IOException ex)
        {
            Logger.getLogger(Cruzamento.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    public void criaMovimentos()
    {
        for (Rua rua : this.ruas)
        {
            if (rua.getViaEntrada() != null)
            {
                String destinos = rua.destinos();
                String aux = "";
                for (char c : destinos.toCharArray())
                {
                    if (c != ' ')
                    {
                        Rua dest = retornaRuaPorCodigo(Integer.parseInt("" + c));
                        if (dest != null)
                        {
                            aux += dest.getViaSaida().getCodigo() + " ";
                        }
                    }
                }
                rua.addPares(aux);
                for (Movimento movimento : rua.getViaEntrada().getPares())
                {
                    this.movimentos.add(movimento);
                }
            }
        }
        this.setNomes();
    }

    public boolean verificaPares(Movimento A, Movimento B)
    {
        if (A.getOrigem() == B.getOrigem())
        {
            return true;
        }
        Rua rua1 = retornaRuaPorVia(B.getDestino());
        Rua rua2 = retornaRuaPorVia(A.getOrigem());
        Rua rua3 = retornaRuaPorVia(A.getDestino());
        Rua rua4 = retornaRuaPorVia(B.getOrigem());
        if ((rua1.equals(rua2) == false) && (rua3.equals(rua4) == false))
        {
            return false;
        }
        if (verificaParesCruzados(A, B))
        {
            return false;
        }
        if (A.getDestino() == B.getDestino())
        {
            return false;
        }
        return true;
    }

    public boolean verificaParesCruzados(Movimento A, Movimento B)
    {
        if (A.getOrigem() < B.getOrigem() && A.getOrigem() < B.getDestino() && A.getDestino() < B.getOrigem() && A.getDestino() < B.getDestino())
        {
            return false; //se o A for menor que B
        }
        if (A.getOrigem() > B.getOrigem() && A.getOrigem() > B.getDestino() && A.getDestino() > B.getOrigem() && A.getDestino() > B.getDestino())
        {
            return false; //se o A for maior que B
        }
        if (A.getOrigem() < B.getOrigem() && A.getOrigem() < B.getDestino() && A.getDestino() > B.getOrigem() && A.getDestino() > B.getDestino())
        {
            return false;
        }
        if (A.getDestino() < B.getOrigem() && A.getDestino() < B.getDestino() && A.getOrigem() > B.getOrigem() && A.getOrigem() > B.getDestino())
        {
            return false;
        }
        if (B.getOrigem() < A.getOrigem() && B.getOrigem() < A.getDestino() && B.getDestino() > A.getOrigem() && B.getDestino() > A.getDestino())
        {
            return false;
        }
        if (B.getDestino() < A.getOrigem() && B.getDestino() < A.getDestino() && B.getOrigem() > A.getOrigem() && B.getOrigem() > A.getDestino())
        {
            return false;
        }
        return true;
    }

    public void agruparPorOrigem()
    {
        int tam = movimentos.size();
        int cont = -1;
        for (int i = 0; i < tam - 1; i++)
        {
            cont++;
            grupos.add(new ArrayList<Movimento>());
            grupos.get(cont).add(movimentos.get(i));
            for (int j = i + 1; j < tam; j++)
            {
                if (i > 0 && (grupos.get(cont).get(0).getOrigem() == grupos.get(cont - 1).get(0).getOrigem()))
                {
                    grupos.remove(cont);
                    cont--;
                    break;
                }
                if (movimentos.get(i).getOrigem() == movimentos.get(j).getOrigem())
                {
                    grupos.get(cont).add(movimentos.get(j));
                }
            }
        }
        cont++;
        grupos.add(new ArrayList<Movimento>());
        grupos.get(cont).add(movimentos.get(tam - 1));
        if ((tam - 1 > 0) && (grupos.get(cont).get(0).getOrigem() == grupos.get(cont - 1).get(0).getOrigem()))
        {
            grupos.remove(cont);
            cont--;
        }
    }

    public void criaGrupos()
    {
        agruparPorOrigem();
        ordenaGrupos();
        criaConjuntosIndependentes();
    }

    public void juntaGrupos(ArrayList<Movimento> grupo1, ArrayList<Movimento> grupo2)
    {
        for (int k = 0; k < grupo2.size(); k++)
        {
            grupo1.add(grupo2.get(k));
        }
    }

    public void criaConjuntosIndependentes()
    {
        int j;
        for (int i = 0; i < grupos.size() - 1; i++)
        {
            j = i + 1;
            while (j < grupos.size())
            {
                if (isIndependente(grupos.get(i), grupos.get(j)))
                {
                    juntaGrupos(grupos.get(i), grupos.get(j));
                    grupos.remove(j);
                    j--;
                }
                j++;
            }
        }
    }

    public boolean isIndependente(ArrayList<Movimento> a, ArrayList<Movimento> b)
    {
        for (int i = 0; i < a.size(); i++)
        {
            for (int j = 0; j < b.size(); j++)
            {
                if (verificaPares(a.get(i), b.get(j)) == false)
                {
                    return false;
                }
            }
        }
        return true;
    }

    public void ordenaGrupos()
    {
        boolean trocou = false;
        do
        {
            trocou = false;
            for (int i = 0; i < (grupos.size() - 1); i++)
            {
                if (grupos.get(i).size() > grupos.get(i + 1).size())
                {
                    ArrayList<Movimento> aux = grupos.get(i);
                    grupos.remove(i);
                    grupos.add(i + 1, aux);
                    trocou = true;
                }
            }
        } while (trocou);
    }
    
    public int getNumViasEntrada()
    {
        int cont = 0;
        for(Rua r: this.ruas)
        {
            if(r.getViaEntrada() != null) cont++;
        }
        return cont;
    }

    public ArrayList<Movimento> retornaGrupoPorRua(int codigo)
    {
        for (ArrayList<Movimento> grupo : this.grupos)
        {
            for (Movimento movimento : grupo)
            {
                Rua origem = this.retornaRuaPorMovimento(movimento);
                if (origem.getCodigo() == codigo)
                {
                    return grupo;
                }
            }
        }
        return null;
    }

    public Rua retornaRuaPorCodigo(int codigo)
    {
        for (Rua rua : ruas)
        {
            if (rua.getCodigo() == codigo)
            {
                return rua;
            }
        }
        return null;
    }

    public Rua retornaRuaPorVia(int codigo)
    {
        for (Rua rua : ruas)
        {
            if (rua.getViaEntrada() != null)
            {
                if (rua.getViaEntrada().getCodigo() == codigo)
                {
                    return rua;
                }
            }
            if (rua.getViaSaida() != null)
            {
                if (rua.getViaSaida().getCodigo() == codigo)
                {
                    return rua;
                }
            }
        }
        return null;
    }

    public Rua retornaRuaPorMovimento(Movimento movimento)
    {
        for (Rua rua : ruas)
        {
            if (rua.getViaEntrada() != null)
            {
                for (Movimento atual : rua.getViaEntrada().getPares())
                {
                    if (atual.equals(movimento))
                    {
                        return rua;
                    }
                }
            }
        }
        return null;
    }

    public void setNomes()
    {
        int nome = 65;
        for (Rua rua : ruas)
        {
            if (rua.getViaEntrada() != null)
            {
                for (Movimento movimento : rua.getViaEntrada().getPares())
                {
                    movimento.setNome((char) nome);
                    nome++;
                }
            }
        }
    }

    public void imprimeInformacoes()
    {
        System.out.println("Cruzamento " + this.codigo + ":");
        for (Rua rua : this.ruas)
        {
            System.out.println("Codigo: " + rua.getCodigo());
            if (rua.getViaEntrada() != null)
            {
                System.out.println("\t" + rua.getViaEntrada().detalhesPares());
            }
        }
    }
}
