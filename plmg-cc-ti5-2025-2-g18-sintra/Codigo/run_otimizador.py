# run_otimizador.py
from sintra_adapter import otimizar_rede

# exemplo de entrada simples (replicando dados do artigo caso 1)
input_data = {
    "vias": [
        {"id": "v1", "mu_chegada": 8.8, "verde": 33, "vermelho": 22},
        {"id": "v2", "mu_chegada": 18.5, "verde": 22, "vermelho": 33}
    ],
    "movimentos": [],  # não usados na versão simplificada
    "deslocamentos": {}
}

res = otimizar_rede(input_data, ga_params={'pop_size': 20, 'generations': 5, 'sim_time': 3600*1})
print("Melhor solução (tempos verdes por via):", res['best'])
print("Fitness (max avg wait):", res['fitness'])
