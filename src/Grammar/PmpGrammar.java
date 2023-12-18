package Grammar;

import java.util.*;

public class PmpGrammar extends Grammar {
    /**
     * Creates the Pascalmp grammar
     */
    public PmpGrammar(){
        super();
        generatePmpTerminals();
        generatePmpVariables();
        generatePmpStartSymbol();
        generatePmpRules();
    }

    /**
     * This method generates the terminals of the PASCALMP grammar
     */
    public void generatePmpTerminals(){
        Set<String> T = new HashSet<>(Arrays.asList(
                "begin", "end",
                "",
                "...",
                "[VarName]", ":=",
                "[Number]", "(", ")", "-",
                "+", "*", "/",
                "if", "then", "else",
                "and","or",
                "{","}",
                "=", "<",
                "while", "do",
                "print",
                "read"));
        this.setTerminals(T);
    }

    /**
     * This method generates the variables of the PASCALMP grammar
     */
    public void generatePmpVariables(){
        ArrayList<String> V = new ArrayList<>(Arrays.asList(
                "<Program>",
                "<Code>",
                "<InstList>",
                "<EndInstList>",
                "<Instruction>",
                "<Assign>",
                "<ExprArith>",
                "<T>", "<U>",
                "<If>",
                "<EndIf>",
                "<Cond>",
                "<V>", "<W>",
                "<SimpleCond>",
                "<Comp>",
                "<While>",
                "<Print>",
                "<Read>"));
        this.setVariables(new HashSet<>(V));
        this.setOrder(V);
    }

    /**
     * This method generates the start symbol of the PASCALMP grammar
     */
    public void generatePmpStartSymbol(){
        String S = "<Program>";
        this.setStartSymbol(S);
    }

    /**
     * This method generates the production rules of the PASCALMP grammar
     */
    public void generatePmpRules(){
        Map<String, ArrayList<ArrayList<String>>> P = new HashMap<>();

        ArrayList<ArrayList<String>> tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("begin", "<Code>", "end"))));
        P.put("<Program>", tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("")),
                new ArrayList<>(Arrays.asList("<InstList>"))));
        P.put("<Code>", tmp);

        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("<Instruction>")),
                new ArrayList<>(Arrays.asList("<Instruction>","...","<InstList>"))));
        P.put("<InstList>", tmp);

        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("<Assign>")),
                new ArrayList<>(Arrays.asList("<If>")),
                new ArrayList<>(Arrays.asList("<While>")),
                new ArrayList<>(Arrays.asList("<For>")),
                new ArrayList<>(Arrays.asList("<Print>")),
                new ArrayList<>(Arrays.asList("<Read>")),
                new ArrayList<>(Arrays.asList("begin", "<InstList>", "end"))));
        P.put("<Instruction>", tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("[VarName]", ":=", "<ExprArith>"))));
        P.put("<Assign>", tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("<ExprArith>", "+", "<T>")),
                new ArrayList<>(Arrays.asList("<ExprArith>", "-", "<T>")),
                new ArrayList<>(Arrays.asList("<T>"))));
        P.put("<ExprArith>", tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("<T>", "*", "<U>")),
                new ArrayList<>(Arrays.asList("<T>", "/", "<U>")),
                new ArrayList<>(Arrays.asList("<U>"))));
        P.put("<T>", tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("[VarName]")),
                new ArrayList<>(Arrays.asList("[Number]")),
                new ArrayList<>(Arrays.asList("(", "<ExprArith>", ")")),
                new ArrayList<>(Arrays.asList("-", "<U>"))));
        P.put("<U>", tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("if", "<Cond>",  "then", "<Instruction>", "else")),
                new ArrayList<>(Arrays.asList("if", "<Cond>",  "then", "<Instruction>", "else", "<Instruction>"))));
        P.put("<If>" ,tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("<Cond>", "or", "<V>")),
                new ArrayList<>(Arrays.asList("<V>"))));
        P.put("<Cond>", tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("<V>", "and", "<W>")),
                new ArrayList<>(Arrays.asList("<W>"))));
        P.put("<V>", tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("{","<Cond>","}")),
                new ArrayList<>(Arrays.asList("<SimpleCond>"))));
        P.put("<W>", tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("<ExprArith>", "<Comp>", "<ExprArith>"))));
        P.put("<SimpleCond>" ,tmp);

        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("=")),
                new ArrayList<>(Arrays.asList("<"))));
        P.put("<Comp>", tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("while", "<Cond>", "do", "<Instruction>"))));
        P.put("<While>" ,tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("print", "(", "[VarName]", ")"))));
        P.put("<Print>", tmp);


        tmp = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("read", "(", "[VarName]", ")"))));
        P.put("<Read>", tmp);

        this.setRules(P);
    }


}
