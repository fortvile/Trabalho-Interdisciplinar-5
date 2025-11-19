import time
import random

# Configuração do semáforo
tempo_verde = 10   # segundos
tempo_vermelho = 10
tempo_amarelo = 3

# Fila de veículos esperando (inicialmente vazia)
fila = 0

# Estatísticas
total_veiculos = 0
tempo_espera_total = 0

# Simulação de 5 ciclos completos
for ciclo in range(5):
    print(f"\n=== Ciclo {ciclo+1} ===")

    # Veículos chegam aleatoriamente enquanto o sinal está vermelho
    novos = random.randint(0, 20)
    fila += novos
    total_veiculos += novos
    print(f"(Carros detectados: {fila})")

    # Verde: libera veículos da fila
    if fila > 0:
        liberados = min(fila, tempo_verde)  # cada seg libera 1 carro
        fila -= liberados
        print(f"Sinal verde: {liberados} veículos passaram.")
    else:
        print("Sinal verde: Nenhum veículo esperando.")

    # Atualiza tempo de espera dos que ficaram
    tempo_espera_total += fila * (tempo_vermelho + tempo_amarelo)

    # Vermelho + Amarelo (tempo em que fila acumula)
    print("Sinal vermelho... (acumulando veículos)")
    time.sleep(0.5)  # tempo de espera

# Resultados
print("\n=== Resultados da Simulação ===")
print(f"Total de veículos que chegaram: {total_veiculos}")
print(f"Veículos ainda na fila: {fila}")
print(f"Tempo total de espera acumulado: {tempo_espera_total} segundos")

if total_veiculos > 0:
    tempo_medio = tempo_espera_total / total_veiculos
    print(f"Tempo médio de espera por veículo: {tempo_medio:.2f} segundos")
