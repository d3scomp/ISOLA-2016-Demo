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
    
    log = {}
    
    for record in records:
        root = objectify.parse(record).getroot()
        root.time = int(re.sub(".*/|\.xml", "", record))
        #log.append(root)
        log[root.time] = root
    
    print("Loading logs...done")
    return log