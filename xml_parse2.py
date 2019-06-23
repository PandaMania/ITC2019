from lxml import etree


file1 = 'lums-sum17_c.xml'
version = 1 #for naming convention

tree = etree.parse(file1)
root = tree.getroot()

tree2 = etree.parse(file1)
root2 = tree2.getroot()


#If you want to remove a set of rooms greater than use modify.py (just to reduce the size)
#current version modify has a bug, let me know if you need it 

#same syntax as ET library (used in modify file)


#-----

#For now, this script just splits the dataset into two xml files: main and removed
#, that can be analysed separetedly

#Later decide weter rooms removed are allocated a room and a time, which therefore 
#as to be blocked --> Would we need to input something into gurobi for that?
#eg somehow read that from textfile

#-----

rooms_total = 62 #Tere is probably a way to obtain from data.., right now just look at file
rooms_assigned = [1, 4, 13, 14, 62] #These are the rooms to be REMOVED
#not sure how muc order ere will affect

courses_total = 19
courses_assigned = [1, 18, 19]



# ~ rooms_left = []
# ~ for i in range(0,total_rooms):
	# ~ if i not in rooms_assign
	# ~ rooms_left.append(i+1)


#Create a list wit all itmes not to be removes
def calculate_left(items_assigned, items_total):
	items_left = []
	for i in range(0, items_total):
		if (i+1) not in items_assigned:
			items_left.append(i+1)
	return items_left

	
rooms_left = calculate_left(rooms_assigned, rooms_total)
courses_left = calculate_left(courses_assigned, courses_total)


print(rooms_left)
print(rooms_assigned)


#Need to think how to input individual constraints into gurobi? wit already assigned rooms..

#exit()

#First side (root, tree):

for a in root.findall('rooms'):
	for b in a.findall('room'):
		id1 = int(b.get('id'))
		#print(id1)
		
		if id1 in rooms_assigned:
			a.remove(b)

for a in root.findall('courses'):
	for b in a.findall('course'):
		for c in b.findall('config'):
			for d in c.findall('subpart'):
				for e in d.findall('class'):
					for g in e.findall('room'):
						
						id2 = int(g.get('id'))
						print(id2)
						
						if id2 in rooms_assigned:
							e.remove(g)

for a in root.findall('courses'):
	for b in a.findall('course'):	
		id1 = int(b.get('id'))
		#print(id1)
		
		if id1 in courses_assigned:
			a.remove(b)	
			

tree.write('{}_main_{}.xml'.format(file1, version))
#more sopisitcated way to write approapiate name?


#Second side (root2, tree2)

for a in root2.findall('rooms'):
	for b in a.findall('room'):
		id1 = int(b.get('id'))
		#print(id1)
		
		if id1 in rooms_left:
			a.remove(b)

for a in root2.findall('courses'):
	for b in a.findall('course'):
		for c in b.findall('config'):
			for d in c.findall('subpart'):
				for e in d.findall('class'):
					for g in e.findall('room'):
						
						id2 = int(g.get('id'))
						print(id2)
						
						if id2 in rooms_left:
							e.remove(g)

for a in root2.findall('courses'):
	for b in a.findall('course'):	
		id1 = int(b.get('id'))
		print(id1)
		
		if id1 in courses_left:
			a.remove(b)	
			
tree2.write('{}_removed_{}.xml'.format(file1, version))
