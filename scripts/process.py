import matplotlib.pyplot as plot
from matplotlib import pyplot, colors
import numpy as np
import xml.etree.ElementTree as ET

import os
from logging import root
from xml.etree.ElementTree import Element

print("Processing logs")

#RAW_LOG_FILENAME = "../logs/runtimeData.xml";
RAW_LOG_FILENAME = "../logs/simple.xml";

rawLogFile = open(RAW_LOG_FILENAME);
logText = rawLogFile.read()
rawLogFile.close() 

#logText = "<log>" + logText + "</log>"

#### Process logs XML into object structure

root = ET.fromstring(logText)

log = {}

class Position:
    def __init__(self, x, y, z):
        self.x = x
        self.y = y
        self.z = z
        
    def __str__(self):
        return "[" + str(self.x) + "," + str(self.y) + "," + str(self.z) + "]"

class LogRecord:
    def __init__(self, time, id, position):
        self.time = time
        self.id = id
        self.pos = position
    
    def __str__(self):
        return str(self.time) + ", " + self.id + ", " + str(self.pos)

log = []

for event in root:
    id = event.get("id")
    time = int(event.get("time"))
    positionElement = event[0]
    x = float(positionElement[0].text)
    y = float(positionElement[1].text)
    z = float(positionElement[2].text)
    
    log.append(LogRecord(time, id, Position(x, y, z)))
    
#for rec in log:
#    print(rec)
    
#### Visualize
colors = ["red", "green", "blue", "yellow", "black", "lime", "cyan", "orange"]
antdata = {}

print("Creating point sets")
class Object:
    pass
for rec in log:
    if rec.id not in antdata:
         antdata[rec.id] = Object()
         antdata[rec.id].x = []
         antdata[rec.id].y = []
         antdata[rec.id].color = colors[int(rec.id)]
    
    antdata[rec.id].x.append(rec.pos.x)
    antdata[rec.id].y.append(rec.pos.y)

print("Rendering")
for key in antdata:
    ant = antdata[key]
    plot.scatter(ant.x, ant.y, c=ant.color, alpha=0.5, linewidths=0)

plot.savefig('all.pdf')

print("Crating time sets")
