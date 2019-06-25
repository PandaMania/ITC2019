import matplotlib.pyplot as plt
import numpy as np
from distutils.util import strtobool


#dir = "wbg-fal10.xml-"
#dir = "muni-fsps-spr17.xml-"
dir = "bet-sum18.xml-"

def read_cost(file):
    f = open(dir + file)
    line = f.readline()
    return np.fromstring(line[1:-2], sep=",")

temps = [1,10,50,70,200,500]
for temp in temps:
    costs = read_cost("temperature{}.0.txt".format(temp))
    plt.plot(costs, label=temp)
plt.yscale("log")
plt.legend()
plt.title("cost per iteration for temperature")
plt.xlabel("iteration")
plt.ylabel("cost")
plt.savefig(dir+"temp.png")
plt.show()

neighboursize = [1,3,5,10,50]
for n in neighboursize:
    costs = read_cost("neighboursize{}.txt".format(n))
    plt.plot(costs, label=n)
plt.yscale("log")
plt.legend()
plt.title("cost per iteration for neighbourhood size")
plt.xlabel("iteration")
plt.ylabel("cost")
plt.savefig(dir+"neigh.png")
plt.show()
"neighboursize{}.txt".format(n)


for exp in ["true", "false"]:
    costs= read_cost("exp{}.txt".format(exp))
    label = "linear"
    if strtobool(exp):
        label="exponential"
    plt.plot(costs, label=label)

plt.yscale("log")
plt.legend()
plt.title("cost per iteration for temperature decay method")
plt.xlabel("iteration")
plt.ylabel("cost")
plt.savefig(dir+"tempDecay.png")
plt.show()

for method in ["random","availabilityBased"]:
    costs = read_cost("roomSelect{}.txt".format(method))
    plt.plot(costs, label=method)

plt.yscale("log")
plt.legend()
plt.title("cost per iteration for room selection method")
plt.xlabel("iteration")
plt.ylabel("cost")
plt.savefig(dir+"roomSelect.png")
plt.show()

for method in ["random","constraintBased"]:
    costs = read_cost("timeSelect{}.txt".format(method))
    plt.plot(costs, label=method)

plt.yscale("log")
plt.legend()
plt.title("cost per iteration for time selection method")
plt.xlabel("iteration")
plt.ylabel("cost")
plt.savefig(dir+"roomSelect.png")
plt.show()

