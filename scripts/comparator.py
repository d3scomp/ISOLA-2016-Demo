import simloader
import os
import matplotlib
matplotlib.use("Qt5Agg")
import matplotlib.pyplot as plot
from matplotlib import pyplot, colors
import numpy as np

print("Comparator")

#logsDir = "../logs/10-99rebprob"
logsDir = "../logs"

logs = []

for (dirpath, dirnames, filenames) in os.walk(logsDir):
    for dir in dirnames:
        if dir.startswith("world"):
            path = dirpath + os.sep + dir;
            print(path)
            log = simloader.load(path)
            logs.append(log)
    break

data = {}

for log in logs:
    maxtimeskew = log[0].config.maxTimeSkewMs;
    collected = log[len(log) - 1].collectedFoodPieces
    
    if not maxtimeskew in data:
        data[maxtimeskew] = []
    
    data[maxtimeskew].append(int(collected))

pdata = []

cnt = 1
xtckcnt = []
xtckname = []
for key in sorted(data.keys()):
    pdata.append(data[key])
    xtckcnt.append(cnt)
    cnt = cnt + 1
    xtckname.append(str(key) + " ms")

plot.boxplot(pdata)
plot.xticks(xtckcnt, xtckname)
plot.show()