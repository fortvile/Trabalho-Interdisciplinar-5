# sintra_adapter.py
# funções de integração para converter os dados do SINTRA (ex: seus dicts / protótipos) em objetos do simulador
from typing import Dict, Any
from simulacao import Via, Semaforo
from otimizacao import OtimizadorGA

def build_vias_from_input(input_data: Dict[str, Any]) -> Dict[str, Via]:
    """
    Espera input_data no formato:
    {
      "vias": [
         {"id": "v1", "mu_chegada": 8.8, "verde": 33, "vermelho": 22, "offset": 0},
         ...
      ],
      "movimentos": [...],
      "deslocamentos": {("v1","v2"): 10.0, ...}
    }
    Retorna dict id->Via
    """
    vias = {}
    for v in input_data.get('vias', []):
        sem = Semaforo(id=v['id'], verde=float(v.get('verde', 30.0)),
                       vermelho=float(v.get('vermelho', 30.0)),
                       offset=float(v.get('offset', 0.0)))
        mu = v.get('mu_chegada', None)
        vias[v['id']] = Via(id=v['id'], semaforo=sem, mu_chegada=(float(mu) if mu is not None else None))
    return vias

def otimizar_rede(input_data: Dict[str, Any], ga_params: Dict[str, Any] = None):
    vias = build_vias_from_input(input_data)
    movimentos = input_data.get('movimentos', [])
    desloc = input_data.get('deslocamentos', {})
    ga_params = ga_params or {}
    ga = OtimizadorGA(rede_vias=vias, desloc=desloc, movimentos=movimentos,
                      pop_size=ga_params.get('pop_size', 30),
                      generations=ga_params.get('generations', 5),
                      mutation_rate=ga_params.get('mutation_rate', 0.05),
                      cycle_limit=ga_params.get('cycle_limit', 120),
                      sim_time=ga_params.get('sim_time', 24*3600))
    best, fit = ga.run()
    return {'best': best, 'fitness': fit}
