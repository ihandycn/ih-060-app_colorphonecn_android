#!/usr/bin/python

import os
import re
import hashlib
from sys import argv
from punk import Punk

PROCESS_PNG_IN_COMPLIANCE_WITH_PNG_SPEC = True
STRING_DOPE_RATIO = 1.0
PRIMES = [2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47]

class ExtraStringPool:
    index = 0
    keyCounter = 0
    pickingInterval = PRIMES[0]
    strings = []

    def __init__(self, flavorIndex):
        self.pickingInterval = PRIMES[int(flavorIndex) % len(PRIMES)]
        print 'Picking interval ' + str(self.pickingInterval)
        with open('./multiproduct/extra-strings.txt', 'r') as fp:
            for cnt, line in enumerate(fp):
                self.strings.append(line.replace('\n', ''))
        print str(len(self.strings)) + ' strings in the pool'

    def nextString(self):
        ret = self.strings[self.index % len(self.strings)]
        self.index = self.index + self.pickingInterval
        return ret

    def generateKey(self):
        self.keyCounter = self.keyCounter + 1
        return "sa_extra_string_key_" + str(self.keyCounter)

def resourceNodeWithRawString(rawString, stringPool):
    return '    <string name=\"' + stringPool.generateKey() + '\">' + rawString + '</string>\n'

def dopeStringResources(path, stringPool):
    print path
    extraCount = 0
    contents = []
    with open(path, 'r') as fp:
        stringCount = 0
        contents = fp.readlines()
        for line in contents:
            if re.match('.*<string.*>.*</string>.*', line):
                stringCount = stringCount + 1
        extraCount = int(stringCount * STRING_DOPE_RATIO)
        if extraCount == 0:
            return
        print str(stringCount) + ' strings in file, adding ' + str(extraCount) + ' extra strings'
    for i in range(extraCount):
        contents.insert(2, resourceNodeWithRawString(stringPool.nextString(), stringPool))
    with open(path, 'w') as fp:
        contents = "".join(contents)
        fp.write(contents)

def dopeDrawable(path, isPng, flavorIndex):
    if int(flavorIndex) <= 0:
        return
    if isPng and PROCESS_PNG_IN_COMPLIANCE_WITH_PNG_SPEC:
        print 'Altering MD5 of ' + path + ' by adding a small private chunk before IEND chunk'
        try:
            Punk().encode(path, bytearray(flavorIndex))
        except ValueError as e:
            print e
    else:
        print 'Altering MD5 of ' + path + ' by tailing ' + flavorIndex + ' zero-valued byte(s)'
        with open(path, 'r+') as fp:
            content = fp.read()
            newContent = content + ('\0' * int(flavorIndex))
            print 'Before: ' + hashlib.md5(content).hexdigest()
            print 'After: ' + hashlib.md5(newContent).hexdigest()
            fp.seek(0)
            fp.write(newContent)
            fp.truncate()

if __name__ == "__main__":
    flavor = argv[1]
    buildType = argv[2]
    flavorIndex = argv[3]
    print 'Processing resources for ' + flavor + ' of build type ' + buildType + ', flavor index ' + flavorIndex

    extraStringPool = ExtraStringPool(flavorIndex)

    for root, dirs, files in os.walk('./build/intermediates/res/merged/' + flavor + os.sep + buildType):
        path = root.split(os.sep)
        for file in files:
            fullPath = root + os.sep + file
            if re.match('values.xml', file):
                dopeStringResources(fullPath, extraStringPool)
            elif re.match('.*\.png', file):
                dopeDrawable(fullPath, True, flavorIndex)
            elif re.match('.*\.jpg', file):
                dopeDrawable(fullPath, False, flavorIndex)
