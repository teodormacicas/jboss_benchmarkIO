for t in `ls 1000-*-100000-*-log.txt | sed 's/^1000-//g' | sed 's/-.*$//g' | sort | uniq`; do cat 1000-$t-100000-*-log.txt | ./mean.pl > 1000-$t-100000.dat; done
