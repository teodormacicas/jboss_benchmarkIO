#!/bin/sh
#
#
#
#

#host=$1;
#port=$2
#n=$3;
#nReq=$4;

#host=localhost
host=172.17.7.254
port=8001
n=100;
nReq=100000
IOTYPE=$1
SYTYPE=$2

#for i in 2000 1000 500 250 200 166 142 125 100
# 2000 1950 1900 1850 1800 1750 ?
#for i in 175 180 185 190 195 200 210 215 220 225 230 235 240 245 250 500 1000 2000
#for i in 3500 3000 2750 2600 2550 2500 2450 2400 2350 2300 2250 2200 2150 2100 2050 2000 1950 1900 1850 1800 1750
#for i in 350 300 275 260 255 250 245 240 235 230 225 220 215 210 205 200 195 190 185 180 175
#for i in 210 205 200
for i in 250 225 200 175 150 125 100 75
do
	echo ""
	printf "sh run.sh $host $port $n $i $nReq \n";

  # stop previous tests...
  ssh cluster08 ps -ef | grep -v grep | grep jfclere | grep java 2>&1 1> $$.tmp
  grep java $$.tmp > /dev/null
  if [ $? -eq 0 ]; then
    pid=`grep java $$.tmp | awk ' { print $2 } '`
    echo $pid
    ssh cluster08 kill -9 $pid
    sleep 10
  fi
  ssh cluster08 /home/jfclere/PROXY/nio2-xnio3-test/run-$IOTYPE-$SYTYPE.sh &
  sleep 10

  # find the java and top it.
  ssh cluster08 ps -ef | grep -v grep | grep jfclere | grep java 2>&1 1> $$.tmp
  grep java $$.tmp > /dev/null
  if [ $? -eq 0 ]; then
    pid=`grep java $$.tmp | awk ' { print $2 } '`
    echo $pid
  fi
  ssh cluster08 top -b -p $pid 2>&1 1> $$.top &

  date > $n-$i-$nReq-ifconfig-log.txt
  /sbin/ifconfig eth2 >> $n-$i-$nReq-ifconfig-log.txt
  sh run.sh $host $port $n $i $nReq
  /sbin/ifconfig eth2 >> $n-$i-$nReq-ifconfig-log.txt
  date >> $n-$i-$nReq-ifconfig-log.txt

  #stop the top
  ssh cluster08 ps -ef | grep -v grep | grep jfclere | grep top 2>&1 1> $$.tmp
  grep top $$.tmp > /dev/null
  if [ $? -eq 0 ]; then
    pid=`grep top $$.tmp | awk ' { print $2 } '`
    echo $pid
    ssh cluster08 kill -15 $pid
  fi
  grep java $$.top > $n-$i-$nReq-top-log.txt
  grep Cpu $$.top > $n-$i-$nReq-Cpu-log.txt

  sleep 10
done
# like nio2.sync.tests.tar.gz
tar cvf $IOTYPE.$SYTYPE.tests.tar.gz $n-*-log.txt
rm *-log.txt
