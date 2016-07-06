import os
dir = os.path.dirname(os.path.realpath(__file__))
pom = os.path.join(dir, "..", "pom.xml")
import xml.etree.ElementTree as ET
root = ET.parse(open(pom)).getroot()
v = root.find('{http://maven.apache.org/POM/4.0.0}version').text
print(v)
