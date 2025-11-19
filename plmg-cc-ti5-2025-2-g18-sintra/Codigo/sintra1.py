# sintra_server_debug.py (VERSÃO CORRIGIDA)
from flask import Flask, request
from flask_cors import CORS
import threading, time, socket

from sintra_optimizer import calcular_tempos_otimizados

app = Flask(__name__)
CORS(app)  # permite CORS para todos os origens

# ---------- Configurações ----------
SOCKET_HOST = "0.0.0.0"
SOCKET_PORT = 5000
HTTP_PORT = 8000
VEHICLE_TIMEOUT = 10.0
CYCLE_INTERVAL = 8.0

ZONES = ["S1", "S2"]

vehicles_lock = threading.Lock()
vehicles = {}

assign_lock = threading.Lock()
assign_index = 0

conn_lock = threading.Lock()
conn_socket = None
conn_addr = None


# -----------------------------------------------------------
# ----------------------   HTTP /gps   ----------------------
# -----------------------------------------------------------

@app.route("/gps", methods=["POST"])
def gps():
    try:
        data = request.get_json(force=True)
    except Exception as e:
        print(f"[HTTP] parse error: {e}")
        return f"Bad request: {e}", 400

    vid = data.get("id")
    zone = data.get("zone")

    if not vid:
        vid = request.remote_addr
        if not vid:
            return "Missing id and no remote addr available", 400
        vid = f"ip-{vid}"

    vid = str(vid).strip()

    if zone:
        zone = str(zone).strip().upper()
        if zone not in ZONES:
            return f"Invalid zone '{zone}'. Valid: {ZONES}", 400
    else:
        global assign_index
        with assign_lock:
            zone = ZONES[assign_index % len(ZONES)]
            assign_index += 1

    now = time.time()
    with vehicles_lock:
        vehicles[vid] = {"zone": zone, "ts": now}

    return "OK", 200


# -----------------------------------------------------------
# ----------------------  CLEANUP THREAD ---------------------
# -----------------------------------------------------------

def cleanup_thread():
    while True:
        time.sleep(5)
        now = time.time()
        removed = 0
        with vehicles_lock:
            to_remove = [vid for vid, v in vehicles.items()
                         if now - v['ts'] > VEHICLE_TIMEOUT]
            for vid in to_remove:
                del vehicles[vid]
                removed += 1
        if removed:
            print(f"[CLEANUP] Removidos {removed} aparelhos inativos")


# -----------------------------------------------------------
# ----------------------   COUNT VEHICLES  -------------------
# -----------------------------------------------------------

def count_in_zone(zone_key):
    now = time.time()
    cnt = 0
    with vehicles_lock:
        for v in vehicles.values():
            if now - v['ts'] <= VEHICLE_TIMEOUT and v.get("zone") == zone_key:
                cnt += 1
    return cnt


# -----------------------------------------------------------
# ----------- FUNÇÃO ANTIGA (DEIXADA COMO BACKUP) -----------
# -----------------------------------------------------------

def compute_green_time(count):
    if count < 1:
        return 0
    elif count < 2:
        return 5
    elif count < 4:
        return 10
    else:
        return 20


# -----------------------------------------------------------
# --------------------- CONTROL THREAD -----------------------
# -----------------------------------------------------------

def control_thread():
    global conn_socket
    cycle = 0
    while True:
        time.sleep(CYCLE_INTERVAL)
        cycle += 1

        cnt_s1 = count_in_zone("S1")
        cnt_s2 = count_in_zone("S2")

        # INTEGRAÇÃO COM O OTIMIZADOR DO ARTIGO
        try:
            tempo_verde_s1, tempo_verde_s2 = calcular_tempos_otimizados(cnt_s1, cnt_s2)
        except Exception as e:
            print("[OPT] Erro na otimização, usando cálculo simples:", e)
            tempo_verde_s1 = compute_green_time(cnt_s1)
            tempo_verde_s2 = compute_green_time(cnt_s2)

        print(f"\n[CICLO {cycle}] S1={cnt_s1} S2={cnt_s2} "
              f"-> verdeS1={tempo_verde_s1}s  verdeS2={tempo_verde_s2}s  "
              f"(ativos totais={len(vehicles)})")

        # Envio para o semáforo2 via socket
        with conn_lock:
            if conn_socket:
                try:
                    msg = f"{int(tempo_verde_s1)},{int(tempo_verde_s2)}\n"
                    conn_socket.send(msg.encode())
                    print(f"[NET] Enviado para Semáforo2: {msg.strip()}")
                except Exception as e:
                    print(f"[NET ERROR] Falha ao enviar para Semáforo2: {e}")
                    try:
                        conn_socket.close()
                    except:
                        pass
                    conn_socket = None
            else:
                print("[NET] Semáforo2 não conectado")


# -----------------------------------------------------------
# ---------------------- SOCKET THREAD -----------------------
# -----------------------------------------------------------

def socket_server_thread():
    global conn_socket, conn_addr

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind((SOCKET_HOST, SOCKET_PORT))
    s.listen(1)
    print(f"[SOCKET] Aguardando conexão em {SOCKET_HOST}:{SOCKET_PORT}")

    while True:
        conn, addr = s.accept()
        with conn_lock:
            conn_socket = conn
            conn_addr = addr
        print(f"[SOCKET] Conectado por {addr}")

        try:
            while True:
                data = conn.recv(1024)
                if not data:
                    break
                print(f"[SOCKET RX] {data.decode(errors='ignore').strip()}")
        except Exception as e:
            print(f"[SOCKET] Conexão perdida: {e}")

        with conn_lock:
            try:
                conn_socket.close()
            except:
                pass
            conn_socket = None
            conn_addr = None

        print("[SOCKET] aguardando nova conexão...")


# -----------------------------------------------------------
# ------------------------ MAIN ------------------------------
# -----------------------------------------------------------

if __name__ == "__main__":
    threading.Thread(target=cleanup_thread, daemon=True).start()
    threading.Thread(target=socket_server_thread, daemon=True).start()
    threading.Thread(target=control_thread, daemon=True).start()

    print("[HTTP] iniciando Flask na porta", HTTP_PORT)
    app.run(host="0.0.0.0", port=HTTP_PORT, debug=False)
