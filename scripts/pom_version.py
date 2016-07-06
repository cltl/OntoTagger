import xml.etree.ElementTree as ET
root = ET.parse(open('pom.xml')).getroot()
v = root.find('{http://maven.apache.org/POM/4.0.0}version').text
print(v)
