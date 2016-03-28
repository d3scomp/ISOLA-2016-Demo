import os
import threading
from time import sleep

os.chdir("..")

class Cfg(threading.Thread):
    def __init__(self, seed, limit, maxtimeskew, numbigants, numsmallants, radiorange):
        super().__init__()
        self.seed = seed
        self.limit = limit
        self.maxtimeskew = maxtimeskew
        self.numbigants = numbigants
        self.numsmallants = numsmallants
        self.radiorange = radiorange
        
    def run(self):
        print("Running execution thread")
        wd = os.getcwd()
        os.system("export PATH=$PATH" + os.pathsep + wd + os.sep + "omnet")
        args = '--numBigAnts ' + str(self.numbigants) + ' '
        args += '--numSmallAnts ' + str(self.numsmallants) + ' '
        args += '--seed ' + str(self.seed) + ' '
        args += '--limitMs ' + str(self.limit) + ' '
        args += '--maxTimeSkewMs ' + str(self.maxtimeskew) + ' '
        args += '--radioRangeM ' + str(self.radiorange) + ' '        
        args += '--loggingIntervalMs 30000'
        os.system('mvn exec:java -Dexec.args="' + args + '"')
        print("Execution done")

MAX_THREADS = 4
cfgs = [];

# Define configurations
numbigants = 10
numsmallants = 40
radiorange = 3
limit = 180000
seeds = range(0, 10)
maxtimeskews = [5000, 10000, 20000, 30000]
for seed in seeds:
    for maxtimeskew in maxtimeskews:
        cfgs.append(Cfg(numbigants=numbigants, numsmallants=numsmallants, seed=seed, limit=limit, maxtimeskew=maxtimeskew, radiorange=radiorange))
    
print("Running " + str(len(cfgs)) + " configurations")

running = []

while len(cfgs) > 0:
    if threading.active_count() >= MAX_THREADS:
        running.pop().join();
    
    print("To go: " + str(len(cfgs)))
    sleep(3);
    cfg = cfgs.pop()
    running.append(cfg)
    cfg.start()
    
while len(running) > 0:
    running.pop().join();
    
print("All done")
    
