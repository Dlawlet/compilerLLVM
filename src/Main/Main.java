package Main;

import Grammar.Grammar;
import Grammar.PmpGrammar;
import Parser.LLVMCreator;
import Parser.ParseTree;
import Parser.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Main {
    /**
     * This method reads the file given as argument and writes on the standard output stream the corresponding llvm code
     *
     * @param args the arguments of the program arg[0] = inputFile
     * @throws Exception the exception thrown if a problem occurs during the execution of the method
     */
    public static void main(String[] args) throws Exception{
        if(args.length != 1){
            System.out.println("Usage: java -jar part3.jar inputFile");
            System.exit(0);
        }
        parseFile(args[args.length - 1]);
    }

    /**
     * This method reads the content of a file and extracts the tokens to put them in a list
     * @param analyzer the LexicalAnalyzer
     * @return tokenList an array of symbols containing the tokens extracted from the file
     */
    private static ArrayList<Symbol> getTokens(LexicalAnalyzer analyzer){
        ArrayList<Symbol> tokenList = new ArrayList<>();
        Symbol token;
        do{
            try{
                token = analyzer.nextToken();
                if(token != null){
                    tokenList.add(token);
                }
            }catch(java.io.IOException e){
                token = null;
            }
        }while(!token.getType().equals(LexicalUnit.EOS));
        return tokenList;
    }

    /**
     * This method prints the tokens of a given symbol list
     *
     * @param tokens an array of Symbols
     */
    private static void printTokens(ArrayList<Symbol> tokens){
        for(Symbol token : tokens){
            if(token.isTerminal()){
                System.out.println(token);
            }
            else{
                System.out.println("token not recognized : " + (String)token.getValue() + " in line " + token.getLine());
            }
        }
    }

    
    /**
     * This method extracts the variables from an array of symbols and create a TreeMap containing these symbols
     *
     * @param tokenList an array of symbols potentially containing variables
     * @return variables a TreeMap containing the variables of a given array of Symbols
     */
    private static TreeMap<String, Symbol> getVariables(ArrayList<Symbol> tokenList){
        TreeMap<String, Symbol> variables = new TreeMap<>();
        for(Symbol token : tokenList) {
            if (token.getType() == LexicalUnit.VARNAME) {
                if (!variables.containsKey(token.getValue())) {
                    variables.put((String) token.getValue(), token);
                }
            }
        }
        return variables;
    }
 
    /**
     * This method prints the variables table
     *
     * @param variables a Treemap of Symbols and their associated strings
     */
    private static void printVariablesTable(TreeMap<String, Symbol> variables){
        Symbol token;
        System.out.println("\nVariables");
        for (String varname : variables.keySet()) {
            token =  variables.get(varname);
            System.out.println(token.getValue()+ "	" + token.getLine());
        }
    }

    /**
     * This method builds a terminal map associating the terminals of the FORTRESS grammar
     * with the corresponding lexical units
     *
     * @return the terminal map built
     */
    private static Map<String, LexicalUnit> getTerminalsMap(){
        Map<String, LexicalUnit> terminalsMap = new HashMap<String, LexicalUnit>(){
            {
                put("begin", LexicalUnit.BEG);
                put("...", LexicalUnit.DOTS);
                put("end", LexicalUnit.END);
                put("[VarName]", LexicalUnit.VARNAME);
                put(":=", LexicalUnit.ASSIGN);
                put("[Number]", LexicalUnit.NUMBER);
                put("(", LexicalUnit.LPAREN);
                put(")", LexicalUnit.RPAREN);
                put("{", LexicalUnit.LBRACK);
                put("}", LexicalUnit.RBRACK);
                put("-", LexicalUnit.MINUS);
                put("+", LexicalUnit.PLUS);
                put("*", LexicalUnit.TIMES);
                put("/", LexicalUnit.DIVIDE);
                put("if", LexicalUnit.IF);
                put("then", LexicalUnit.THEN);
                put("else", LexicalUnit.ELSE);
                put("and", LexicalUnit.AND);
                put("or",LexicalUnit.OR);
                put("=", LexicalUnit.EQUAL);
                put("<", LexicalUnit.SMALLER);
                put("while", LexicalUnit.WHILE);
                put("do", LexicalUnit.DO);
                put("print", LexicalUnit.PRINT);
                put("read", LexicalUnit.READ);
            }
        };
        return terminalsMap;
    }

    /**
     * This method parses a given file, produces and prints the corresponding llvm code
     *
     * @param fileName the name of the file that is parsed
     * @throws Exception the exception that may be thrown during the parsing process
     */
    private static void parseFile(String fileName) throws Exception{
        LexicalAnalyzer analyzer  = new LexicalAnalyzer(new java.io.FileReader(fileName));
        ArrayList<Symbol> symbolList = getTokens(analyzer);
        Grammar G = new PmpGrammar();
        Map<String, LexicalUnit> terminalsMap = getTerminalsMap();
        Parser parser = new Parser(G, symbolList, terminalsMap);
        ParseTree parseTree = parser.parse();
        parser.buildAST(parseTree);
        
        LLVMCreator llvmCreator = new LLVMCreator(parseTree);
        System.out.println(llvmCreator.getCode());
    }

}

