import os
import threading
from time import sleep

class Cfg(threading.Thread):
    def __init__(self, seed, limit, maxtimeskew, numbigants, numsmallants, radiorange, rebroadcastrangem, rebrodcastdelay, userebroadcasting):
        super().__init__()
        self.seed = seed
        self.limit = limit
        self.maxtimeskew = maxtimeskew
        self.numbigants = numbigants
        self.numsmallants = numsmallants
        self.radiorange = radiorange
        self.rebroadcastrangem = rebroadcastrangem
        self.rebroadcastdelay = rebroadcastdelay
        self.userebroadcasting = userebroadcasting
        
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
        args += '--rebroadcastRangeM ' + str(self.rebroadcastrangem) + ' '
        args += '--rebroadcastDelayMs ' + str(self.rebroadcastdelay) + ' '
        args += '--useRebroadcasting ' + str(self.userebroadcasting) + ' '
        args += '--logIntervalMs 30000'
        print(args)
        os.system('java -jar experiment.jar ' + args)
        print("Execution done")

MAX_THREADS = 12
cfgs = [];

# Define configurations
numbigants = 6
numsmallants = 40
radiorange = 5
limit = 300000
seeds = range(0, 10)
rebroadcatDelays = [1000, 3000, 5000]
rebroadcastRanges = [0, 5, 10, 15]
maxtimeskews = [5000, 10000, 30000]
for seed in seeds:
    for rebroadcastdelay in rebroadcatDelays:
        for rebroadcastrange in rebroadcastRanges: 
            for maxtimeskew in maxtimeskews:
                userebroadcasting = rebroadcastrange != 0
                 
                cfgs.append(Cfg(
                                numbigants=numbigants,
                                numsmallants=numsmallants,
                                seed=seed,
                                limit=limit,
                                maxtimeskew=maxtimeskew,
                                radiorange=radiorange,
                                rebroadcastrangem=rebroadcastrange,
                                rebrodcastdelay=rebroadcastdelay,
                                userebroadcasting=userebroadcasting)
                            )
    
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
    
