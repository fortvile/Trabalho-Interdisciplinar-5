import socket
import time

def semaforo2():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(("localhost", 5000))  # conecta ao Semáforo 1

    while True:
        data = s.recv(1024).decode()  # recebe tempo do S1
        if not data:
            break

        tempo_verde_s1 = int(data)

        # Regra de não-conflito: se S1 está verde, S2 fica vermelho
        print(f"[S2] Semáforo 1 VERDE {tempo_verde_s1}s → S2 fica VERMELHO")

        # depois que S1 terminar, S2 pode abrir
        time.sleep(3)
        print(f"[S2] Agora pode ficar VERDE por {tempo_verde_s1}s")

if __name__ == "__main__":
    semaforo2()
