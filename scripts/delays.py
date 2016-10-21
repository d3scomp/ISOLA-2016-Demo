import os
import matplotlib
matplotlib.use("Qt5Agg")
import matplotlib.pyplot as plot
from matplotlib import pyplot, colors
import numpy as np


with open("delays") as f:
    lines = f.readlines()
    
delays = []

for line in lines:
    try:
        delays.append(int(line))
    except:
        pass

delays = [int(delay) for delay in delays]

plot.hist(delays, bins=50)


plot.show()