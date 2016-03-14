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
        os.system('mvn exec:java -Dexec.args="--numants ' + str(self.numants) + ' --seed ' + str(self.seed) + '--limit ' + str(self.limit) + ' --maxtimeskew ' + str(self.maxtimeskew) + '"')
        print("Execution done")

MAX_THREADS = 4
cfgs = [];

# Define configurations
numants = 20
seedfrom = 0
seedto = 10
maxtimeskews = [3000, 5000, 10000, 20000, 30000]
for maxtimeskew in maxtimeskews:
    for i in range(seedfrom, seedto):
        cfgs.append(Cfg(numants=numants, seed=i, limit=180000, maxtimeskew=maxtimeskew))
    
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
    