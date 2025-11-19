# simulacao.py
# Simulador orientado a eventos e classes de entidade (portado do artigo para Python)
import heapq
import math
import random
from dataclasses import dataclass, field
from typing import List, Dict, Optional, Tuple

# ---------- Entidades ----------
@dataclass
class Semaforo:
    id: str
    verde: float    # tempo verde (s)
    vermelho: float # tempo vermelho (s)
    offset: float = 0.0  # atraso para primeira abertura (s)
    # We consider yellow included in verde as in the article

    def state_at(self, t: float) -> str:
        """Return 'verde' or 'vermelho' at time t (seconds)"""
        cycle = self.verde + self.vermelho
        if cycle <= 0:
            return 'vermelho'
        # shift by offset
        tt = (t - self.offset) % cycle
        return 'verde' if tt < self.verde else 'vermelho'

    def time_to_next_green(self, t: float) -> float:
        """If currently green returns 0. Else returns time until next green."""
        cycle = self.verde + self.vermelho
        if cycle <= 0:
            return 0.0
        tt = (t - self.offset) % cycle
        if tt < self.verde:
            return 0.0
        return cycle - tt

@dataclass
class Via:
    id: str
    semaforo: Semaforo
    mu_chegada: Optional[float] = None  # média entre chegadas (s) for source
    fila: List[float] = field(default_factory=list)  # arrival times of vehicles in queue

@dataclass
class Movimentacao:
    origem: str
    destino: str
    prob: float = 1.0

@dataclass(order=True)
class Evento:
    t: float
    ordem: int
    tipo: str
    dados: object = field(compare=False)

# ---------- Simulador ----------
class Simulador:
    def __init__(self, vias: Dict[str, Via], movimentos: List[Movimentacao],
                 deslocamentos: Dict[Tuple[str,str], float],
                 T_reage: float = 4.1, T_passa: float = 3.4, sim_time: float = 3600*24):
        self.vias = vias
        self.movimentos = movimentos
        self.desloc = deslocamentos
        self.T_reage = T_reage
        self.T_passa = T_passa
        self.sim_time = sim_time
        self.event_queue: List[Evento] = []
        self.ordem = 0
        # stats
        self.waits: Dict[str, List[float]] = {vid: [] for vid in vias}
        self.num_passados: Dict[str, int] = {vid: 0 for vid in vias}

    def schedule(self, t: float, tipo: str, dados):
        heapq.heappush(self.event_queue, Evento(t, self.ordem, tipo, dados))
        self.ordem += 1

    def init_sources(self):
        # generate first arrival for each source via with mu_chegada
        for vid, via in self.vias.items():
            if via.mu_chegada is not None:
                x = self._exp_sample(via.mu_chegada)
                self.schedule(x, 'chegada', {'via': vid})

    def _exp_sample(self, mu):
        # Exp with mean mu: X = -mu * ln(U)
        U = random.random()
        return -mu * math.log(1 - U) if U < 1 else -mu * math.log(1e-12)

    def run(self):
        self.init_sources()
        while self.event_queue:
            ev = heapq.heappop(self.event_queue)
            t = ev.t
            if t > self.sim_time:
                break
            if ev.tipo == 'chegada':
                self.process_chegada(t, ev.dados['via'])
            elif ev.tipo == 'liberar':
                # liberar marca que um veículo terminou de atravessar via
                self.process_liberar(t, ev.dados['via'])

        # return stats
        avg_waits = {vid: (sum(w)/len(w) if w else 0.0) for vid, w in self.waits.items()}
        return {'avg_waits': avg_waits, 'num_passados': self.num_passados}

    # core logic approximating article rules
    def process_chegada(self, t: float, via_id: str):
        via = self.vias[via_id]
        # create vehicle arrival
        arrival_time = t
        # append to queue
        via.fila.append(arrival_time)
        # if this arrival is first in queue, attempt to depart
        if len(via.fila) == 1:
            self.attempt_departure(t, via_id)
        # schedule next external arrival if source
        if via.mu_chegada is not None:
            x = self._exp_sample(via.mu_chegada)
            self.schedule(t + x, 'chegada', {'via': via_id})

    def attempt_departure(self, t: float, via_id: str):
        via = self.vias[via_id]
        sem = via.semaforo
        # compute when previous vehicle would leave: if none, previous leave = -inf
        # We do not explicitly track last depart per via; instead we schedule 'liberar' events.
        # If semaforo is green now and this vehicle is first and no blocking, it can go.
        if sem.state_at(t) == 'verde':
            # first vehicle has reaction time
            leave_time = t + self.T_reage + self.T_passa
            # schedule liberar event at leave_time (indicates this vehicle passed)
            self.schedule(leave_time, 'liberar', {'via': via_id})
            # record wait
            arr = via.fila.pop(0)
            wait = (t - arr)  # vehicle starts moving at t (when green and reacts)
            self.waits[via_id].append(wait)
            self.num_passados[via_id] += 1
        else:
            # semaforo vermelho: schedule at next green + reaction time
            dt = sem.time_to_next_green(t)
            leave_time = t + dt + self.T_reage + self.T_passa
            self.schedule(leave_time, 'liberar', {'via': via_id})
            # vehicle will wait until that green; record approximate wait based on leave_time - arrival_time
            # we will record actual wait when popped in liberar
            # to avoid double-count, mark by not popping now (pop in liberar)
            pass

    def process_liberar(self, t: float, via_id: str):
        via = self.vias[via_id]
        # The organizarion: when a liberar event occurs, assume one vehicle leaves the queue (if any)
        if via.fila:
            arr = via.fila.pop(0)
            # compute when it started moving: approximate as t - T_passa (it finishes at t)
            start_move = t - self.T_passa
            wait = max(0.0, start_move - arr)
            self.waits[via_id].append(wait)
            self.num_passados[via_id] += 1
            # After this vehicle leaves, next in queue may depart if semaforo is green at that time
            if via.fila:
                # immediate departure if green
                if via.semaforo.state_at(t) == 'verde':
                    # next departs after T_passa from previous leave (we assume headway T_passa)
                    next_leave = t + self.T_passa
                    self.schedule(next_leave, 'liberar', {'via': via_id})
                else:
                    # if red, schedule at next green + reaction + passa
                    dt = via.semaforo.time_to_next_green(t)
                    next_leave = t + dt + self.T_reage + self.T_passa
                    self.schedule(next_leave, 'liberar', {'via': via_id})
        # else: nothing to free (could happen if external scheduling)
