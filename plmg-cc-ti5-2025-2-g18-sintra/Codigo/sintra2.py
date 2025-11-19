# semaforo2.py
import socket
import time

SERVER_HOST = "localhost"
SERVER_PORT = 5000

def semaforo2():
    while True:
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            print(f"[S2] Conectando em {SERVER_HOST}:{SERVER_PORT} ...")
            s.connect((SERVER_HOST, SERVER_PORT))
            print("[S2] Conectado ao servidor.")

            buffer = ""

            while True:
                data = s.recv(1024)
                if not data:
                    print("[S2] Conexão fechada pelo servidor.")
                    break

                buffer += data.decode()

                while "\n" in buffer:
                    line, buffer = buffer.split("\n", 1)
                    text = line.strip()

                    if not text:
                        continue

                    parts = text.split(",")
                    if len(parts) != 2:
                        print(f"[S2] Mensagem inválida: '{text}'")
                        continue

                    try:
                        tempo_verde_s1 = int(parts[0])
                        tempo_verde_s2 = int(parts[1])
                    except:
                        print(f"[S2] Erro ao interpretar valores: '{text}'")
                        continue

                    print(f"[S2] Semáforo 1 VERDE {tempo_verde_s1}s → S2 fica VERMELHO")
                    time.sleep(tempo_verde_s1)

                    print(f"[S2] AGORA S2 abre por {tempo_verde_s2}s")
                    time.sleep(tempo_verde_s2)

                    print("[S2] S2 amarelo (3s)")
                    time.sleep(3)

                    print("[S2] S2 voltou para VERMELHO")

        except Exception as e:
            print(f"[S2] Erro de conexão/execução: {e}. Tentando reconectar em 3s...")
            try:
                s.close()
            except:
                pass
            time.sleep(3)

if __name__ == "__main__":
    semaforo2()
