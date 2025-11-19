package entidades;

import java.util.ArrayList;

public class ViaEntradaSemSemaforo extends ViaEntrada
{
    private ArrayList<int[]> prioridades;
    
    public ViaEntradaSemSemaforo(int codigo, int numeroFaixas, Faixa[] faixas, Double mediaCarros, int[] direcoes, 
            double[] probabilidades, ArrayList<int[]> prioridades, int peso)
    {
        super(codigo, numeroFaixas, faixas, mediaCarros, direcoes, probabilidades, peso);
    	this.prioridades = prioridades;
    }
    
    public ArrayList<int[]> getPrioridades()
    {
        return this.prioridades;
    }
    
    public int getPrioridadeMovimento(int direcao)
    {
        for(int[] movimento: this.prioridades)
        {
            if(movimento[0] == direcao)
                return movimento[1];
        }
        return -1;
    }
}
