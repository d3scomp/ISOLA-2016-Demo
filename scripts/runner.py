import os
import threading
from time import sleep

MAX_THREADS = 48
TOTAL_MEM_MB = 128000

class Cfg(threading.Thread):
    def __init__(self, seed, limit, maxtimeskew, numbigants, numsmallants, radiorange, rebroadcastrangem, rebrodcastdelay, networkModel, mode, fitnessType):
        super().__init__()
        self.seed = seed
        self.limit = limit
        self.maxtimeskew = maxtimeskew
        self.numbigants = numbigants
        self.numsmallants = numsmallants
        self.radiorange = radiorange
        self.rebroadcastrangem = rebroadcastrangem
        self.rebroadcastdelay = rebroadcastdelay
        self.userebroadcasting = True
        self.networkModel = networkModel
        self.mode = mode
        self.fitnessType = fitnessType
        
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
        args += '--logIntervalMs 0 '
        args += '--mode ' + str(self.mode) + ' '
        args += '--sourceCount 8 '
        args += '--perSourceRemoveProbabilityPerS 0.02 '
#        args += '--perSourceRemoveProbabilityPerS 0.1 '
        args += '--networkModel ' + str(self.networkModel) + ' '
        args += '--fitnessType ' + str(self.fitnessType) + ' '

#        print(args)
        cmd = 'LD_LIBRARY_PATH=omnet java -Djava.library.path=/home/matena/ants/omnet -Xmx' + str(int(TOTAL_MEM_MB / MAX_THREADS)) + 'm -jar experiment.jar ' + args + ' > /dev/null 2>&1';
        print(cmd)
        os.system(cmd)
        print("Execution done")
        
class MetaCfg:
    def __init__(self, numbigantss, numsmallantss, radioranges, limits, seeds, rebroadcatDelays, rebroadcastRanges, maxtimeskews, networkModels, modes, fitnessTypes):
        self.numbigantss = numbigantss
        self.numsmallantss = numsmallantss
        self.radioranges = radioranges
        self.limits = limits
        self.seeds = seeds
        self.rebroadcatDelays = rebroadcatDelays
        self.rebroadcastRanges = rebroadcastRanges
        self.maxtimeskews = maxtimeskews
        self.networkModels = networkModels
        self.modes = modes
        self.fitnessTypes = fitnessTypes

metaCfgs = [];
cfgs = [];

limit_ms = 1800000
seed_range = range(0, 10)

# Define meta configurations
for mode in ['quantum']:#['standard', 'quantum']:
    for networkModel in ['simple']:#['simple', 'omnet']:
        # Rebroadcast range comparison
        metaCfgs.append(MetaCfg(
                    numbigantss = [6],
                    numsmallantss = [40],
                    radioranges = [5],#[3, 5, 7],
                    limits = [limit_ms],
                    seeds = seed_range,
                    rebroadcatDelays = [5000],
                    rebroadcastRanges = [0, 5, 10, 15],
                    maxtimeskews = [5000],
                    networkModels = [networkModel],
                    modes = [mode],
                    fitnessTypes = ["PreferClose", "PreferDistant", "PreferNeutral"]
                    ))
        # Rebroadcast delay
        metaCfgs.append(MetaCfg(
                    numbigantss = [6],
                    numsmallantss = [40],
                    radioranges = [5],#[3, 5, 7],
                    limits = [limit_ms],
                    seeds = seed_range,
                    rebroadcatDelays = [1000, 5000, 10000, 15000],
                    rebroadcastRanges = [10],
                    maxtimeskews = [30000],
                    networkModels = [networkModel],
                    modes = [mode],
                    fitnessTypes = ["PreferClose", "PreferDistant", "PreferNeutral"]
                    ))
        # Knowledge age preference 
        metaCfgs.append(MetaCfg(
                    numbigantss = [6],
                    numsmallantss = [40],
                    radioranges = [5],#[3, 5, 7],
                    limits = [limit_ms],
                    seeds = seed_range,
                    rebroadcatDelays = [10000],
                    rebroadcastRanges = [10],
                    maxtimeskews = [1000, 5000, 10000, 30000, 60000],
                    networkModels = [networkModel],
                    modes = [mode],
                    fitnessTypes = ["PreferClose", "PreferDistant", "PreferNeutral"]
                    ))

print("Generating using " + str(len(metaCfgs)) + " meta configurations")

for metaCfg in metaCfgs:
    for numbigants in metaCfg.numbigantss:
        for numsmallants in metaCfg.numsmallantss:
            for radiorange in metaCfg.radioranges:
                for limit in metaCfg.limits:
                    for seed in metaCfg.seeds:
                        for rebroadcastdelay in metaCfg.rebroadcatDelays:
                            for rebroadcastrange in metaCfg.rebroadcastRanges:
                                for maxtimeskew in metaCfg.maxtimeskews:
                                    for networkModel in metaCfg.networkModels:
                                        for mode in metaCfg.modes:
                                            for fitnessType in metaCfg.fitnessTypes:
                                                cfgs.append(Cfg(
                                                    numbigants=numbigants,
                                                    numsmallants=numsmallants,
                                                    seed=seed,
                                                    limit=limit,
                                                    maxtimeskew=maxtimeskew,
                                                    radiorange=radiorange,
                                                    rebroadcastrangem=rebroadcastrange,
                                                    rebrodcastdelay=rebroadcastdelay,
                                                    networkModel=networkModel,
                                                    mode = mode,
                                                    fitnessType = fitnessType)
                                                )
    
print("Running " + str(len(cfgs)) + " configurations")

running = []

while len(cfgs) > 0:
    active = 0;
    for cfg in running:
        if cfg.isAlive() == True:
            active = active + 1
    print("Active: " + str(active))
    if active >= MAX_THREADS:
        sleep(1)
        continue
    
    print("To go: " + str(len(cfgs)))
    sleep(1);
    cfg = cfgs.pop()
    running.append(cfg)
    cfg.start()
    
while len(running) > 0:
    running.pop().join();
    
print("All done")
    
