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

def boxplot(data, msgdata, name = "comparison"):
    print ("NAME " + name)
    pdata = []
    msgpdata = []
    
    plot.close()
    
    # Boxplot names and data aggregation
    cnt = 0
    xtckcnt = []
    xtckname = []
    for key in sorted(data.keys()):
        pdata.append(data[key])
        xtckcnt.append(cnt)
        cnt = cnt + 2
        xtckname.append(str(key))
        
    for key in sorted(msgdata.keys()):
        msgpdata.append(msgdata[key])
        
    
    fig, ax1 = plot.subplots()
    ax2 = ax1.twinx()
    
    
    # Box-plots
   
    databoxes = ax1.boxplot(pdata, positions = range(0,len(pdata) * 2, 2), labels=xtckname)
    plot.setp(databoxes['boxes'], color='blue')
    
    msgboxes = ax2.boxplot(msgpdata, positions = range(1,len(msgpdata) * 2 + 1, 2))
    plot.setp(msgboxes['boxes'], color='green')
    
    plot.xlim(-1, len(pdata) * 2)
    
    # value dots
    for i in range(len(pdata)):
        y = pdata[i]
        x = 2*i;
        px = np.random.normal(x, 0.075, size=len(y))
        ax1.plot(px, y, 'bo', alpha=0.4)
        
    # msg dots
    for i in range(len(pdata)):
        y = msgpdata[i]
        x = 2*i+1;
        px = np.random.normal(x, 0.075, size=len(y))
        ax2.plot(px, y, 'go', alpha=0.4)
    
    plot.xticks(xtckcnt, xtckname)
    ax1.set_xlabel(name)
    ax1.set_ylabel("Solution value in collected foods", color = "blue")
    ax2.set_ylabel("Number of messages", color = "green")
    plot.savefig(name + ".png", dpi=256, width = 20, wight = 15)

def processDimension(dimension, filter):
    data = {}
    msgdata = {}
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
                messages = log.report.numMessages
                
                # Add collected record to data to process      
                if not key in data:
                    data[key] = []
                    msgdata[key] = []
                data[key].append(int(collected))
                msgdata[key].append(int(messages))
        break

    print("Data: ")
    for key in data:
        print(str(key) + ": " + str(data[key]) + " " + str(msgdata[key]))
    print("Data end")
    
    boxplot(data, msgdata, name = dimension)
    
for dimension in dimensions:
    processDimension(dimension, dimensions[dimension])

