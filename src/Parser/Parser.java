package Parser;

import Grammar.GrammarTransformation;
import Grammar.Grammar;
import Grammar.ActionTable;
import Main.LexicalUnit;
import Main.Symbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;


public class Parser {
    private Grammar grammar;
    private ActionTable actionTable;
    private ArrayList<Symbol> symbolList;
    private ArrayList<Integer> rulesSequence;
    private Map<String, LexicalUnit> terminalsMap;
    private Map<String, Integer> variablesNumbers;
    private int index = 0;


    /**
     * Constructs a Parser
     *
     * @param grammar the grammar of the considered language
     * @param symbolList the list of the symbols supported by the language
     * @param terminalsMap a map having the terminals of the grammar as keys and the corresponding lexical units as values
     */
    public Parser(Grammar grammar, ArrayList<Symbol> symbolList, Map<String, LexicalUnit> terminalsMap){
        Grammar reducedGrammar = GrammarTransformation.removeUseless(grammar);
        reducedGrammar.removeLeftRecursion();
        reducedGrammar.leftFactor();
        this.grammar = reducedGrammar;
        //System.out.println(this.grammar.toString());
        this.actionTable = new ActionTable(reducedGrammar);
        System.out.println(actionTable);
        this.symbolList = symbolList;
        this.terminalsMap = terminalsMap;
        this.variablesNumbers =  this.grammar.findVariablesNumbers();
        this.rulesSequence = new ArrayList<>();
    }


    /**
     * This method checks whether the current symbol's type matches any element of a given set of A
     *
     * @param A the element whose set is inspected to find an element matching the current symbol
     * @param set a map containing the set of A to be inspected
     * @return true if the current symbol matches any element of the set of A
     */
    private boolean isIn(String A, Map<String, Set<String>> set){
        Symbol currentSymbol = symbolList.get(index);
        for(String elementOfA : set.get(A)){
            if(terminalsMap.get(elementOfA) == currentSymbol.getType()){return true;}
        }
        return false;
    }


    /**
     * This method tries to match the specified element with the expected lexical unit
     * and throws an exception if the operation was unsuccessful
     *
     * @param element the element to check the match with the current symbol's type
     * @throws Exception the exception thrown if the elements don't match
     */
    private void match(String element) throws Exception {
        Symbol currentSymbol = symbolList.get(index);
        if(terminalsMap.get(element) == currentSymbol.getType()){index += 1;}
        else{
            String str = "\n\n" + "Syntax Error:\n" +
                    "The following token does not match with the expected lexical unit:\n"
                    + "Expected lexical unit: " + terminalsMap.get(element) + "\n" +
                    "Token encountered: type = " + currentSymbol.getType() + " value =  " + currentSymbol.getValue() + "  at line  " + currentSymbol.getLine() + " and column " + currentSymbol.getColumn() + "\n\n";
            throw new Exception(str);
        }
    }
    
    
    /**
     * This method throws an exception message describing that a mismatch has occurred
     * @param A the variable involved in the mismatch
     * @throws Exception the exception thrown if a mismatch occurs
     */
    private void throwMismatchException(String A) throws Exception{
        Symbol currentSymbol = symbolList.get(index);
        String str = "\n\nSyntax Error:\n" + "The following token does not match with any of the acceptable lexical units expected\n" + "Acceptable lexical units: ";
        for(String componentOfFirstA : actionTable.getFirst().get(A)){
            str += terminalsMap.get(componentOfFirstA) + " ";
        }
        str +=  "\n";
        str += "Token encountered: type = " + currentSymbol.getType() + " value =  " + currentSymbol.getValue() + "  at line  " + currentSymbol.getLine() + " and column " + currentSymbol.getColumn() + "\n\n";
        throw new Exception(str);
    }


    /**
     * This recursive method reads the current input variable and builds the parse tree associated to it
     *
     * @param A the current input variable
     * @return parseTree the parse tree associated to A
     * @throws Exception the exception raised if the current input doesn't match
     */
    private ParseTree buildParseTree(String A) throws Exception {
        ArrayList<ArrayList<String>> rulesOfA = grammar.getRulesOf(A);
        ArrayList<String> epsilon = new ArrayList<>(Arrays.asList(""));
        if(rulesOfA.contains(epsilon)  &&  isIn(A, actionTable.getFollow())){
            int ruleNumber = variablesNumbers.get(A) + rulesOfA.indexOf(epsilon);
            rulesSequence.add(ruleNumber);
            return null;
        }
        ParseTree parseTree;
        ArrayList<ParseTree> children = new ArrayList<>();
        boolean matched = false;
        for(int i = 0; i < rulesOfA.size(); ++i) {
            ArrayList<String> ruleOfA = rulesOfA.get(i);
            String firstElementOfRule = ruleOfA.get(0);
            if(isIn(firstElementOfRule, actionTable.getFirst())){
                matched = true;
                int ruleNumber = variablesNumbers.get(A) + i;
                rulesSequence.add(ruleNumber);
                for(String componentOfRule: ruleOfA){
                    if(grammar.getVariables().contains(componentOfRule)){
                        ParseTree child = buildParseTree(componentOfRule);
                        if(child != null){children.add(child);}
                    }
                    else{
                        match(componentOfRule);
                        Symbol symbol = symbolList.get(index - 1);
                        children.add(new ParseTree(symbol));
                    }
                }
                break;
            }
        }
        if(!matched){throwMismatchException(A);}
        Symbol symbol = new Symbol(null, A);
        parseTree = new ParseTree(symbol, children);
        return parseTree;
    }


    /**
     * This method recursively builds the parse tree from the start symbol
     * and returns the latex representation of it
     *
     * @return the latex representation of the parse tree
     */
    public String parse() throws Exception{
        this.index = 0;
        ParseTree parseTree = buildParseTree(grammar.getStartSymbol());
        String str = "";
        for(int i = 0; i < rulesSequence.size(); i++){str += rulesSequence.get(i) + " ";}
        System.out.println(str);
        return parseTree.toLaTeX();
    }


}
