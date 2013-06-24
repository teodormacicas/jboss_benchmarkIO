#!/bin/bash

# check the file name format !!!
# for all fixed delays between issuing two consecutive reqs in a thread

for t in `ls 1000-*-100000-*-log.txt | sed 's/^1000-//g' | sed 's/-.*$//g' | sort | uniq`; do 

	# beginning
	X=`cat 1000-${t}-100000-1*-log.txt | sort -n -k 2 | tail -1 | awk '{ print $2 }'`
	# end
	Y=`cat 1000-${t}-100000-1*-log.txt | sort -n -k 2 | head -1 | awk '{ print $2 }'`

	# DEBUG
	# echo X: $X 
	# echo Y: $Y

	# duration in nanoseconds
	Z=`expr $X - $Y`

	# duration in seconds
	T=`perl -le "print $Z/1000000000"`

	# DEBUG
	# echo T: $T

	# number of pkts 
	PKTS=`wc -l 1000-${t}-100000-1*-log.txt | awk '{ print $1 }'`

	# load in pkts/s
	FREQ=`perl -le "print $PKTS/$T"`

	# mean cpu load
	EX=`cat 1000-${t}-100000-Cpu-log.txt | ./cpu.pl | awk '{ print $1 }'`
	
	# std deviation
	SIGMA=`cat 1000-${t}-100000-Cpu-log.txt | ./cpu.pl | awk '{ print $2 }'`

	# print delay between reqs, load, mean, std deviation, std deviation
	echo $t $FREQ $EX $SIGMA $SIGMA

done
