compiler:
	jflex src/Main/LexicalAnalyzer.flex
	sed -i '1,1s/^/package Main; /' src/Main/LexicalAnalyzer.java
	javac -d bin -cp src/ src/Main/Main.java
	jar cfe dist/part3.jar Main/Main -C bin .
	javadoc  src/*/*.java -d doc/javadoc

test_goodpmp: 
	java -jar dist/part3.jar test/TestGoodpmp.pmp

test_operations: 
	java -jar dist/part3.jar  test/TestOperations.pmp


test_incorrectIf: 
	java -jar dist/part3.jar test/TestIncorectIf.pmp

test_badread: 
	java -jar dist/part3.jar  test/TestBadRead.pmp

test_euclid:
	java -jar dist/part3.jar  test/euclid.pmp

test_for:
	java -jar dist/part3.jar  test/TestFor.pmp
all: compiler test_goodpmp

	