package entidades;

import simulacao.Movimento;
import java.util.ArrayList;
import java.util.Random;

public abstract class ViaEntrada
{

    protected int numeroFaixas;
    protected Faixa[] faixas;
    protected double mediaCarros;
    protected double tempoEsperaTotal;
    protected long numeroTotalCarros;
    //private double desvioPadrao;
    protected int codigo;
    protected ArrayList<Movimento> pares;
    protected int[] direcoes;
    protected double[] probabilidades;
    protected int peso;

    public ViaEntrada(int codigo, int numeroFaixas, Faixa[] faixas,
            Double mediaCarros, int[] direcoes, double[] probabilidades, int peso)
    {
        this.numeroFaixas = numeroFaixas;
        this.faixas = faixas;
        this.mediaCarros = mediaCarros;
        this.tempoEsperaTotal = 0;
        this.numeroTotalCarros = 0;
        //this.desvioPadrao = 0;
        this.codigo = codigo;
        this.pares = new ArrayList<Movimento>();
        this.direcoes = direcoes;
        this.probabilidades = probabilidades;
        this.peso = peso;
    }

    public int getCodigo()
    {
        return this.codigo;
    }

    public int getNumeroFaixas()
    {
        return numeroFaixas;
    }

    public Faixa[] getFaixas()
    {
        return faixas;
    }

    public double getMediaCarros()
    {
        return mediaCarros;
    }

    public double getTempoEsperaTotal()
    {
        return tempoEsperaTotal;
    }

    public void setTempoEsperaTotal(double tempoEsperaTotal)
    {
        this.tempoEsperaTotal = tempoEsperaTotal;
    }

    public void aumentaTempoEsperaTotal(double tempo)
    {
        this.tempoEsperaTotal += tempo;
    }

    public long getNumeroTotalCarros()
    {
        return numeroTotalCarros;
    }

    public void setNumeroTotalCarros(long numeroTotalCarros)
    {
        this.numeroTotalCarros = numeroTotalCarros;
    }

    public void aumentaNumeroTotalCarros()
    {
        this.numeroTotalCarros++;
    }

    public ArrayList<Movimento> getPares()
    {
        return pares;
    }

    public void setPares(ArrayList<Movimento> pares)
    {
        this.pares = pares;
    }

    public int[] getDirecoes()
    {
        return this.direcoes;
    }

    public double[] getProbabilidades()
    {
        return this.probabilidades;
    }

    public int getPeso()
    {
        return this.peso;
    }

    public void addPares(String destinos)
    {
        for (char c : destinos.toCharArray())
        {
            if (c != ' ')
            {
                pares.add(new Movimento(this.codigo, Integer.parseInt("" + c)));
            }
        }
    }

    public String detalhesPares()
    {
        String str = "";
        for (Movimento par : this.pares)
        {
            str += par.toString() + "  ";
        }
        return str;
    }

    public void imprimirEstatistica()
    {
        double mediaEspera = (this.tempoEsperaTotal / 10.0) / this.numeroTotalCarros;
        //this.calculaDesvio(mediaEspera);
        System.out.printf("Numero de carros: %d\n", this.numeroTotalCarros);
        System.out.printf("Tempo de espera medio: %.2f segundos\n", mediaEspera);
            //System.out.printf("Desvio padrao: %.2f: \n",this.desvioPadrao);

        //double intervalo = this.calculaProbabilidadeT() * (this.desvioPadrao/Math.sqrt(this.numeroTotalCarros));
        //System.out.printf("%.3f <= media <= %.3f\n",(mediaEspera-intervalo),(mediaEspera+intervalo));
    }

    /*public double calculaProbabilidadeT()
     {
     TDistribution t = new TDistribution(this.numeroTotalCarros-1);
     double confianca = Teclado.lerDouble("Entre com o nivel de confianca (0 < x < 1)");
     return t.inverseCumulativeProbability(confianca);
     }*/

    /*public void calculaDesvio(double media)
     {
     double desvio = 0;
     for(Carro carro: this.carros)
     {
     desvio += Math.pow(((carro.getTempoEspera()/10) - media),2);
     }
     this.desvioPadrao = Math.sqrt(desvio/(this.numeroTotalCarros));
     }*/
    public double exponencial()
    {
        Random rand = new Random();
        double r = rand.nextDouble();
        return (-this.mediaCarros * Math.log(1.0 - r));
    }

    public void inicializaFaixas()
    {
        for (Faixa faixa : this.faixas)
        {
            faixa.setUltimaSaida(0);
        }
    }

    public Faixa determinaFaixa(Carro carro, Integer dir)
    {
        //de acordo com a direcao passada, determina em qual faixa o carro vai entrar
        double menorTempo = 0;
        Faixa faixaEscolhida = null;
        boolean escolhido = false;
        for (Faixa f : this.faixas)
        {
            boolean existe = false;
            for (int direcao : f.getDirecoes())
            {
                if (direcao == dir)
                {
                    existe = true;
                }
            }
            if (existe)
            {
                if (escolhido == false)
                {
                    menorTempo = f.getUltimaSaida();
                    escolhido = true;
                    faixaEscolhida = f;
                } else
                {
                    if (menorTempo >= f.getUltimaSaida())
                    {
                        menorTempo = f.getUltimaSaida();
                        faixaEscolhida = f;
                    }
                }
            }
        }
        return faixaEscolhida;
    }
}
