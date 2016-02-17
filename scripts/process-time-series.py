import matplotlib.pyplot as plot
from matplotlib import pyplot, colors
import numpy as np
import loader

log = loader.load()
    
#### Visualize
colors = ["red", "green", "blue", "yellow", "black", "lime", "cyan", "orange"]
antdata = {}

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