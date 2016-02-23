import matplotlib.pyplot as plot
from matplotlib import pyplot, colors
import numpy as np
import loader

log = loader.load()
    
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
    plot.plot(ant.x, ant.y, c=ant.color, alpha=0.5)

plot.show()
plot.savefig(loader.getDatasetName() + '-all.pdf')