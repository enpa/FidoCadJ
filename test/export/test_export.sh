#!/bin/sh

# All the tests are being run in the headless mode. This avoid some kind of appearing and
# disappearing icon in the dock and it makes Java startup a little faster.

echo "Test script for graphical export facility of FidoCadJ"
echo "-----------------------------------------------------"
echo ""

echo "Testing the export on bitmap formats"
echo "PNG (Portable Network Graphic)"
java -jar ../../jar/fidocadj.jar -n -c r2 png png/test_out.png original/test_pattern.fcd
java -jar ../../jar/fidocadj.jar -n -c 800 600 png png/test_out_r.png original/test_pattern.fcd
echo "JPG (Joint Photographic Experts Group)" 
java -jar ../../jar/fidocadj.jar -n -c r2 jpg jpg/test_out.jpg original/test_pattern.fcd
java -jar ../../jar/fidocadj.jar -n -c 800 600 jpg jpg/test_out_r.jpg original/test_pattern.fcd

echo

echo Testing the export on vector formats
echo "SVG (Scalar Vector Graphic)"
java -jar ../../jar/fidocadj.jar -n -c r2 svg svg/test_out.svg original/test_pattern.fcd
java -jar ../../jar/fidocadj.jar -n -c 800 600  svg svg/test_out_r.svg original/test_pattern.fcd
java -jar ../../jar/fidocadj.jar -n -c 800 600  svg svg/led_circuit_r.svg original/led_circuit.fcd

echo "EPS (Encapsulated Postscript)"
java -jar ../../jar/fidocadj.jar -n -c r2 eps eps/test_out.eps original/test_pattern.fcd
echo "PDF (Portable Document Format)"
java -jar ../../jar/fidocadj.jar -n -c r2 pdf pdf/test_out.pdf original/test_pattern.fcd
echo "SCR (script for CadSoft Eagle)"
java -jar ../../jar/fidocadj.jar -n -c r2 scr scr/test_out.scr original/test_pattern.fcd
echo "PGF (PGF/TikZ script for LaTeX)"
java -jar ../../jar/fidocadj.jar -n -c r1.5 pgf pgf/test_out.pgf original/test_pattern.fcd
cd pgf
pdflatex test_out.tex
rm test_out.aux
rm test_out.log
cd ..
echo "LaTeX test file for PGF export compiled. Please note that it is perfectly normal that the PGF export does not include the text size and font attributes, since the idea is that the user might control those aspects via LaTeX commands."

echo "The reference drawing has been exported in all the supported formats. Checking the results begins now..."

./check.sh
echo ""

