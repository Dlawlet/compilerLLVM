package Main;

import Grammar.Grammar;
import Grammar.PmpGrammar;
import Parser.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {
    /**
     * This method reads the file given as argument and writes on the standard output stream
     * the leftmost derivation and optionally writes the parse tree in the specified latex file
     *
     * @param args the arguments of the program ({arg[0], arg[1], arg[2]} = {sourceFile.pmp, "", ""} or {-wt, sourceFile.tex, sourceFile.pmp})
     * @throws Exception the exception thrown if a problem occurs during the execution of the method
     */
    public static void main(String[] args) throws Exception{
        if(args.length < 1 || args.length > 3){
            System.out.println("Usage: java -jar part2.jar sourceFile.pmp\n"
                    + "or     java -jar part2.jar -wt sourceFile.tex sourceFile.pmp");
            System.exit(0);
        }

        String destinationFile = null;
        for(int i = 0; i < args.length - 1; ++i){
            if(args[i].equals("-wt")){destinationFile = args[i+1];}
        }
        
        LexicalAnalyzer analyzer  = new LexicalAnalyzer(new java.io.FileReader(args[args.length - 1]));
        ArrayList<Symbol> symbolList = getTokens(analyzer);

        Grammar G = new PmpGrammar();

        Map<String, LexicalUnit> terminalsMap = new HashMap<String, LexicalUnit>(){
            {
                put("begin", LexicalUnit.BEG );
                put("end", LexicalUnit.END);
                put("...", LexicalUnit.DOTS);
                put("[VarName]", LexicalUnit.VARNAME);
                put(":=", LexicalUnit.ASSIGN);
                put("[Number]", LexicalUnit.NUMBER);
                put("(", LexicalUnit.LPAREN);
                put(")", LexicalUnit.RPAREN);
                put("{", LexicalUnit.LBRACK);
                put("}", LexicalUnit.RBRACK);
                put("and", LexicalUnit.AND);
                put("or", LexicalUnit.OR);
                put("-", LexicalUnit.MINUS);
                put("+", LexicalUnit.PLUS);
                put("*", LexicalUnit.TIMES);
                put("/", LexicalUnit.DIVIDE);
                put("if", LexicalUnit.IF);
                put("then", LexicalUnit.THEN);
                put("else", LexicalUnit.ELSE);
                put("=", LexicalUnit.EQUAL);
                put("<", LexicalUnit.SMALLER);
                put("while", LexicalUnit.WHILE);
                put("do", LexicalUnit.DO);
                put("print", LexicalUnit.PRINT);
                put("read", LexicalUnit.READ);
            }
        };

        Parser parser = new Parser(G, symbolList, terminalsMap);
        String latexParseTree = parser.parse();
        if(destinationFile != null){
            FileCreator file = new FileCreator();
            file.createFile(destinationFile);
            file.setContent(destinationFile, latexParseTree);
        }
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

}