# otimizacao.py
# Algoritmo genético simplificado para otimizar tempos verdes por grupo (por via)
import random
import copy
from typing import Dict, List
from simulacao import Semaforo, Via, Simulador

class OtimizadorGA:
    def __init__(self, rede_vias: Dict[str, Via], desloc, movimentos,
                 pop_size=100, generations=10, mutation_rate=0.05, cycle_limit=120,
                 sim_time=24*3600):
        self.rede_vias = rede_vias
        self.desloc = desloc
        self.movimentos = movimentos
        self.pop_size = pop_size
        self.generations = generations
        self.mutation_rate = mutation_rate
        self.cycle_limit = cycle_limit
        self.sim_time = sim_time

    def random_individual(self):
        # individual: dict via_id -> verde_time (float >0), ensure sum per intersection < cycle_limit
        indiv = {vid: random.uniform(5, 30) for vid in self.rede_vias.keys()}
        # simple normalize per-sum if necessary (for isolated intersections this is ok)
        s = sum(indiv.values())
        if s > self.cycle_limit:
            factor = self.cycle_limit / s
            for k in indiv:
                indiv[k] *= factor
        return indiv

    def fitness(self, indiv):
        # build semaphores with given verdes and simulate
        vias = {}
        for vid, via in self.rede_vias.items():
            verde = indiv[vid]
            vermelho = max(1.0, self.cycle_limit - verde)  # simple approach
            sem = Semaforo(id=vid, verde=verde, vermelho=vermelho, offset=0.0)
            vias[vid] = Via(id=vid, semaforo=sem, mu_chegada=via.mu_chegada)
        sim = Simulador(vias=vias, movimentos=self.movimentos, deslocamentos=self.desloc,
                        T_reage=4.1, T_passa=3.4, sim_time=self.sim_time)
        res = sim.run()
        # objective: minimize maximum average wait among vias (as article)
        avg_waits = res['avg_waits']
        max_wait = max(avg_waits.values()) if avg_waits else float('inf')
        # lower is better, return negative for maximization if needed; here we return directly
        return max_wait, avg_waits

    def tournament_select(self, population, k=2):
        a = random.choice(population)
        b = random.choice(population)
        return a if a[0] < b[0] else b  # choose with smaller fitness (lower wait)

    def crossover(self, parent1, parent2):
        child = {}
        for vid in parent1.keys():
            child[vid] = parent1[vid] if random.random() < 0.5 else parent2[vid]
        # fix sum if needed
        s = sum(child.values())
        if s > self.cycle_limit:
            factor = self.cycle_limit / s
            for k in child:
                child[k] *= factor
        return child

    def mutate(self, indiv):
        if random.random() < self.mutation_rate:
            vid = random.choice(list(indiv.keys()))
            indiv[vid] = random.uniform(5, 30)
            # ensure sum constraint
            s = sum(indiv.values())
            if s > self.cycle_limit:
                factor = self.cycle_limit / s
                for k in indiv:
                    indiv[k] *= factor

    def run(self):
        # initialize population: list of tuples (fitness_value, individual, avg_waits)
        population = []
        for _ in range(self.pop_size):
            indiv = self.random_individual()
            fit, _ = self.fitness(indiv)
            population.append((fit, indiv))
        # evolve
        for gen in range(self.generations):
            new_pop = []
            # elitism: keep best 2
            population.sort(key=lambda x: x[0])
            new_pop.extend(population[:2])
            while len(new_pop) < self.pop_size:
                p1 = self.tournament_select(population)
                p2 = self.tournament_select(population)
                child = self.crossover(p1[1], p2[1])
                self.mutate(child)
                fit, _ = self.fitness(child)
                new_pop.append((fit, child))
            population = new_pop
            print(f"GA gen {gen+1}/{self.generations} best fit {population[0][0]:.3f}")
        # return best
        population.sort(key=lambda x: x[0])
        best_fit, best_indiv = population[0]
        return best_indiv, best_fit
