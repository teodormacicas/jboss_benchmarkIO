#!/bin/bash

# create lmls.tex lmls.ps
gnuplot lmls.gp
# create a latex document t.dvi containing the graph -> see t.tex
latex t.tex
# convert dvi to ps
dvips t.dvi
# convert ps to pdf
epstopdf t.ps
# crop the image to the actual plot size
pdfcrop t.pdf
# save the plot under the desired file name
mv t-crop.pdf delaynio2vsxnio3.pdf
# convert the image to the eps as well
pdftops -eps delaynio2vsxnio3.pdf
# remove all the auxiliary files
rm -f t.{dvi,ps,pdf,aux,log} t-crop.pdf texput.log
