package simulacao;

import entidades.Carro;
import entidades.Cruzamento;
import entidades.Rua;
import java.util.ArrayList;

public class Chegada implements Cloneable
{
    private static int count = 0;
    private int codigo;
    private Carro carro;
    private Cruzamento cruzamento;
    private int rua;
    private double horaChegada;
    private double horaSaida;
    private int direcao;

    public Chegada(Carro carro, Cruzamento cruzamento, int rua, double horaChegada, int direcao)
    {
        this.carro = carro;
        this.cruzamento = cruzamento;
        this.rua = rua;
        this.horaChegada = horaChegada;
        this.horaSaida = 0;
        this.direcao = direcao;
        this.codigo = count++;
    }
    
    public Chegada(Carro carro, Cruzamento cruzamento, int rua, double horaChegada, int direcao, int codigo)
    {
        this.carro = carro;
        this.cruzamento = cruzamento;
        this.rua = rua;
        this.horaChegada = horaChegada;
        this.horaSaida = 0;
        this.direcao = direcao;
        this.codigo = codigo;
    }

    public Cruzamento getCruzamento()
    {
        return this.cruzamento;
    }

    public Carro getCarro()
    {
        return this.carro;
    }
    
    public int getCodigo()
    {
        return this.codigo;
    }

    public Rua getRua()
    {
        for (Rua rua : this.cruzamento.getRuas())
        {
            if (rua.getCodigo() == this.rua)
            {
                return rua;
            }
        }
        return null;
    }

    public double getHoraChegada()
    {
        return this.horaChegada;
    }

    public void setHoraChegada(double horaChegada)
    {
        this.horaChegada = horaChegada;
    }

    public double getHoraSaida()
    {
        return this.horaSaida;
    }

    public void setHoraSaida(double horaSaida)
    {
        this.horaSaida = horaSaida;
    }

    public int getDirecao()
    {
        return this.direcao;
    }

    public void atrasarChegada(double horaChegada)
    {
        this.setHoraChegada(horaChegada);
        this.setHoraSaida(0);
    }
    
    @Override
    public Chegada clone() throws CloneNotSupportedException
    {
        Carro carroClonado = new Carro(this.carro.getReacao(), this.carro.getComprimento());
        Cruzamento cruzamentoClonado = this.cruzamento;
        int ruaClonada = this.rua;
        double horaChegadaClonada = this.horaChegada;
        int direcaoClonada = this.direcao;
        int codigoClonado = this.codigo;
       return new Chegada(carroClonado, cruzamentoClonado, ruaClonada, horaChegadaClonada, direcaoClonada, codigoClonado);
    }
    /*public double processar(double saidaAnterior)
     {
     Rua atual = null;
     for(Rua r: this.cruzamento.getRuas())
     {
     if(r.getCodigo() == this.rua)
     {
     atual = r;
     }
     }
     if(atual instanceof RuaSemSemaforo)
     {
     Rede rede = Rede.getInstance();
     RuaSemSemaforo atualSemSemaforo = (RuaSemSemaforo) this.cruzamento.getRuas()[this.rua];
     for(Chegada chegada: rede.getChegadas())
     {
     if(chegada.getHoraChegada() > this.horaChegada + 5)
     {
     break;
     }
     if(chegada.getCruzamento().getCodigo() == this.cruzamento.getCodigo())
     {
     RuaSemSemaforo proxima = (RuaSemSemaforo) chegada.getRua();
     if(proxima.getCodigo() != atualSemSemaforo.getCodigo())
     {
     if(proxima.getPrioridade() < atualSemSemaforo.getPrioridade())
     {
     this.horaSaida = chegada.getHoraChegada() + 2;
     return -(this.horaSaida - this.horaChegada);
     }
     }
     }
     }
     if(this.horaSaida == 0)
     {
     this.horaSaida = this.horaChegada;
     }
     }
     else
     {
     Semaforo semaforo = atual.getViaEntrada().getSemaforo();
     if(this.horaChegada >= saidaAnterior) //carro atual chegou depois do anterior sair
     {
     if(semaforo.descobreEstadoSinal(this.horaChegada) == true)
     {
     //Nesse caso, o carro nao precisou parar
     this.horaSaida = this.horaChegada;
     }
     else
     {
     //o carro eh obrigado a parar, mas eh o primeiro da fila
     this.horaSaida = this.horaChegada + carro.getReacao() + (semaforo.fimVermelho(this.horaChegada));
     }
     }
     else //o carro chegou antes do anterior sair, ou seja, nao eh o primeiro na fila
     {
     this.horaSaida = saidaAnterior;
     if(semaforo.descobreEstadoSinal(this.horaSaida) == false)
     {
     this.horaSaida = this.horaSaida + carro.getReacao() + semaforo.fimVermelho(this.horaSaida);
     }
     }
     }
     return this.horaSaida - this.horaChegada;
     }*/
}
