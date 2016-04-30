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
    if log.config.fitnessType != dim['fitness']:
        return False
    
    return True

dimensions = []
for networkModel in ['simple']:#['simple', 'omnet']:
    for mode in ['standard']:#['standard', 'quantum']:
        for fitness in ["PreferDistant"]:#["PreferClose", "PreferDistant", "PreferNeutral"]:
            for radiorange in [5]:#[3, 5, 7]:
                dimensions.append({
                #'headline': fitness + " - rebroadcast range on " + mode + " " + str(radiorange) + "m radio range (" + networkModel + ")",
                'headline': "Prefer distant beacons - rebroadcast radius influence",
                'xaxisText': "Rebroadcast radius in meters",
                'xaxisTransform': lambda val: str(int(val)),
                'value': "rebroadcastRangeM",
                'mode': mode,
                'networkModel': networkModel,
                'radiorange': radiorange,
                'fitness': fitness,
                'filter': lambda log, dim: cfgGuard(log, dim) and log.config.rebroadcastDelayMs == 5000 and log.config.maxTimeSkewMs == 120000,
                })
                
                dimensions.append({
                #'headline': fitness + " - rebroadcast delay on " + mode + " " + str(radiorange) + "m radio range (" + networkModel + ")",
                'headline': "Prefer distant beacons - rebroadcast period influence",
                'xaxisText': "Rebroadcast period in seconds",
                'xaxisTransform': lambda val: str(int(val / 1000)),
                'value': "rebroadcastDelayMs",
                'mode': mode,
                'networkModel': networkModel,
                'radiorange': radiorange,
                'fitness': fitness,
                'filter': lambda log, dim: cfgGuard(log, dim) and log.config.rebroadcastRangeM == 10 and log.config.maxTimeSkewMs == 120000
                })
                
                dimensions.append({
                #'headline': fitness + " - old knowledge removal on " + mode + " " + str(radiorange) + "m radio range (" + networkModel + ")",
                'headline': "Prefer distant beacons - max. packet age influence",
                'xaxisText': "Max. packet age in seconds",
                'xaxisTransform': lambda val: str(int(val / 1000)),
                'value': "maxTimeSkewMs",
                'mode': mode,
                'networkModel': networkModel,
                'radiorange': radiorange,
                'fitness': fitness,
                'filter': lambda log, dim: cfgGuard(log, dim) and log.config.rebroadcastRangeM == 10 and log.config.rebroadcastDelayMs == 10000 and log.config.maxTimeSkewMs != 1000
                })

def boxplot(data, msgdata, name="comparison", xaxisText="value", xaxisTransform=lambda x: x):
    print ("NAME " + name)
    pdata = []
    msgpdata = []
        
    # Boxplot names and data aggregation
    xtckname = []
    cnt = 0
    xtckcnt = []
    for key in sorted(data.keys()):
        pdata.append(data[key])
        xtckcnt.append(cnt)
        cnt = cnt + 2
        xtckname.append(str(xaxisTransform(key)))
        
    for key in sorted(msgdata.keys()):
        msgpdata.append(msgdata[key])
    
    print(str(len(pdata)) + " " + str(len(msgpdata)))
    
    fig = plot.figure()
    ax1 = fig.add_subplot(111)
    
    ax2 = ax1.twinx()
    
###### value dots
#    for i in range(len(pdata)):
#        y = pdata[i]
#        x = 2 * i;
#        px = np.random.normal(x, 0.075, size=len(y))
#        ax1.plot(px, y, 'bo', alpha=0.4)
        
###### msg dots
#    for i in range(len(pdata)):
#        y = msgpdata[i]
#        x = 2 * i + 1;
#        px = np.random.normal(x, 0.075, size=len(y))
#        ax2.plot(px, y, 'go', alpha=0.4)
    
    # Box-plots
    plot.xlim(-1, len(pdata) * 2)
    
    
    databoxes = ax1.boxplot(pdata, positions=range(0, len(pdata) * 2, 2), patch_artist=True, manage_xticks = False)
    for patch in databoxes['boxes']:
        patch.set_facecolor('green')
    for median in databoxes['medians']:
        median.set_color('black')
    
    msgboxes = ax2.boxplot(msgpdata, positions=range(1, len(msgpdata) * 2 + 1, 2), patch_artist=True, manage_xticks = False)
    for patch in msgboxes['boxes']:
        patch.set_facecolor('red')
    for median in msgboxes['medians']:
        median.set_color('black')
    
    plot.xticks(list(map(lambda x: x + 0.5, range(0, len(pdata) * 2 + 0, 2))), xtckname)
    
    fig.suptitle(name)
    ax1.set_xlabel(xaxisText)
    ax1.set_ylabel("System utility", color="green")
    ax2.set_ylabel("Number of messages in thousands", color="red")
    fig.savefig(name + ".png", dpi=256, width=20, wight=15)
    fig.savefig(name + ".eps")
    fig.savefig(name + ".pdf")
    fig.savefig(name + ".ps")
    fig.savefig(name + ".svg")

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
        utility = log.report.utility
        messages = log.report.msgCounter.sendCounter
        
        # Add collected record to data to process      
        if not key in data:
            data[key] = []
            msgdata[key] = []
        data[key].append(float(utility))
        msgdata[key].append(int(messages) / 1000)

    print("")

    print("Data: ")
    for key in data:
        print("Util:" + str(key) + ": " + str(data[key]))
        print("Msgs:" + str(key) + ": " + str(msgdata[key]))
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


