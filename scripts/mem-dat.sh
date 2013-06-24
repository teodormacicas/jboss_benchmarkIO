#!/bin/bash

for t in `ls 1000-*-100000-*-log.txt | sed 's/^1000-//g' | sed 's/-.*$//g' | sort | uniq`; do 

	X=`cat 1000-${t}-100000-1*-log.txt | sort -n -k 2 | tail -1 | awk '{ print $2 }'`
	Y=`cat 1000-${t}-100000-1*-log.txt | sort -n -k 2 | head -1 | awk '{ print $2 }'`

	# echo X: $X 
	# echo Y: $Y

	Z=`expr $X - $Y`

	T=`perl -le "print $Z/1000000000"`

	# echo T: $T

	PKTS=`wc -l 1000-${t}-100000-1*-log.txt | awk '{ print $1 }'`

	FREQ=`perl -le "print $PKTS/$T"`

	cat 1000-${t}-100000-top-log.txt | ./mem.pl > 1000-${t}-100000-mem.dat

        cat <<EOF >file.gp

        f(x) = a * x + b

        fit f(x) "1000-$t-100000-mem.dat" using 1:2 via a,b

        print "ALPHA: ", a
        print "BETA: ", b

EOF
	ALPHA=`gnuplot file.gp 2>&1 | grep ALPHA | awk '{ print $2 }'`

	BETA=`gnuplot file.gp 2>&1 | grep BETA | awk '{ print $2 }'`

	echo $t $FREQ $ALPHA

done
