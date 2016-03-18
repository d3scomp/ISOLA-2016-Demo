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

data = {}

for (dirpath, dirnames, filenames) in os.walk(logsDir):
    for dir in dirnames:
        if dir.startswith("world"):
            path = dirpath + os.sep + dir;
            print(path)
            
            
            log = simloader.loadLast(path)
            maxtimeskew = log.config.maxTimeSkewMs;
            collected = log.collectedFoodPieces
    
            if not maxtimeskew in data:
                data[maxtimeskew] = []
            data[maxtimeskew].append(int(collected))
    break


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

for i in range(len(pdata)):
    y = pdata[i]
    x = np.random.normal(1 + i, 0.075, size=len(y))
    plot.plot(x, y, 'bo', alpha=0.4)

plot.xticks(xtckcnt, xtckname)
plot.xlabel("Max allowed data age in ms")
plot.ylabel("Solution value in collected foods")
plot.show()