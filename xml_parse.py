import xml.etree.ElementTree as ET



tree = ET.parse('lums-sum17_c.xml') #put name of file to modify
root = tree.getroot()


# to be removed (split) #(right now I am just using bigger or smaller than)
list_rooms = [] 
list_courses = []

room_limit = 50
course_limit = 15


#removes rooms from room section
#better version
for a in root.findall('rooms'):
	for b in a.findall('room'):
		id1 = int(b.get('id'))
		print(id1)
		
		if id1 > room_limit:
			a.remove(b)


for a in root.findall('courses'):
	for b in a.findall('course'):
		for c in b.findall('config'):
			for d in c.findall('subpart'):
				for e in d.findall('class'):
					for g in e.findall('room'):
						
						id2 = int(g.get('id'))
						print(id2)
						
						if id2 > room_limit:
							e.remove(g)
							
for a in root.findall('courses'):
	for b in a.findall('course'):	
		id1 = int(b.get('id'))
		print(id1)
		
		if id1 > course_limit:
			a.remove(b)		

for a in root.findall('distributions'):
	root.remove(a)


tree.write('output2.xml') #name of modified file


#---------------

# ~ #putting into functiopn format (so that it is easier to work with)

# ~ def remove_rooms(list_rooms, condition):
	
	# ~ for a in root.findall('courses'):
		# ~ for b in a.findall('course'):
			# ~ for c in b.findall('config'):
				# ~ for d in c.findall('subpart'):
					# ~ for e in d.findall('class'):
						# ~ for g in e.findall('room'):
						
							# ~ id2 = int(g.get('id'))
							# ~ print(id2)
						
							# ~ if condition == True:
								# ~ e.remove(g)


# ~ def condition(id1):
	# ~ if id1 in list_rooms:
		# ~ return True
	# ~ if if2 < 4:
		# ~ return True
		


#------------------

# ~ #Spare code

# ~ #Find all all elements that begin with room
# ~ for a in root.iter('room'): #Could do with time as well
    # ~ print(a.attrib)
    
# ~ #Does not work
# ~ for a in root.iter('room'):
	
	# ~ id1 = int(a.get('id'))
	# ~ print(id1)
	
	# ~ if id1 > 50:
		# ~ root.remove(a)


