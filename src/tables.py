instance = "bet-sum18.xml"
folder_name = "bet-sum"

class experiment():
    def __init__(self, id, T_S, T_E, num_iterations, num_changes, temp_decay, room_select, time_select, path):
        self.id = id
        self.T_S = T_S
        self.T_E = T_E
        self.num_iterations = num_iterations
        self.num_changes = num_changes
        self.temp_decay = temp_decay
        self.room_select = room_select
        self.time_select = time_select
        self.path = path
        self.get_costs_constraints()

    def get_costs_constraints(self):
        with open(self.path, 'r') as file:
            data = file.read().strip().split("\n")
        self.costs = data[0].replace('[', '').replace(']', '').split(', ')
        self.constraints = []
        for i in range(1, len(data)):
            self.constraints.append(data[i].split(','))
        self.num_infeasible = [0 for x in range(10)]
        self.num_violated = [0 for x in range(10)]
        for i in range(len(self.constraints)):
            self.constraints[i].remove('')
        for idx, item in enumerate(self.constraints):
            self.num_violated[idx] = len(item)
            for j in range(len(item)):
                if "req" in item[j]:
                    self.num_infeasible[idx] += 1
        self.num_infeasible = sum(self.num_infeasible) / len(self.num_infeasible)
        self.num_violated = sum(self.num_violated) / len(self.num_violated)


    def __print__(self):
        line = "{0} & {1} & {2} & {3} & {4} & {5} & {6} & {7} & {8} \\\\ \\hline".format(self.id, self.T_S, self.num_changes, self.temp_decay, self.room_select,
                                                                               self.time_select, self.costs[-1], self.num_infeasible, self.num_violated)
        print(line)

id = 1
header = """\\begin{figure}[H]
\\begin{table}[H]
\\centering
\\begin{tabular}{|l|l|l|l|l|l||l|l|l|l|l|l|l|l|l|}
\\hline
 &  &  &  &  &  &  & avg num & avg num \\\\
 Id & $T_S$ & $n_{\\text{changes}}$ & temp decay & room select & time select & avg cost & infeasible & violated \\\\ \\hline
"""
print(header, end='')

# tempExp:
temps = [1, 10, 50, 70, 200, 500]
for temp in temps:
    exp = experiment(id, temp, 0.1, 10000, 3, "exp", "random", "random", "{}/{}-temperature{}.0.txt".format(folder_name, instance, temp))
    exp.__print__()
    id += 1

# neighborsizeExp:
neighborhood_sizes = [1, 3, 5, 10, 50]
for size in neighborhood_sizes:
    exp = experiment(id, 70, 0.1, 10000, size, "exp", "random", "random",
                     "{}/{}-neighboursize{}.txt".format(folder_name, instance, size))
    exp.__print__()
    id += 1

# decayExp:
for bool, decay in zip(["true", "false"], ["exp", "lin"]):
    exp = experiment(id, 200, 0.1, 10000, 1, decay, "random", "random",
                     "{}/{}-exp{}.txt".format(folder_name, instance, bool))
    exp.__print__()
    id += 1

# roomselectExp:
for method in ["random", "availabilityBased"]:
    exp = experiment(id, 200, 0.1, 10000, 1, "exp", method, "random",
                     "{}/{}-roomSelect{}.txt".format(folder_name, instance, method))
    exp.__print__()
    id += 1

# timeselectExp:
for method in ["random", "constraintBased"]:
    exp = experiment(id, 200, 0.1, 10000, 1, "exp", "random", method,
                     "{}/{}-timeSelect{}.txt".format(folder_name, instance, method))
    exp.__print__()
    id += 1

footer = """  
\end{tabular}
\caption{Results and overview of violated constraints of simulated annealing on the problem instance """ + instance.replace(".xml", '') + """ with end temperature $T_E=0.1$ and a total number of iterations $n=10000$.}
\label{results_annealing_""" + instance.replace(".xml", '') + """}
\end{table}
\end{figure}"""
print(footer)