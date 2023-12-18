package Grammar;

import java.util.*;

public class GrammarTransformation {


    /**
     * This method searches for the productive variables of the Grammar G
     * at a given step of the removal of unproductive variables
     *
     * @param G the grammar in which the search of the productive variables is performed
     * @param previousV the set of productive variables computed at the previous step of the removal of unproductive variables
     * @return currentV the set of productive variables computed at the current step of the removal of unproductive variables
     */
    private static Set<String> findProductiveVariables(Grammar G, Set<String> previousV){
        Set<String> currentV = new HashSet<>();
        Map<String, ArrayList<ArrayList<String>>> rules = G.getRules();
        Set<String> alpha_set = G.getTerminals();
        alpha_set.addAll(previousV);
        for(Map.Entry<String, ArrayList<ArrayList<String>>> P : rules.entrySet()){
            String A = P.getKey();
            for(ArrayList<String> rule : P.getValue()){
                boolean productive = true;
                int i = 0;
                while(productive && i < rule.size()){
                    String ruleComponent = rule.get(i);
                    if(!alpha_set.contains(ruleComponent)){productive = false;}
                    else {i++;}
                }
                if(productive){
                    currentV.add(A);
                    break;
                }
            }
        }
        currentV.addAll(previousV);
        return currentV;
    }


    /**
     * This method searches for the productive rules of a grammar given a set of productive variables
     *
     * @param G the grammar from which the productive rules must be extracted
     * @param productiveVariables a set of productive variables
     * @return Pprime the productive rules of the grammar
     */
    private static Map<String, ArrayList<ArrayList<String>>> findProductiveRules(Grammar G, Set<String> productiveVariables){
        Map<String, ArrayList<ArrayList<String>>> rules = G.getRules();
        Map<String, ArrayList<ArrayList<String>>> Pprime = new HashMap<>();
        Set<String> productive_symbols = G.getTerminals();
        productive_symbols.addAll(productiveVariables);
        Set<String> unproductive_symbols = G.getVariables();
        unproductive_symbols.removeAll(productiveVariables);
        for(Map.Entry<String, ArrayList<ArrayList<String>>> P : rules.entrySet()){
            String A = P.getKey();
            if(!unproductive_symbols.contains(A)){
                ArrayList<ArrayList<String>> productiveRulesOfA = new ArrayList<>();
                for(ArrayList<String> ruleOfA : P.getValue()){
                    boolean productive = true;
                    int i = 0;
                    while(productive && i<ruleOfA.size()){
                        String ruleComponent = ruleOfA.get(i);
                        if(!productive_symbols.contains(ruleComponent)){productive = false;}
                        else {i++;}
                    }
                    if(productive){productiveRulesOfA.add(ruleOfA);}
                }
                if(!productiveRulesOfA.isEmpty()){Pprime.put(A, productiveRulesOfA);}
            }
        }
        return Pprime;
    }


    /**
     * This method updates the order of the variables according to the current variables of the grammar
     * @param G the grammar whose variables have been previously updated
     * @param currentVariables the current variables of the grammar
     * @return newOrder the order of the grammar variables after the update of the variables
     */
    private static ArrayList<String> findNewOrder(Grammar G, Set<String> currentVariables){
        ArrayList<String> newOrder = null;
        if(G.getOrder() != null){
            ArrayList<String> previousOrder = new ArrayList<>(G.getOrder());
            newOrder = new ArrayList<>(G.getOrder());
            previousOrder.removeAll(currentVariables);
            newOrder.removeAll(previousOrder);
        }
        return newOrder;
    }


    /**
     * This method removes the unproductive symbols from a Grammar G
     *
     * @param G the grammar potentially containing unproductive symbols
     * @return Gprime the grammar produced by removing the unproductive symbols from the grammar G
     */
    public static Grammar removeUnproductive(Grammar G){
        Set<String> previousV = new HashSet<>();
        Set<String> currentV = new HashSet<>();
        do{
            previousV = currentV;
            currentV = findProductiveVariables(G, previousV);
        }while (!currentV.equals(previousV));
        Set<String> Vprime = currentV;
        Map<String, ArrayList<ArrayList<String>>> Pprime = findProductiveRules(G, Vprime);
        ArrayList<String> newOrder = findNewOrder(G, Vprime);
        Grammar Gprime = new Grammar(Vprime, G.getTerminals(), Pprime, G.getStartSymbol(), newOrder);
        return Gprime;
    }


    /**
     * This method searches for the accessible variables of the Grammar G
     * at a given step of the removal of inaccessible variables
     *
     * @param G the grammar in which the search of the accessible variables is performed
     * @param previousV the set of accessible variables computed at the previous step of the removal of inaccessible variables
     * @return currentV the set of accessible variables computed at the current step of the removal of inaccessible variables
     */
    private static Set<String> findAccessibleVariables(Grammar G, Set<String> previousV){
        Map<String, ArrayList<ArrayList<String>>> rules = G.getRules();
        Set<String> currentV = new HashSet<>();
        for(Map.Entry<String, ArrayList<ArrayList<String>>> P : rules.entrySet()){
            String A = P.getKey();
            if(previousV.contains(A)){
                for(ArrayList<String> ruleOfA : P.getValue()){
                    for(String ruleComponent : ruleOfA){
                        currentV.add(ruleComponent);
                    }
                }
            }
        }
        currentV.addAll(previousV);
        return currentV;
    }


    /**
     * This method searches for the accessible rules of a grammar given a set of accessible variables
     *
     * @param G the grammar from which the accessible rules must be extracted
     * @param accessibleVariables a set of accessible variables
     * @return Pprime the accessible rules of the grammar
     */
    private static Map<String, ArrayList<ArrayList<String>>> findAccessibleRules(Grammar G, Set<String> accessibleVariables) {
        Map<String, ArrayList<ArrayList<String>>> Pprime = new HashMap<>();
        Map<String, ArrayList<ArrayList<String>>> rules = G.getRules();
        for(Map.Entry<String, ArrayList<ArrayList<String>>> P : rules.entrySet()){
            String A = P.getKey();
            ArrayList<ArrayList<String>> accessibleRulesOfA = new ArrayList<>();
            if(accessibleVariables.contains(A)){
                for(ArrayList<String> ruleOfA : P.getValue()){
                    boolean accessible = true;
                    int i = 0;
                    while(accessible && i < ruleOfA.size()){
                        String ruleComponent = ruleOfA.get(i);
                        if(accessibleVariables.contains(ruleComponent)){i++;}
                        else {accessible = false;}
                    }
                    if(accessible){accessibleRulesOfA.add(ruleOfA);}
                }
                if(!accessibleRulesOfA.isEmpty()){Pprime.put(A, accessibleRulesOfA);}
            }
        }
        return Pprime;
    }


    /**
     * This method removes the inaccessible symbols from a Grammar G
     *
     * @param G the grammar potentially containing inaccessible symbols
     * @return Gprime the grammar produced by removing the inaccessible symbols from the grammar G
     */
    public static Grammar removeInaccessible(Grammar G){
        Set<String> previousV = new HashSet<>();
        Set<String> currentV = new HashSet<>();
        currentV.add(G.getStartSymbol());

        do {
            previousV = currentV;
            currentV = findAccessibleVariables(G, previousV);
        }while (!previousV.equals(currentV));

        Set<String> V = G.getVariables();
        Set<String> T = G.getTerminals();


        Set<String> Vprime = new HashSet<>();
        Vprime.addAll(currentV);
        Vprime.retainAll(V);


        Set<String> Tprime = new HashSet<>();
        Tprime.addAll(currentV);
        Tprime.retainAll(T);

        ArrayList<String> newOrder = findNewOrder(G, Vprime);
        Map<String, ArrayList<ArrayList<String>>> Pprime = findAccessibleRules(G,previousV);
        Grammar Gprime = new Grammar(Vprime, Tprime, Pprime, G.getStartSymbol(), newOrder);
        return  Gprime;
    }


    /**
     * This method removes the useless symbols from a Grammar G
     * @param G a grammar potentially containing useless symbols
     * @return G2 the grammar produced by removing the useless symbols from the grammar G
     */
    public static Grammar removeUseless(Grammar G){
        Grammar G1 = removeUnproductive(G);
        Grammar G2 = removeInaccessible(G1);
        return G2;
    }


}
