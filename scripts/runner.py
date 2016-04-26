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
        args = '--numbigants ' + str(self.numbigants) + ' '
        args += '--numsmallants ' + str(self.numsmallants) + ' '
        args += '--seed ' + str(self.seed) + ' '
        args += '--limit ' + str(self.limit) + ' '
        args += '--maxtimeskew ' + str(self.maxtimeskew) + ' '
        args += '--radiorange ' + str(self.radiorange) + ' '        
        os.system('java -jar experiment.jar ' + args)
        print("Execution done")

MAX_THREADS = 42
cfgs = [];

# Define configurations
numbigants = 10
numsmallants = 40
radiorange = 5
limit = 300000
seeds = range(0, 20)
maxtimeskews = [500, 1000, 2000, 3000, 5000, 10000, 30000, 10000000]
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
    
