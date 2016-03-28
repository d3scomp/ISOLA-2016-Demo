from lxml import objectify
import os
import re

class Logs:
    def __init__(self, log, final):
        self.log = log
        self.final = final

#### Process logs XML into object structure
def load(logDir):
    print("Loading logs...")
    
    records = []
    for (dirpath, dirnames, filenames) in os.walk(logDir):
        for file in filenames:
            if file != "final.xml":
                records.append(dirpath + os.sep + file)
        
    log = []
    for record in records:
        root = objectify.parse(record).getroot()
        root.time = int(re.sub(".*/|\.xml", "", record))
        log.append(root)
        #log[root.time] = root
    
    print("Loading logs...done")
    
    return Logs(log, loadFinal(logDir))

def loadFinal(logDir):
    final = objectify.parse(logDir + os.sep + "final.xml").getroot()
    return final