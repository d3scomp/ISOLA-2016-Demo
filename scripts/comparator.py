import simloader
import os
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plot
from matplotlib import pyplot, colors
import numpy as np

print("Comparator generic dimensions")

#logsDir = "../logs/10-99rebprob"
logsDir = "logs"

dimensions = {
              "rebroadcastRangeM": lambda log: log.config.rebroadcastDelayMs < 5000,
              "rebroadcastDelayMs": lambda log: log.config.rebroadcastRangeM >= 5 and log.config.maxTimeSkewMs > 10000,
              "maxTimeSkewMs": lambda log: log.config.rebroadcastRangeM > 5 and log.config.rebroadcastDelayMs > 5000
             }

def boxplot(data, name = "comparison"):
    print ("NAME " + name)
    pdata = []
    
    plot.close()
    
    # Boxplot names and data agregation
    cnt = 1
    xtckcnt = []
    xtckname = []
    for key in sorted(data.keys()):
        pdata.append(data[key])
        xtckcnt.append(cnt)
        cnt = cnt + 1
        xtckname.append(str(key))
    
    # Box-plots
    plot.boxplot(pdata)
    
    # value dots
    for i in range(len(pdata)):
        y = pdata[i]
        x = np.random.normal(1 + i, 0.075, size=len(y))
        plot.plot(x, y, 'bo', alpha=0.4)
    
    plot.xticks(xtckcnt, xtckname)
    plot.xlabel(name)
    plot.ylabel("Solution value in collected foods")
    plot.savefig(name + ".png", dpi=256, width = 20)

for dimension in dimensions:
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
                
                # Custom filtering
                if not dimensions[dimension](log):
                    print("FILTERED OUT")
                    continue
                
                key = log.config[dimension];
                collected = log.report.collected
                
                # Add record to data to process      
                if not key in data:
                    data[key] = []
                data[key].append(int(collected))
        break

    print("Data: ")
    for key in data:
        print(str(key) + ": " + str(data[key]))
    print("Data end")
    
    boxplot(data, name = dimension)

