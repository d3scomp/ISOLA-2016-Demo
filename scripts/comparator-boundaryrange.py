import simloader
import os
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plot
from matplotlib import pyplot, colors
import numpy as np

print("Comparator boundary range")

#logsDir = "../logs/10-99rebprob"
logsDir = "logs"

data = {}

for (dirpath, dirnames, filenames) in os.walk(logsDir):
    for dir in dirnames:
        if dir.startswith("world"):
            path = dirpath + os.sep + dir;
            print("Path: " + path)
            
            if not os.path.isfile(path + os.sep + "final.xml"):
                print("no final.xml in path, skipping")
                continue
                        
            log = simloader.loadFinal(path)
            print(log)
            boundaryrange = log.config.rebroadcastRangeM;
            collected = log.report.collected
    
            if not boundaryrange in data:
                data[boundaryrange] = []
            data[boundaryrange].append(int(collected))
    break

print("Data: ")
for key in data:
    print(str(key) + ": " + str(data[key]))
print("Data end")

pdata = []

# Boxplot names
cnt = 1
xtckcnt = []
xtckname = []
for key in sorted(data.keys()):
    pdata.append(data[key])
    xtckcnt.append(cnt)
    cnt = cnt + 1
    xtckname.append(str(key) + " meters")

# Box-plots
plot.boxplot(pdata)

# value dots
for i in range(len(pdata)):
    y = pdata[i]
    x = np.random.normal(1 + i, 0.075, size=len(y))
    plot.plot(x, y, 'bo', alpha=0.4)

plot.xticks(xtckcnt, xtckname)
plot.xlabel("Boundary range in meters")
plot.ylabel("Solution value in collected foods")
plot.savefig("boundaryrange.png")