import socket
import time
import random

def semaforo1():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind(("localhost", 5000))  # servidor na porta 5000
    s.listen(1)
    print("Semáforo 1 aguardando conexão...")

    conn, addr = s.accept()
    print("Semáforo 2 conectado:", addr)

    ciclos = 0

    # métricas
    total_veiculos = 0
    tempo_espera_total = 0
    fila = 0

    while ciclos < 5:
        # chegada aleatória de veículos
        novos = random.randint(0, 20)
        fila += novos
        total_veiculos += novos

        # cálculo do tempo verde adaptativo
        if fila < 1:
            tempo_verde = 0
        elif fila < 5:
            tempo_verde = 5
        elif fila < 15:
            tempo_verde = 10
        else:
            tempo_verde = 20

        # veículos liberados no ciclo
        liberados = min(fila, tempo_verde)
        fila -= liberados

        # envia tempo para o Semáforo 2
        conn.send(str(tempo_verde).encode())

        # imprime status
        print(f"\n=== Ciclo {ciclos+1} ===")
        print(f"[S1] Carros chegaram: {novos}")
        print(f"[S1] Verde = {tempo_verde}s → {liberados} veículos liberados")
        print(f"[S1] Fila restante: {fila}")

        # atualiza tempo de espera dos que ficaram
        tempo_espera_total += fila * 3  # enquanto S2 fica verde

        time.sleep(3)  # espera antes do próximo ciclo
        ciclos += 1

    # resultados finais
    print("\n=== Resultados da Simulação (S1) ===")
    print(f"Total de veículos que chegaram: {total_veiculos}")
    print(f"Veículos ainda na fila: {fila}")
    print(f"Tempo total de espera acumulado: {tempo_espera_total} segundos")
    if total_veiculos > 0:
        tempo_medio = tempo_espera_total / total_veiculos
        print(f"Tempo médio de espera por veículo: {tempo_medio:.2f} segundos")

if __name__ == "__main__":
    semaforo1()
