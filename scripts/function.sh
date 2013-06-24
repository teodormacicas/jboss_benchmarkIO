#!/bin/bash

# for all fixed delays between issuing two consecutive reqs in a single thread
# check the file name format !!!

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

	# This procedure requires gnu-plot (apt-get install gnuplot gnuplot-x11)
	# We create a gnuplot file in which we 
	# 1) define the Gamma Distribution f(a:=ALPHA,b:=BETA,x) = b^a / Gamma(a) * x^(a - 1) * exp(-b * x)
	# 2) fit the function
	# 3) display both parameters of this function
	cat <<EOF >file.gp

	f(x) = (abs(b) ** abs(a)) / gamma(abs(a)) * x**(abs(a)-1) * exp(-abs(b) * x)

	fit f(x) "1000-$t-100000.dat" using 1:2 via a,b

	print "ALPHA: ", a
	print "BETA: ", b

EOF
	# get ALPHA (a)
	ALPHA=`gnuplot file.gp 2>&1 | grep ALPHA | awk '{ print $2 }'`

	# get BETA (b)
	BETA=`gnuplot file.gp 2>&1 | grep BETA | awk '{ print $2 }'`

	# DEBUG
	# echo "ALPHA: $ALPHA"
	# echo "BETA: $BETA"

	# EX = ALPHA/BETA -> check the Wikipedia article on the Gamma Distribution
	EX=`perl -le "print abs($ALPHA/$BETA)"`
	# D2X = ALPHA/BETA^2 -> check the Wikipedia article on the Gamma Distribution
	DDX=`perl -le "print sqrt(abs($ALPHA/$BETA/$BETA))"`

	# total number of pkts
	PKTS=`wc -l 1000-${t}-100000-1*-log.txt | awk '{ print $1 }'`

	# load in pkts/s
	FREQ=`perl -le "print $PKTS/$T"`

	# DEBUG
	# echo FREQ: $FREQ;
	
	# DEBUG
	# echo EX: $EX;

	# DEBUG
	# echo sigma: $DDX;

	# print load EX, std deviation, std deviation
	echo $FREQ $EX $DDX $DDX

done
