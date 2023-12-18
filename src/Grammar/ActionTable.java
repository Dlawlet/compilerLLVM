package Grammar;

import java.util.*;

public class ActionTable {
    private Grammar grammar;
    private Map<String, Set<String>> first;
    private Map<String, Set<String>> follow;
    private Map<ArrayList<String>, Set<ArrayList<String>>> actionTable;


    /**
     * Constructs an action table
     *
     * @param G the grammar from which the action table is built
     */
    public ActionTable(Grammar G) {
        this.grammar = G;
        this.first = buildFirst();
        this.follow = buildFollow();
        this.actionTable = new HashMap<>();
        buildActionTable();

    }


    //Getters
    public Grammar getGrammar(){return grammar;}
    public Map<String, Set<String>> getFirst(){return first;}
    public Map<String, Set<String>> getFollow(){return follow;}
    public Map<ArrayList<String>, Set<ArrayList<String>>> getActionTable(){return actionTable;}


    //Setters
    public void setGrammar(Grammar grammar) {this.grammar = grammar;}
    public void setFirst(Map<String, Set<String>> first){this.first = first;}
    public void setFollow(Map<String, Set<String>> follow){this.follow = follow;}
    public void setActionTable(Map<ArrayList<String>, Set<ArrayList<String>>> actionTable){this.actionTable = actionTable;}


    //First set methods

    /**
     * This method computes the current first set for a variable A
     * at a given step of the building of the first set of the grammar
     *
     * @param A the variable whose first set is computed
     * @param previousFirstA the first set of A computed at the previous step
     * @param first the current first set of the grammar
     * @return currentFirstA the first set of A computed at the current step
     */
    private Set<String> buildCurrentFirstA(String A, Set<String> previousFirstA, Map<String, Set<String>> first){
        Set<String> currentFirstA = new HashSet<>(previousFirstA);
        Map<String, ArrayList<ArrayList<String>>> rules = this.grammar.getRules();
        for(ArrayList<String> ruleOfA : rules.get(A)){
            currentFirstA.addAll(first.get(ruleOfA.get(0)));
        }
        return currentFirstA;
    }


    /**
     * This method builds the first set of the grammar (containing the first sets of variables and terminals)
     *
     * @return first the first set of the grammar (containing the first sets of each symbol)
     */
    public Map<String, Set<String>> buildFirst(){
        Map<String, Set<String>> first = new HashMap<>();
        for(String a : this.grammar.getTerminals()){
            Set<String> firsta = new HashSet<>();
            firsta.add(a);
            first.put(a, firsta);
        }
        for(String A : this.grammar.getVariables()){
            Set<String> firstA = new HashSet<>();
            first.put(A, firstA);
        }
        boolean stable;
        do{
            stable = true;
            for(String A : this.grammar.getVariables()){
                Set<String> previousFirstA = first.get(A);
                Set<String> currentFirstA = buildCurrentFirstA(A, previousFirstA, first);
                if(stable){stable = previousFirstA.equals(currentFirstA);}
                first.put(A, currentFirstA);
            }
        }while(!stable);
        return first;
    }


    /**
     * This method builds a string representation of the first set of the grammar
     *
     * @param V the variables of the grammar
     * @return str a string representation of the first set of the grammar
     */
    private String firstSetToString(Collection<String> V){
        String str = "";
        for(String A : V){
            str += A + "  : { " ;
            for(String elemOfFirstA : first.get(A)){
                if(elemOfFirstA != ""){str += elemOfFirstA + " ";}
                else{str += "epsilon ";}
            }
            str += "}\n";
        }
        return str;
    }


    //Follow set methods

    /**
     * This method computes the current follow set for a variable
     * at a given step of the building of the follow set of the grammar
     *
     * @param B the left-hand side of the rule associated with the current step of building the follow set of A
     * @param beta the suffix following the variable A in the right-hand side of the rule
     * @param follow the current follow set of the grammar
     * @param previousFollowA the follow set of A computed at the previous step
     * @return currentFollowA the follow set of A computed at the current step
     */
    private Set<String> buildCurrentFollowA(String B, String beta, Map<String, Set<String>> follow, Set<String> previousFollowA){
        Set<String> currentFollowA = new HashSet<>(previousFollowA);
        if (beta != null){
            currentFollowA.addAll(this.first.get(beta));
            currentFollowA.remove("");
        }
        if(beta == null || this.first.get(beta).contains("")){
            Set<String> followB = follow.get(B);
            if (followB != null){currentFollowA.addAll(followB);}
        }
        return currentFollowA;
    }


    /**
     * This method builds the follow set of the grammar (containing the follow sets of the variables of the grammar)
     *
     * @return follow the follow set of the grammar (containing the follow sets of each variable)
     */
    public Map<String, Set<String>> buildFollow(){
        Map<String, Set<String>> follow = new HashMap<>();
        for(String A : this.grammar.getVariables()){
            if(A != this.grammar.getStartSymbol()){follow.put(A, new HashSet<>());}
            else {follow.put(A, null);}
        }
        boolean stable;
        do{
            stable = true;
            Map<String, ArrayList<ArrayList<String>>> rules = this.grammar.getRules();
            for(Map.Entry<String, ArrayList<ArrayList<String>>> P : rules.entrySet()){
                String B = P.getKey();
                if(this.grammar.getVariables().contains(B)){
                    for(ArrayList<String> ruleOfB : P.getValue()){
                        for(int i = 0; i < ruleOfB.size(); i++){
                            String A = ruleOfB.get(i);
                            if(this.grammar.getVariables().contains(A)){
                                String beta = null;
                                if(i+1 < ruleOfB.size()){beta = ruleOfB.get(i+1);}
                                Set<String> previousFollowA = follow.get(A);
                                if(previousFollowA != null){
                                    Set<String> currentFollowA = buildCurrentFollowA(B, beta, follow, previousFollowA);
                                    if(stable){stable = previousFollowA.equals(currentFollowA);}
                                    follow.put(A, currentFollowA);
                                }
                            }
                        }
                    }
                }
            }
        }while(!stable);
        return follow;
    }


    /**
     * This method builds a string representation of the follow set of the grammar
     *
     * @param V the variables of the grammar
     * @return str a string representation of the follow set of the grammar
     */
    private String followToString(Collection<String> V){
        String str = "";
        for(String A : V){
            str += A + "  : { " ;
            if(follow.get(A) != null){
                for(String elemOfFollowA : follow.get(A)){
                    if(elemOfFollowA != ""){str += elemOfFollowA + " ";}
                    else{str += "epsilon ";}
                }
            }
            str += "}\n";
        }
        return str;
    }


    //Action Table methods

    /**
     * This method returns the entry of the action table corresponding to the variable A and the terminal a
     *
     * @param A a variable of the grammar
     * @param a a terminal of the grammar
     * @return the rules linking the variable A and the terminal a
     */
    private Set<ArrayList<String>> getActionTableEntry(String A, String a){
        ArrayList<String> actionTableIndex = new ArrayList<>();
        actionTableIndex.add(A);
        actionTableIndex.add(a);
        return this.actionTable.get(actionTableIndex);
    }


    /**
     * This function adds an entry to the action table
     * (a rule producing a given terminal from a given variable)
     *
     * @param A a variable of the grammar
     * @param a a terminal of the grammar
     * @param ruleOfA a rule of the grammar linking the variable A and the terminal a
     */
    private void addActionTableEntry(String A, String a, ArrayList<String> ruleOfA){
        Set<ArrayList<String>> actionTableEntry = getActionTableEntry(A, a);
        if(actionTableEntry == null){
            actionTableEntry = new HashSet<>(Arrays.asList(ruleOfA));
            this.actionTable.put(new ArrayList<>(Arrays.asList(A, a)), actionTableEntry);
        }
        else{actionTableEntry.add(ruleOfA);}
    }


    /**
     * This method builds the action table of the current grammar.
     */
    public void buildActionTable(){
        // Retrieve the grammar rules as a map, where the key is a non-terminal and the value is a list of production rules
        Map<String, ArrayList<ArrayList<String>>> rules = this.grammar.getRules();

        // Iterate over each entry (non-terminal and its production rules) in the grammar rules map
        for(Map.Entry<String, ArrayList<ArrayList<String>>> P : rules.entrySet()){
            String A = P.getKey(); // 'A' is the current non-terminal
            
            // Iterate over the production rules of the current non-terminal
            for(int i = 0; i < P.getValue().size(); i++){
                ArrayList<String> ruleOfA = P.getValue().get(i); // 'ruleOfA' is the current production rule for 'A'
                
                // Iterate over each symbol in the current production rule
                for(int j = 0; j < ruleOfA.size(); j++){
                    String alpha = ruleOfA.get(j); // 'alpha' is the current symbol in the production rule
                    boolean containsEpsilon = false; // Flag to check if the First set contains an epsilon
                    
                    // Iterate over the First set of the current symbol 'alpha'
                    for(String a : this.first.get(alpha)){
                        if(Objects.equals(a, "")){ 
                            containsEpsilon = true; // Mark that epsilon is found in the First set
                        }
                        else{
                            // If 'a' is not epsilon, add an entry to the action table for the combination of 'A' and 'a'
                            addActionTableEntry(A, a, ruleOfA);
                        }
                    }

                    // If epsilon is in the First set of 'alpha'
                    if (containsEpsilon){
                        // Check if there are Follow set entries for 'A'
                        if(this.follow.get(A) != null){
                            // Iterate over the Follow set of 'A'
                            for(String a : this.follow.get(A)){
                                if(!Objects.equals(a, "")){
                                    // Add action table entries for each symbol in the Follow set of 'A' (except for epsilon)
                                    addActionTableEntry(A, a, ruleOfA);
                                }
                            }
                        }
                    }
                    else{
                        // If epsilon is not in the First set of 'alpha', break the innermost loop
                        break;
                    }
                }
            }
        }
    }



    /**
     *  This method builds a string representation of the action table
     *
     * @param V the variables of the grammar
     * @param variablesNumbers a Map which associates to each variable of the grammar the number of the rule in which it appears for the first time
     * @return str a string representation of the action table
     */
    private String actionTableToString(Collection<String> V, Map<String, Integer> variablesNumbers){
        String str = "";
        for(String A : V){
            for(String a : grammar.getTerminals()){
                Set<ArrayList<String>> rules = getActionTableEntry(A, a);
                if(rules != null){
                    String strOfRule = "";
                    for(ArrayList<String> rule : rules){
                        if(grammar.getOrder() != null){
                            strOfRule += (grammar.getRulesOf(A).indexOf(rule) +  variablesNumbers.get(A)) + " ";
                        }
                        else{
                            for(String componentOfRule : rule){
                                strOfRule += componentOfRule;
                            }
                            strOfRule += " ";
                        }
                    }
                    str += "[ " + A + "   " +  a + " ] = " +  strOfRule + "\n";
                }
            }
        }
        return str;
    }


    /**
     * This method checks whether each entry of the action table of the current grammar
     * contains at most n rules
     *
     * @param n the maximum number of rules for an entry of the action table
     * @return true if the grammar is LL(n) or false otherwise (for n = 1 the method returns true if the grammar is LL(1))
     */
    public boolean isLL(int n){
        for(String A : this.grammar.getVariables()){
            for(String a : this.grammar.getTerminals()){
                Set<ArrayList<String>> actionTableEntry = getActionTableEntry(A, a);
                if(actionTableEntry != null){
                    int nbRules = actionTableEntry.size();
                    if(nbRules > n){return false;}
                }
            }
        }
        return true;
    }


    /**
     * This method builds a string representation of all the elements related to the action table (first set, follow set, action table)
     *
     * @return str a string representation of the whole class (first set, follow set, action table)
     */
    public String toString(){
        Map<String, Integer> variablesNumbers = this.grammar.findVariablesNumbers();
        Collection<String> V = this.grammar.getVariables();
        if(variablesNumbers.size() > 0){V = this.grammar.getOrder();}
        String str = "";
        str += "First : \n" + firstSetToString(V) + "\n";
        str += "Follow : \n" + followToString(V) + "\n\n";
        str += "Action Table : \n" + actionTableToString(V, variablesNumbers);
        return str;
    }


}
