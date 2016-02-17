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
    plot.scatter(ant.x, ant.y, c=ant.color, alpha=0.5, linewidths=0)

plot.show()
plot.savefig('all.pdf')
plot.close()

print("Crating time sets")
times = {}
for rec in log:
    if rec.time not in times:
        times[rec.time] = []
    times[rec.time].append(rec)
    
print("Rendering time sets")
for time in times:
    print(time)
    for rec in times[time]:
        plot.title("Time: " + str(time) + " ms")
        plot.scatter(rec.pos.x, rec.pos.y, c=colors[int(rec.id)], alpha=0.5, linewidths=0)
    plot.xlim(-15, 15)
    plot.ylim(-15, 15)
    plot.savefig("out/" + str(time) + ".png")
    plot.close()