import re
import urllib.request

def readWriteLov(file):
	
	iris = set()
	owls = set()
	
	line = file.readline()
	while line:
		m = re.match("^.*\<(.*?)\> \.$",line).group(1)
		if m:
			if re.match(".*\.owl",m): owls.add(m)
			else: iris.add(m)
		line = file.readline()
	
	return iris,owls

def writeLov(iris,owls):
	
	didntGo = open("lov/notWorking.txt","w")
	notAutomatic = open("lov/notAutomatic.txt","w")
	
	for iri in owls:
		try:
			data = urllib.request.urlopen(iri,timeout=10).read().decode('utf-8')
			file = open("lov/owl/"+re.match(".*\/(.*?\.owl).*",iri).group(1),'w')
			file.write(data)
			file.close()
		except: didntGo.write("{}\n".format(iri))
	
	owlfilecheck = re.compile("[\s\S]*(http[\s\S]*\.owl)[\s\S]*",re.MULTILINE)
	rdffilecheck = re.compile("[\s\S]*(http[\s\S]*\.rdf)[\s\S]*",re.MULTILINE)
	ttlfilecheck = re.compile("[\s\S]*(http[\s\S]*\.ttl)[\s\S]*",re.MULTILINE)
	
	i = 1
	for iri in iris:
		if iri == 'http://sparql.sstu.ru:3030/speciality/': didntGo.write("{}\n".format(iri)) ; continue
		print(i,iri)
		i = i + 1
		try:
			data = urllib.request.urlopen(iri,timeout=10).read().decode('utf-8')
			if '@prefix' in data:
				file = open("lov/unknown/"+re.match(".*[^\w](\w+)[^\w]*",iri).group(1)+str(i)+".ttl",'w')
				file.write(data)
				file.close()				
			elif 'Ontology(' in data or 'owl:Ontology' in data:
				file = open("lov/unknown/"+re.match(".*[^\w](\w+)[^\w]*",iri).group(1)+str(i)+".owl",'w')
				file.write(data)
				file.close()
			elif 'rdf:RDF' in data:
				file = open("lov/unknown/"+re.match(".*[^\w](\w+)[^\w]*",iri).group(1)+str(i)+".rdf",'w')
				file.write(data)
				file.close()					
			else:
				m = re.match(owlfilecheck,data)
				if m:
					data = urllib.request.urlopen(m.group(1),timeout=10).read().decode('utf-8')
					file = open("lov/unknown/"+re.match(".*[^\w](\w+)[^\w]*",iri).group(1)+str(i)+".owl",'w')
					file.write(data)
					file.close()
					continue
				m = re.match(rdffilecheck,data)
				if m:
					data = urllib.request.urlopen(m.group(1),timeout=10).read().decode('utf-8')
					file = open("lov/unknown/"+re.match(".*[^\w](\w+)[^\w]*",iri).group(1)+str(i)+".rdf",'w')
					file.write(data)
					file.close()
					continue					
				m = re.match(ttlfilecheck,data)
				if m:
					data = urllib.request.urlopen(m.group(1),timeout=10).read().decode('utf-8')
					file = open("lov/unknown/"+re.match(".*[^\w](\w+)[^\w]*",iri).group(1)+str(i)+".ttl",'w')
					file.write(data)
					file.close()
					continue					
				else: notAutomatic.write("{}\n".format(iri))
		except: 
			didntGo.write("{}\n".format(iri))
