from os import listdir
from os.path import isfile, join
import json

resultsPath = 'build\\reports\\jmh'
outputDir = 'plots'

onlyfiles = [f for f in listdir(resultsPath) if isfile(join(resultsPath, f))]
print onlyfiles

with open(join(outputDir, 'testFile'), "w") as handle:
    json.dump(onlyfiles, handle)
