package Grammar;

import java.util.*;

public class Grammar{
    private Set<String> variables;
    private Set<String> terminals;
    private Map<String, ArrayList<ArrayList<String>>> rules;
    private String startSymbol;
    private ArrayList<String> order;


    /**
     * Creates a grammar G = ( V,T,P,S )
     *
     * @param variables V a finite set of variables
     * @param terminals T a finite set of terminals
     * @param rules P a finite set of production rules
     * @param startSymbol S the start symbol
     * @param order an array representing the order to give to the variables
     */
    public Grammar(Set<String> variables, Set<String> terminals, Map<String, ArrayList<ArrayList<String>>> rules, String startSymbol, ArrayList<String> order) {
        this.variables = variables;
        this.terminals = terminals;
        this.rules = rules;
        this.startSymbol = startSymbol;
        this.order = order;
    }


    /**
     * Default Constructor
     */
    public Grammar(){
        this.variables = null;
        this.terminals = null;
        this.rules = null;
        this.startSymbol = null;
        this.order = null;
    }


    //Getters
    public Set<String> getVariables() {return variables;}
    public Set<String> getTerminals() {return terminals;}
    public Map<String, ArrayList<ArrayList<String>>> getRules() {return rules;}
    public String getStartSymbol() {return startSymbol;}
    public ArrayList<String> getOrder() {return order;}


    //Setters
    public void setVariables(Set<String> variables) {this.variables = variables;}
    public void setTerminals(Set<String> terminals) {this.terminals = terminals;}
    public void setRules(Map<String, ArrayList<ArrayList<String>>> rules) {this.rules = rules;}
    public void setStartSymbol(String startSymbol) {this.startSymbol = startSymbol;}
    public void setOrder(ArrayList<String> order) {this.order = order;}


    /**
     * This method returns the variables having at least 2 rules in which a same prefix occurs
     * @return factorisableVariables a HashMap having such variables as keys and the corresponding prefixes as values
     */
    private Map<String, ArrayList<String>> findFactorisableVariables(){
        Map<String, ArrayList<String>> factorisableVariables = new HashMap<>();
        for(Map.Entry<String, ArrayList<ArrayList<String>>> P : this.getRules().entrySet()){
            String A = P.getKey();
            ArrayList<String> prefix = new ArrayList<>();
            boolean common = true;
            for(int i = 0; i < P.getValue().size(); i++){
                ArrayList<String> firstRule = P.getValue().get(i);
                for (int j = 0; j < i; j++){
                    ArrayList<String> secondRule = P.getValue().get(j);
                    common = true;
                    for(int k = 0; k < firstRule.size() && k < secondRule.size(); k++){
                        String firstPrefix = firstRule.get(k);
                        String secondPrefix = secondRule.get(k);
                        if(firstPrefix != secondPrefix){common = false;}
                        if(common){prefix.add(firstPrefix);}
                        else{break;}
                    }
                    if (!common){break;}
                }
                if (!common){break;}
            }
            if(!prefix.isEmpty()){factorisableVariables.put(A,prefix);}
        }
        return factorisableVariables;
    }


    /**
     * This method searches for the rules having the variable A as left-hand side
     * and having in their right-hand side the specified prefix
     *
     * @param A the left-hand side of the considered rules
     * @param prefix the prefix occurring in the right-hand side of the rules
     * @return prefixRules an array of the right-hand sides of the rules in which the prefix occurs
     */
    private ArrayList<ArrayList<String>> findPrefixedRules(String A, ArrayList<String> prefix) {
        ArrayList<ArrayList<String>> rulesOfA = this.getRulesOf(A);
        ArrayList<ArrayList<String>> rulesWithPrefix = new ArrayList<>();
        for(int i = rulesOfA.size() - 1; i > -1; i--){
            ArrayList<String> rule = rulesOfA.get(i);
            boolean containsPrefix = rule.size() >= prefix.size();
            for(int j = 0; containsPrefix && j < prefix.size(); j++){
                if(rule.get(j) != prefix.get(j)){containsPrefix = false;}
            }
            if(containsPrefix){
                rulesWithPrefix.add(rule);
                rulesOfA.remove(i);
            }
        }
        return rulesWithPrefix;
    }


    /**
     * This method generates a variable for the specified left-hand side
     *
     * @param A the variable of the grammar for which it is necessary to generate a new variable
     * @return var the generated variable
     */
    private String generateVariableFor(String A){
        String var = A + "'";
        int offset = 1;
        while(this.getVariables().contains(var)){
            var += "'";
            offset++;
        }
        this.getVariables().add(var);
        if(this.order != null){
            int positionOfVar = this.order.indexOf(A) + offset;
            this.order.add(positionOfVar, var);
        }
        return var;
    }


    /**
     * This method adds factorised rules in the set of rules of the grammar
     *
     * @param A the variable of the grammar associated with the factorisation step
     * @param newVar the variable created during the factorisation step
     * @param prefixRules the rules having a common prefix
     * @param prefix the prefix shared by the rules to be factorised
     */
    private void addFactorisedRules(String A, String newVar, ArrayList<ArrayList<String>> prefixRules, ArrayList<String> prefix) {
        Map<String,ArrayList<ArrayList<String>>> rules = this.getRules();

        if(rules.get(A) == null){
            ArrayList<ArrayList<String>> aRules = new ArrayList<>();
            rules.put(A, aRules);
        }

        ArrayList<ArrayList<String>> newRules = new ArrayList<>();
        rules.put(newVar, newRules);
        ArrayList<String> alphaPrefix = new ArrayList<>(prefix);
        alphaPrefix.add(newVar);
        rules.get(A).add(alphaPrefix);

        for(ArrayList<String> rule : prefixRules){
            ArrayList<String> suffix = new ArrayList<>();
            for(int i = prefix.size(); i < rule.size(); i++) {
                    suffix.add(rule.get(i));
            }
            if (suffix.isEmpty()) {
                suffix.add(""); // Add epsilon if the suffix is empty
            }
            rules.get(newVar).add(suffix);
        }
    }


    /**
     * Applies left factoring to the current grammar
     *
     */
    public void leftFactor(){
        Map<String, ArrayList<String>> factorisableVariables;
        do{
            factorisableVariables = this.findFactorisableVariables();
            for(Map.Entry<String, ArrayList<String>> P : factorisableVariables.entrySet()){
                String A = P.getKey();
                ArrayList<String> prefix = P.getValue();
                ArrayList<ArrayList<String>> prefixRules = this.findPrefixedRules(A,prefix);
                String newVar = generateVariableFor(A);
                addFactorisedRules(A,newVar,prefixRules,prefix);
            }
        }while (factorisableVariables.size() > 0);
    }


    /**
     * This method searches for the variables of the grammar associated with at least one rule in which a left recursion occurs
     *
     * @return recursiveVariables the set of variables associated with at least one left-recursive rule
     */
    private Set<String> findRecursiveVariables() {
        Set<String> recursiveVariables = new HashSet<>();
        for(Map.Entry<String, ArrayList<ArrayList<String>>> P : this.getRules().entrySet()){
            String A = P.getKey();
            boolean leftRecursive = false;
            for(ArrayList<String> rule : P.getValue()){
                int i = 0;
                while(!leftRecursive && i<rule.size()){
                    String prefix = rule.get(i);
                    if(prefix == A && i < rule.size() - 1){
                        recursiveVariables.add(A);
                        leftRecursive = true;
                    }
                    if(!leftRecursive) {i++;}
                }
                if(leftRecursive){break;}
            }
        }
        return recursiveVariables;
    }


    /**
     * This method returns the rules associated with the specified variable
     *
     * @param A a variable of the grammar
     * @return the rules of A
     */
    public ArrayList<ArrayList<String>> getRulesOf(String A) {return this.getRules().get(A);}


    /**
     * This method removes the rules associated to the variable A
     *
     * @param A a variable of the grammar
     */
    private void removeRulesOf(String A) {this.getRules().remove(A);}


    /**
     * This method updates the rules associated with the variable A
     * by adding 2 new variables U, V and the needed rules to the grammar in order to remove
     * the left-recursion associated with the variable A
     *
     * @param R the rules associated to the variable A
     * @param A the variable associated with the left-recursive rules to be updated
     * @param U the new variable associated to the non-left-recursive rules formerly associated to A
     * @param V the new variable associated to the left-recursive rules formerly associated to A
     */
    private void updateRecursiveRules(ArrayList<ArrayList<String>> R, String A, String U, String V) {
        Map<String,ArrayList<ArrayList<String>>> rules = this.getRules();
        ArrayList<ArrayList<String>> rulesOfA = new ArrayList<>();
        ArrayList<String> uvRule = new ArrayList<>();
        uvRule.add(U);
        uvRule.add(V);
        rulesOfA.add(uvRule);
        rules.put(A, rulesOfA);
        ArrayList<ArrayList<String>> rulesOfU = new ArrayList<>();
        rules.put(U, rulesOfU);
        ArrayList<ArrayList<String>> rulesOfV = new ArrayList<>();
        rules.put(V, rulesOfV);

        for(ArrayList<String> rule : R){
            boolean leftRecursive = false;

            if (!rule.isEmpty() && Objects.equals(rule.get(0), A)){
                rule.remove(0);
                leftRecursive = true;
            }

            if(!leftRecursive){
                this.getRulesOf(U).add(rule);
            }

            else{
                rule.add(V);
                this.getRulesOf(V).add(rule);
            }
        }
        ArrayList<String> epsilonRule = new ArrayList<>();
        epsilonRule.add("");
        this.getRulesOf(V).add(epsilonRule);
    }


    /**
     * This method removes the left recursion from the grammar
     *
     */
    public void removeLeftRecursion(){
        Set<String> recursiveVariables = this.findRecursiveVariables();
        while(!recursiveVariables.isEmpty()){
            for(String A : recursiveVariables) {
                ArrayList<ArrayList<String>> R = this.getRulesOf(A);
                String U = generateVariableFor(A);
                String V = generateVariableFor(A);
                this.removeRulesOf(A);
                updateRecursiveRules(R, A, U, V);
            }
            recursiveVariables = this.findRecursiveVariables();
        }
    }


    /**
     * This method finds for each variable the number of the rule in which it appears for the first time
     *
     * @return variableNumber a Map which associates to each variable of the grammar the number of the rule in which it appears for the first time
     */
    public Map<String, Integer> findVariablesNumbers(){
        Map<String, Integer> variablesNumbers = new HashMap<>();
        if(order != null){
            variablesNumbers.put(order.get(0), 1);
            for(int i = 1; i < order.size(); i++){
                int nbRulesOfPreviousVar = getRulesOf(order.get(i-1)).size();
                int posOfPreviousVar = variablesNumbers.get(order.get(i-1));
                String variableName = order.get(i);
                variablesNumbers.put(variableName, posOfPreviousVar + nbRulesOfPreviousVar);
            }
        }
        return variablesNumbers;
    }

    private String create_well_display_1(String input, int lineNumber, int A_length) {
        StringBuilder str = new StringBuilder();
        
        str.append("\n");

        // Replace " ".repeat(4 - (lineNumber + "").length())
        int spacesBeforeLineNumber = 4 - (lineNumber + "").length();
        for (int i = 0; i < spacesBeforeLineNumber; i++) {
            str.append(" ");
        }

        str.append("[").append(lineNumber).append("] ");

        // Replace " ".repeat(A_length + 4)
        int spacesAfterLineNumber = A_length + 4;
        for (int i = 0; i < spacesAfterLineNumber; i++) {
            str.append(" ");
        }

        str.append("    ");

        return str.toString();
    }

    //String representations

    private String removeTrailingSpaces(String str, int A_length) {
        int totalLengthToRemove = A_length + 4 + 4; // A_length + 4 spaces + 4 spaces for "    "

        if (str.length() > totalLengthToRemove) {
            return str.substring(0, str.length() - totalLengthToRemove);
        } else {
            // In case the string is shorter than the total length to remove
            return "";
        }
    }

    /**
     * This method builds the string representation of the rules of the variable A
     *
     * @param A the variable A whose representation is built
     * @param lineNb the line number at which the variable A appears for the first time
     * @return str the string representation of the rules of the variable A
     */
    private String variableRepresentation(String A, Integer lineNb){
        int lineNumber = lineNb;
        String str =  A + " -> " + "    ";
        for(ArrayList<String> ruleOfA : this.getRulesOf(A)){
            for(String ruleComponent : ruleOfA){
                if (ruleComponent == ""){str += "epsilon";}
                else{str += ruleComponent;}
            }
            lineNumber++;

            str = create_well_display_1(str, lineNumber, A.length());
        }
        str = removeTrailingSpaces(str,A.length());
        return str;
    }
    private String create_well_display_2(String str, int line) {
        String res = "";
        for (int i=0;i<(4 - (line+ "").length());i++){
            res += " ";
        }
        
        res += "["  + line + "] ";
        return res;
    }


    /**
     * This method builds the string representation of the rules of the grammar in case the variables are given a specific order
     *
     * @return str the string representation of the rules of the grammar
     */
    private String orderedGrammarString(){
        String str = "";
        int line = 1;
        str += create_well_display_2(str,line);
        for(String A : this.order){
            str = str + variableRepresentation(A, line);
            line += this.getRulesOf(A).size();
        }
        String str2 = "\n" + create_well_display_2(str,line);
        
        str = str.substring(0, str.length() - (str2).length());
        return str;
    }


    /**
     * This method builds the string representation of the rules of the grammar in case the variables are not given any order
     *
     * @return str the string representation of the rules of the grammar
     */
    private String unorderedGrammarString(){
        String str = "\n";
        int line = 1;
        str += create_well_display_2(str,line);
        
        for(Map.Entry<String, ArrayList<ArrayList<String>>> P : this.rules.entrySet()){
            String A = P.getKey();
            str = str + variableRepresentation(A, line);
            line += this.getRulesOf(A).size();
        }

        String str2 = "\n" + create_well_display_2(str,line);
        str = str.substring(0, str.length() - (str2).length());
        return str;
    }


    /**
     * This method builds the string representation of the grammar
     *
     * @return the string representation of the grammar
     */
    @Override
    public String toString(){
        if(order == null){return unorderedGrammarString();}
        else{return orderedGrammarString();}
    }


}
