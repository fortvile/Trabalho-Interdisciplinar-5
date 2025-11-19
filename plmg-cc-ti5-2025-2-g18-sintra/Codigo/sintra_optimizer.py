# sintra_optimizer.py
from sintra_adapter import otimizar_rede

def montar_input(cnt_s1, cnt_s2):
    """
    Transforma os contadores reais (número de veículos detectados em S1 e S2)
    em tempos médios de chegada (mu_chegada), como no artigo.
    
    O artigo usa chegadas exponenciais:
        mu = 1 / λ
    λ depende do fluxo observado.
    Aqui fazemos uma aproximação simples:
    mais veículos → intervalo médio menor.
    """
    # evitar divisão por zero
    mu_s1 = 30 if cnt_s1 == 0 else max(3, 40 / cnt_s1)
    mu_s2 = 30 if cnt_s2 == 0 else max(3, 40 / cnt_s2)

    input_data = {
        "vias": [
            {"id": "S1", "mu_chegada": mu_s1, "verde": 10, "vermelho": 10},
            {"id": "S2", "mu_chegada": mu_s2, "verde": 10, "vermelho": 10},
        ],

        # Sem movimentos / rede simples
        "movimentos": [],
        "deslocamentos": {}
    }
    
    return input_data


def calcular_tempos_otimizados(cnt_s1, cnt_s2):
    """
    Integra o SINTRA às funções do artigo.
    """
    input_data = montar_input(cnt_s1, cnt_s2)

    res = otimizar_rede(
        input_data,
        ga_params={
            "pop_size": 20,
            "generations": 5,
            "cycle_limit": 60,    # ciclo total 60s
            "sim_time": 3600,     # simula 1h (rápido)
        }
    )

    best = res['best']

    # São os tempos verdes calculados pelo GA
    tempo_verde_s1 = int(best["S1"])
    tempo_verde_s2 = int(best["S2"])

    return tempo_verde_s1, tempo_verde_s2
