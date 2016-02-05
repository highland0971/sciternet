#!/usr/bin/python

import sys
import os

buf = {}

if __name__ == '__main__':
	if os.access(os.sys.argv[1],os.R_OK):
		with open(os.sys.argv[1]) as fp:
			for line in fp:
				line = line.strip()
				try:
					domain = ".".join(line.split('.')[-2:])
					buf[domain]=None
				except:
					continue
			for key in buf:
				print key
	else:
		print('Invalied file {} .',os.sys.argv[1])
