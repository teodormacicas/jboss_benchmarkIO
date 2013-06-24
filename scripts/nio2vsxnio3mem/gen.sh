#!/bin/bash

gnuplot lmls.gp
latex t.tex
dvips t.dvi
epstopdf t.ps
pdfcrop t.pdf
mv t-crop.pdf nio2vsxnio3mem.pdf
pdftops -eps nio2vsxnio3mem.pdf
rm -f t.{dvi,ps,pdf,aux,log} t-crop.pdf texput.log
