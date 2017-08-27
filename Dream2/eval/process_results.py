from math import sqrt
import os
import scipy.stats

results_dir = "./results/"
running_time = 1000 * 0.5

def extractList(predicate):
    global results_dir
    lst = []
    for file in filter(predicate, os.listdir(results_dir)):
        if file.endswith(".txt"):
            f = open(results_dir + "/" + file, "r")
            lines = f.readlines()
            for i in range(0, len(lines)):
                lst.append(float(lines[i]))
            f.close()

    return lst


def sumTraffic(predicate):
    global results_dir
    trafficMap = {}
    for file in filter(predicate, os.listdir(results_dir)):
        if file.endswith(".txt"):
            f = open(results_dir + "/" + file, "r")
            lines = f.readlines()
            for i in range(0, len(lines)):
                line = lines[i]
                pkt = line.split()[0]
                val = float(line.split()[2])
                if pkt in trafficMap:
                    val = val + trafficMap[pkt]
                    trafficMap[pkt] = val
                else:
                    trafficMap[pkt] = 0
            f.close()

    return trafficMap


def confidenceDelay(lst):
    sum = 0.0
    sumSquare = 0.0
    count = 0
    
    for val in lst:
        sum = sum + val
        sumSquare = sumSquare+(val**2)
        count = count+1
        
    mean = sum/count
    stdDev = sqrt(abs(sumSquare/count-(mean**2))*count/(count-1))
    confidence = (-scipy.stats.t.ppf(0.05,count-1).sum()*stdDev)/sqrt(count)
    
    return confidence


def printDelay(protocol):
    print(protocol)
    for level in range(1,5):
        lst = extractList(lambda x: x[1] == str(level) and "delay" in x and protocol in x)
        avg = sum(lst)/len(lst)
        confidence = confidenceDelay(lst)
        print(str(level) + " " + str(avg) + " " + str(confidence))


def printTraffic(protocol):
    global running_time
    print(protocol)
    trafficMap = sumTraffic(lambda x: "traffic" in x and protocol in x)
    for (k, v) in trafficMap.items():
        print(k + " " + str(v/(running_time*1000)) + " KB/s")


print("*** Delay ***")
printDelay("Causal")
printDelay("Single_source_glitch_free")
printDelay("Complete_glitch_free")
printDelay("Atomic")

print("*** Traffic ***")
printTraffic("Causal")
printTraffic("Single_source_glitch_free")
printTraffic("Complete_glitch_free")
printTraffic("Atomic")