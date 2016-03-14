import os
import threading
from time import sleep

os.chdir("..")

class Cfg(threading.Thread):
    def __init__(self, seed=42, limit=30000, maxtimeskew=30000, numants = 10):
        super().__init__()
        self.seed = seed
        self.limit = limit
        self.maxtimeskew = maxtimeskew
        self.numants = numants
        
    def run(self):
        print("Running execution thread")
        wd = os.getcwd()
        os.system("export PATH=$PATH" + os.pathsep + wd + os.sep + "omnet")
        args = '--numants ' + str(self.numants) + ' '
        args += '--seed ' + str(self.seed) + ' '
        args += '--limit ' + str(self.limit) + ' '
        args += '--maxtimeskew ' + str(self.maxtimeskew) + ' '
        os.system('mvn exec:java -Dexec.args="' + args + '"')
        print("Execution done")

MAX_THREADS = 4
cfgs = [];

# Define configurations
numants = 20
seeds = range(0, 10)
maxtimeskews = [2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 20000, 30000]
for seed in seeds:
    for maxtimeskew in maxtimeskews:
        cfgs.append(Cfg(numants=numants, seed=seed, limit=180000, maxtimeskew=maxtimeskew))
    
print("Running " + str(len(cfgs)) + " configurations")

running = []

while len(cfgs) > 0:
    if threading.active_count() >= MAX_THREADS:
        running.pop().join();
    
    sleep(3);
    cfg = cfgs.pop()
    running.append(cfg)
    cfg.start()
    
while len(running) > 0:
    running.pop().join();
    
print("All done")
    