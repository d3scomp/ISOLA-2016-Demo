import simloader
import os
import sys
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plot
from matplotlib import pyplot, colors
import numpy as np

print("Comparator generic dimensions")

# logsDir = "../logs/10-99rebprob"
logsDir = "logs"

def cfgGuard(log, dim):
    if not hasattr(log.config, "mode"):
        log.config.mode = "standard"
    if not hasattr(log.config, "networkModel"):
        log.config.networkModel = "simple"
        
    if log.config.mode != dim['mode']:
        return False
    if float(log.config.radioRangeM) != float(dim['radiorange']):
        #print(str(log.config.radioRangeM) + " != " + str(dim['radiorange']))
        return False
    if log.config.networkModel != dim['networkModel']:
        #print(log.config.networkModel + " != " + dim['networkModel'])
        return False
    
    return True

dimensions = []
for networkModel in ['simple', 'omnet']:
    for mode in ['standard', 'quantum']:
        for radiorange in [3, 5, 7]:
            dimensions.append({
            'headline': "Rebroadcast range influence on " + mode + " mode with " + str(radiorange) + "m radio range (" + networkModel + ")",
            'xaxisText': "Rebroadcast range in meters",
            'xaxisTransform': lambda val: str(val),
            'value': "rebroadcastRangeM",
            'mode': mode,
            'networkModel': networkModel,
            'radiorange': radiorange,
            'filter': lambda log, dim: cfgGuard(log, dim) and log.config.rebroadcastDelayMs < 5000,
            })
            
            dimensions.append({
            'headline': "Rebroadcast delay influence on " + mode + " mode with " + str(radiorange) + "m radio range (" + networkModel + ")",
            'xaxisText': "Rebroadcast period in seconds",
            'xaxisTransform': lambda val: str(val / 1000),
            'value': "rebroadcastDelayMs",
            'mode': mode,
            'networkModel': networkModel,
            'radiorange': radiorange,
            'filter': lambda log, dim: cfgGuard(log, dim) and log.config.rebroadcastRangeM >= 5 and log.config.maxTimeSkewMs > 10000
            })
            
            dimensions.append({
            'headline': "Old knowledge removal influence on " + mode + " mode with " + str(radiorange) + "m radio range (" + networkModel + ")",
            'xaxisText': "Maximal allowed knowledge age in seconds",
            'xaxisTransform': lambda val: str(val / 1000),
            'value': "maxTimeSkewMs",
            'mode': mode,
            'networkModel': networkModel,
            'radiorange': radiorange,
            'filter': lambda log, dim: cfgGuard(log, dim) and log.config.rebroadcastRangeM > 5 and log.config.rebroadcastDelayMs > 5000
            })

def boxplot(data, msgdata, name="comparison", xaxisText="value", xaxisTransform=lambda x: x):
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
        xtckname.append(str(xaxisTransform(key)))
        
    for key in sorted(msgdata.keys()):
        msgpdata.append(msgdata[key])
        
    
    fig, ax1 = plot.subplots()
    ax2 = ax1.twinx()
    
    
    # Box-plots
   
    databoxes = ax1.boxplot(pdata, positions=range(0, len(pdata) * 2, 2), labels=xtckname)
    plot.setp(databoxes['boxes'], color='blue')
    
    msgboxes = ax2.boxplot(msgpdata, positions=range(1, len(msgpdata) * 2 + 1, 2))
    plot.setp(msgboxes['boxes'], color='green')
    
    plot.xlim(-1, len(pdata) * 2)
    
    # value dots
    for i in range(len(pdata)):
        y = pdata[i]
        x = 2 * i;
        px = np.random.normal(x, 0.075, size=len(y))
        ax1.plot(px, y, 'bo', alpha=0.4)
        
    # msg dots
    for i in range(len(pdata)):
        y = msgpdata[i]
        x = 2 * i + 1;
        px = np.random.normal(x, 0.075, size=len(y))
        ax2.plot(px, y, 'go', alpha=0.4)
    
    plot.title(name)
    plot.xticks(xtckcnt, xtckname)
    ax1.set_xlabel(xaxisText)
    ax1.set_ylabel("Solution value in collected foods", color="blue")
    ax2.set_ylabel("Number of messages", color="green")
    plot.savefig(name + ".png", dpi=256, width=20, wight=15)

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

def processDimension(dimension, logs):
    data = {}
    msgdata = {}
    
    for log in logs:
        # Custom filtering
        if dimension["filter"](log, dimension) == False:
            print("-", end='', flush=True)
            continue
        
        print("+", end='', flush=True)
        
        key = log.config[dimension['value']];
        collected = log.report.collected
        messages = log.report.numMessages
        
        # Add collected record to data to process      
        if not key in data:
            data[key] = []
            msgdata[key] = []
        data[key].append(int(collected))
        msgdata[key].append(int(messages))

    print("")

    print("Data: ")
    for key in data:
        print(str(key) + ": " + str(data[key]) + " " + str(msgdata[key]))
    print("Data end")
    
    if len(data) > 0:
        boxplot(data, msgdata, name=dimension['headline'], xaxisText=dimension['xaxisText'], xaxisTransform=dimension['xaxisTransform'])
    else:
        print("No data for this comparison dimension")
        print(dimension['headline'])

if len(sys.argv) == 2:
    logsDir = sys.argv[1];
    
print("Using logsdir: " + logsDir);

logs = loadAllLogs()

for dimension in dimensions:
    processDimension(dimension, logs)

