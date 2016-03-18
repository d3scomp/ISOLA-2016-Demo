from lxml import objectify
import os
import re

#### Process logs XML into object structure
def load(logDir):
    print("Loading logs...")
    
    records = []
    for (dirpath, dirnames, filenames) in os.walk(logDir):
        for file in filenames:
            records.append(dirpath + os.sep + file)
        
 #   print(records)
    
    log = []
    
    for record in records:
        root = objectify.parse(record).getroot()
        root.time = int(re.sub(".*/|\.xml", "", record))
        log.append(root)
        #log[root.time] = root
    
    print("Loading logs...done")
    return log

def loadLast(logDir):
    print("Loading LAST log...")
    
    records = []
    for (dirpath, dirnames, filenames) in os.walk(logDir):
        for file in filenames:
            record = dirpath + os.sep + file;
            time = int(re.sub(".*/|\.xml", "", record))
            records.append({"record": record, "time":time})
        
    maxRec = records[0];
    print(maxRec["time"])
    for record in records:
        if record['time'] > maxRec['time']:
            maxRec = record
    
    root = objectify.parse(maxRec["record"]).getroot()
    root.time = maxRec['time']
       
    print("Loading logs...done")
    return root