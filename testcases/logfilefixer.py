#! python

import sys

for filename in sys.argv[1:]:
	with open(filename, 'r') as f:
		lines = [l for l in f]

	with open(filename, 'w') as f:
		for line in lines:
			f.write(line.replace('GameSimulator$SimulateAction', 'SimulateAction'))
