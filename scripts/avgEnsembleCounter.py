import simloader
import os
import sys
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plot
from matplotlib import pyplot, colors
import numpy as np

# logsDir = "../logs/10-99rebprob"
logsDir = "logs"

def loadAllLogs():
    logs = []
    for (dirpath, dirnames, filenames) in os.walk(logsDir):
        for dir in dirnames:
            if dir.startswith("world"):
                path = dirpath + os.sep + dir;
                #print("Path: " + path)
                                
                if not os.path.isfile(path + os.sep + "final.xml"):
                    print("!", end='', flush=True)
                    continue
                            
                logs.append(simloader.loadFinal(path))
    return logs

if len(sys.argv) == 2:
    logsDir = sys.argv[1];
    
print("Using logsdir: " + logsDir);

logs = loadAllLogs()

sum = 0.0 
cnt = 0
for log in logs:
    print(log.report.avgEnsembles)
    sum += log.report.avgEnsembles
    cnt = cnt + 1


totalAvgEnsembles = float(sum/cnt) if cnt > 0 else float('nan')
print("Overall everage ensemble instances: " + str(totalAvgEnsembles))
