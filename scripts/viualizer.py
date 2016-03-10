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
        self.log = log

        self.drawPlot(0)

        FigureCanvas.__init__(self, self.fig)
        self.setParent(parent)

        FigureCanvas.setSizePolicy(self, QSizePolicy.Expanding, QSizePolicy.Expanding)
        FigureCanvas.updateGeometry(self)
    
    def drawPlot(self, recNum):
        rec = self.log[recNum]

        self.plot.clear()
        self.plot.plot(float(rec.antHill.x), float(rec.antHill.y), "go")
        self.plotRecordAnts(rec)
        self.plotRecordAntTargets(rec)
        self.plotRecordSources(rec)
        
        self.plot.set_title("Time: " + str(rec.time) + " ms, collected pieces of food: " + str(rec.collectedFoodPieces))
        self.plot.set_xlim(-15, 15)
        self.plot.set_ylim(-15, 15)
        
    def plotRecordAnts(self, rec):
        col = 0
        for ant in rec.ants:
            self.plot.plot(float(ant.position.x), float(ant.position.y), "g^")
            #self.plot.annotate("ant", xy=(float(ant.position.x), float(ant.position.y)), arrowprops=dict(facecolor='black', shrink=0.05))
            col = col + 1
        
    def plotRecordAntTargets(self, rec):
        col = 0
        for ant in rec.ants:
            try:
                self.plot.plot([float(ant.position.x), float(ant.target.x)], [float(ant.position.y), float(ant.target.y)])
            except Exception as e:
                print(e)
            col = col + 1
        
    def plotRecordSources(self, rec):
        if hasattr(rec, 'foodSources'):
            for source in rec.foodSources:
                self.plot.plot(float(source.position.x), float(source.position.y), "bs")
        if hasattr(rec, 'foodPieces'):
            for piece in rec.foodPieces:
                try:
                    self.plot.plot(float(piece.position.x), float(piece.position.y), "r*")
                except Exception:
                    print("Food piece plot failed")
        
    def updatePlot(self, recNum):
        self.drawPlot(recNum)
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
        slider.setMinimum(0)
        slider.setMaximum(len(log) - 1)
        
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