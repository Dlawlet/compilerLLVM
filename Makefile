jflex:
	jflex src/Main/LexicalAnalyzer.flex
	sed -i '1,1s/^/package Main; /' src/Main/LexicalAnalyzer.java
	javac -d bin -cp src src/Main/Main.java
	jar cfe dist/part2.jar Main/Main -C bin .
#	javadoc -private src/Main/Main.java -d doc/javadoc

testing: dist/part2.jar
	java -jar dist/part2.jar -wt euclid.tex test/euclid.pmp
	pdflatex euclid.tex

test_goodpmp: dist/part2.jar
	java -jar dist/part2.jar -wt TestGoodPmp.tex test/TestGoodpmp.pmp
	pdflatex TestGoodPmp.tex

test_operations: dist/part2.jar
	java -jar dist/part2.jar -wt TestOperations.tex test/TestOperations.pmp
	pdflatex TestOperations.tex

test_incorrectIf: dist/part2.jar
	java -jar dist/part2.jar -wt TestIncorrectIf.tex test/TestIncorectIf.pmp

test_badread: dist/part2.jar
	java -jar dist/part2.jar -wt TestBadRead.tex test/TestBadRead.pmp

all: jflex test_operations

all_tests: testing test_goodpmp test_operations test_incorrectIf test_badread

clean:
	rm -f src/LexicalAnalyzer.java
	rm -rf bin/*
	rm -f dist/part2.jar
	rm -rf doc/javadoc/*
	rm -rf ./*.aux
	rm -rf ./*.log
	rm -rf ./*.tex
	rm -rf ./*.pdf
	
	