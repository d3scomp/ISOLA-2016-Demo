import xml.etree.ElementTree as ET
import sys
from logging import root
from xml.etree.ElementTree import Element

RAW_LOG_FILENAME = "../logs/runtime/runtimeData.xml";

#### Process logs XML into object structure
def load():
    print("Loading logs")
    if len(sys.argv) == 2: 
        rawLogFile = open(sys.argv[1]);
    else:
        rawLogFile = open(RAW_LOG_FILENAME);
    logText = rawLogFile.read()
    rawLogFile.close()

    print("Parsing logs")
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
        
    return log
    
