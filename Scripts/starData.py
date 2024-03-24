"""
This script converts bar separated data obtained from https://vizier.cds.unistra.fr/cgi-bin/VizieR?-source=V/50
into JSON
"""

import json

def timeToDec(inputString):
    values = inputString.strip().split(' ')
    hour = values[0]
    decimalDigits = str((float(values[1])/60) + (float(values[2])/3600))
    decNum = float(hour + "." + decimalDigits.split('.')[1])
    return decNum


starArray = []

starsRaw = open("StarsRaw.txt", "r")
starJson = open("StarData.json", "w")

lines = starsRaw.readlines()

for line in lines:
    values = line.strip().split('|')

    newStar = {"Name": values[0],
               "RA": timeToDec(values[3]),
               "DEC": timeToDec(values[4])
               }
    
    starArray.append(newStar)

jsonData = {"Stars": starArray}         

json.dump(jsonData, starJson, indent=4)

starsRaw.close()
starJson.close()
    

                                       