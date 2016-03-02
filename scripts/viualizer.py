import sys
import matplotlib
matplotlib.use("Qt5Agg")
from matplotlib.backends.backend_qt5agg import FigureCanvasQTAgg as FigureCanvas
from matplotlib.figure import Figure
import numpy as np
import matplotlib.pyplot as plot
from PyQt5.QtCore import *
from PyQt5.QtWidgets import *
import sys

import simloader

colors = ["red", "green", "blue", "yellow", "black", "lime", "cyan", "orange", "orange", "orange", "orange"]

class PlottingCanvas(FigureCanvas):
    def __init__(self, log, parent=None, width=5, height=4, dpi=100):
        self.fig = Figure(figsize=(width, height), dpi=dpi)
        self.plot = self.fig.add_subplot(1, 1, 1)
        self.plot.hold(False)
        self.log = log

        self.drawPlot(0)

        FigureCanvas.__init__(self, self.fig)
        self.setParent(parent)

        FigureCanvas.setSizePolicy(self,
                QSizePolicy.Expanding,
                QSizePolicy.Expanding)
        FigureCanvas.updateGeometry(self)
    
    def drawPlot(self, time):
        time = time * 1000
                
        rec = self.log[time]
        print(time)
        x = []
        y = []
        c = []
        col = 0
        for ant in rec.ants:
            x.append(ant.position.x)
            y.append(ant.position.y)
            c.append(colors[col])
            col = col + 1
         
        self.plot.scatter(x, y, c=colors, linewidths=0)
        self.plot.set_title("Time: " + str(time) + " ms")
        self.plot.set_xlim(-15, 15)
        self.plot.set_ylim(-15, 15)
        
    def updatePlot(self, time):
        self.drawPlot(time)
        self.draw()
        
class Visualizer(QWidget):
    def __init__(self, log):
        super().__init__()
                
        slider = QSlider(Qt.Horizontal, self)
        drawing = PlottingCanvas(log, self, width=5, height=4, dpi=100)

        vbox = QVBoxLayout()
        vbox.addWidget(drawing)
        vbox.addWidget(slider)
        self.setLayout(vbox)
        
        slider.valueChanged.connect(drawing.updatePlot)
        slider.setMinimum(min(log.keys()) / 1000)
        slider.setMaximum(max(log.keys()) / 1000)
        
        self.setGeometry(150, 250, 1024, 768)
        self.setWindowTitle("Visualizer")
        self.show()


app = QApplication(sys.argv)
dirChooser = QFileDialog()
dirChooser.setFileMode(QFileDialog.DirectoryOnly)
if dirChooser.exec_() == QDialog.Accepted:
    logDir = dirChooser.selectedFiles()[0]
else:
    raise(Exception("log dir must be chosen"))

log = simloader.load(logDir)
widget = Visualizer(log)
app.exec_()