set term pslatex color 
set output "lmls.tex"

set xrange [0:3800]
set yrange [-0.8:25]
set xlabel "rate [req/s]"
set ylabel "CPU usage [\\%]"


# set key box
set key box left reverse Left width 1

set xtics 0,1000,3800 nomirror

plot "nio2.sync.dat" using 2:3:($3-$4):($3+$4) ti "NIO.2 sync" ps 1.5 lw 9 lc rgb "#000000" with yerrorlines, "xnio3.sync.dat" using 2:3:($3-$4):($3+$4) ti "XIO3 sync" ps 1.5 lw 9 lc rgb "#900000" with yerrorlines, "nio2.async.dat" using 2:3:($3-$4):($3+$4) ti "NIO.2 async" ps 1.5 lw 9 lc rgb "#009000" with yerrorlines, "xnio3.async.dat" using 2:3:($3-$4):($3+$4)  ti "XNIO3 async" ps 1.5 lw 9 lc rgb "#000090" with yerrorlines;


