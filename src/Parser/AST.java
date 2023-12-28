package Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import Main.LexicalUnit;

public class AST{
    /**
     * This method removes a given child from a given parse tree while maintaining the order in the tree
     *
     * @param tree the parse tree from which a child is removed
     * @param child the child to remove from the parse tree
     */
    private static void removeChild(ParseTree tree, ParseTree child){
        ArrayList<ParseTree> newChildren = new ArrayList<>();
        int indexOfChild = tree.getChildren().indexOf(child);
        for(int i = 0; i < indexOfChild; i++){
            newChildren.add(tree.getChildren().get(i));
        }
        for(int i = 0; i < child.getChildren().size(); i++){
            newChildren.add(child.getChildren().get(i));
            child.getChildren().get(i).setFather(tree);
        }
        for(int i = indexOfChild + 1; i < tree.getChildren().size(); i++){
            newChildren.add(tree.getChildren().get(i));
        }
        tree.getChildren().removeAll(tree.getChildren());
        tree.getChildren().addAll(newChildren);
    }


    /**
     * This method is used to clean a given parse tree from nodes that can be simplified
     *
     * @param tree the parse tree to be cleaned
     */
    private static void clean(ParseTree tree){
        boolean changed = true;
        while(changed){
            changed = false;
            for(int i = 0; i < tree.getChildren().size(); i++){
                ParseTree child = tree.getChildren().get(i);
                if(child.getLabel().getValue().equals("<Code>")){
                    if(!(child.getFather().getLabel().getValue().equals("<Program>"))){
                        changed = true;
                        removeChild(tree, child);
                    }
                }
                else if(child.getLabel().getValue().equals("<Instruction>")){
                    changed = true;
                    removeChild(tree, child);
                }
                else if(child.getLabel().getValue().equals("<Comp>")){
                    changed = true;
                    removeChild(tree, child);
                }
                else if(child.getLabel().getValue().equals("<If>'")){
                    tree.getChildren().remove(child);
                    if(child.getChildren().size() != 0){tree.getChildren().add(child);}
                }
                else if(child.getLabel().getValue().equals("<ExprArith>")){
                    changed = true;
                    removeChild(tree, child);
                }
            }
        }
    }


    /**
     * Helper method for the toAST method that removes the terminals from a given parse tree
     * @param tree the parse tree from which the terminals are removed
     * @param V the set of variables of the considered grammar
     * @param terminalUnits the set of lexical units associated with the terminals of the grammar
     * @param T the set of terminals of the considered grammar
     */
    private static void removeTerminals(ParseTree tree, Set<String> V, Set<LexicalUnit> terminalUnits, Set<String> T){
        for(int i = tree.getChildren().size() - 1; i >= 0; i--){
            ParseTree child = tree.getChildren().get(i);
            boolean notNull = child.getLabel().getType() != null;
            boolean inTerminalUnits = terminalUnits.contains(child.getLabel().getType());
            boolean inTerminals = T.contains(child.getLabel().getValue());
            if(notNull && (inTerminalUnits || inTerminals)){tree.getChildren().remove(child);}
            else{
                if(V.contains(child.getLabel().getValue())){removeTerminals(child, V, terminalUnits, T);}
            }
        }
    } 


    /**
     * This method reorganizes a giving parse tree by putting the operators as fathers of their associated operands
     *
     * @param tree the parse tree whose operations are reorganized
     * @param operators the set of operators of the considered grammar
     */
    private static void pullOperatorsUp(ParseTree tree, Set<String> operators){
        if(operators.contains(tree.getLabel().getValue())){
            tree.getFather().getFather().setLabel(tree.getLabel());
            tree.getFather().getChildren().remove(tree);
        }
        for(int i = tree.getChildren().size() - 1; i >= 0; i--){
            ParseTree child = tree.getChildren().get(i);
            pullOperatorsUp(child, operators);
        }
    }


    /**
     * This method removes a set of useless variables from a given parse tree
     *
     * @param tree the parse tree from which the useless variables are removed
     * @param uselessVariables the set of useless variables to remove from the parse tree
     */
    private static void removeUselessVariable(ParseTree tree, Set<String> uselessVariables){
        boolean uselessVariablesLeft = true;
        while(uselessVariablesLeft){
            uselessVariablesLeft = false;
            for(int i = 0; i < tree.getChildren().size(); i++){
                ParseTree child = tree.getChildren().get(i);
                if(uselessVariables.contains(child.getLabel().getValue())){
                    uselessVariablesLeft = true;
                    removeChild(tree, child);
                }
            }
        }
        for(int i = tree.getChildren().size() - 1; i >= 0; i--){
            ParseTree child = tree.getChildren().get(i);
            removeUselessVariable(child, uselessVariables);
        }
    } // in this function we simply iterate over the children of a given tree and remove the useless variables from it (the useless variables are the ones that are in the set of useless variables) and we do this until there are no more useless variables left in the tree 


    /**
     * here we handle the left associativity of the operators
     * @param tree the parse tree to handle the left associativity of
     * @param multiplyOperators the set of multiply operators of the considered grammar
     * @param addOperators the set of add operators of the considered grammar
     * @param booleanOperators the set of boolean operators of the considered grammar
     * @return the parse tree with the left associativity handled
     */
    private static void handleLeftAssociativity(ParseTree tree, Set<String> multiplyOperators, Set<String> addOperators){
        if(tree.getChildren().size() > 1){
            Object value = tree.getLabel().getValue();
            Object rightChildValue = tree.getChildren().get(1).getLabel().getValue();
            boolean isMultiplyOperator = multiplyOperators.contains(value) && multiplyOperators.contains(rightChildValue);
            boolean isAddOperator = addOperators.contains(value) && addOperators.contains(rightChildValue);
            //boolean isBooleanOperator = booleanOperators.contains(value) && booleanOperators.contains(rightChildValue);
            if(isMultiplyOperator || isAddOperator ){
                ParseTree rightTree = tree.getChildren().get(1);
                ParseTree rightLeftChild = rightTree.getChildren().get(0);
                ParseTree father = tree.getFather();
                if(rightTree.getChildren().size() > 1){
                    tree.getChildren().remove(rightTree);
                    rightTree.setFather(null);
                    tree.getChildren().add(rightLeftChild);
                    rightLeftChild.setFather(tree);
                    rightTree.getChildren().remove(rightLeftChild);
                    father.getChildren().remove(tree);
                    tree.setFather(null);
                    father.getChildren().add(rightTree);
                    rightTree.setFather(father);
                    ArrayList<ParseTree> newRightChildren = new ArrayList<>();
                    newRightChildren.add(tree);
                    newRightChildren.addAll(rightTree.getChildren());
                    rightTree.getChildren().removeAll(rightTree.getChildren());
                    rightTree.getChildren().addAll(newRightChildren);
                    tree.setFather(rightTree);
                    tree = rightTree.getFather();
                }
            }
        }
        for(int i = 0; i < tree.getChildren().size(); i++){
            ParseTree child = tree.getChildren().get(i);
            handleLeftAssociativity(child, multiplyOperators, addOperators);
        }
    } 


    /**
     * This method cleans a given parse tree by removing nodes that can be simplified
     *
     * @param tree the parse tree to be cleaned
     */
    private static void cleanTree(ParseTree tree){
        for(int i = tree.getChildren().size() - 1; i >= 0; i--){
            ParseTree child = tree.getChildren().get(i);
            cleanTree(child);
        }
        int numberChildren = 0;
        while(numberChildren != tree.getChildren().size()){
            numberChildren = tree.getChildren().size();
            clean(tree);
        }
    }


    /**
     * This method transforms a given parse tree into an abstract tree
     *
     * @param tree the parse tree to transform into an AST
     * @param V the set of variables of the considered grammar
     * @param terminalUnits the set of lexical units associated with the terminals of the grammar
     * @param T the set of terminals of the considered grammar
     */
    public static void toAST(ParseTree tree, Set<String> V, Set<LexicalUnit> terminalUnits, Set<String> T){
        removeTerminals(tree, V, terminalUnits, T);
        HashSet<String> operators = new HashSet<>(Arrays.asList("+", "<", "-", "=", "*", "/", "and", "or"));
        pullOperatorsUp(tree, operators);
        Set<String> removeVariable = new HashSet<>(Arrays.asList( "<ExprArith>'", "<ExprArith>''", "<T>'", "<T>''", "<T>", "<U>","<Cond>", "<Cond>'", "<Cond>''", "<V>", "<W>", "<V>'", "<V>''"));
        removeUselessVariable(tree, removeVariable);
        HashSet<String> multiplyOperators = new HashSet<>(Arrays.asList("*", "/"));
        HashSet<String> addOperators = new HashSet<>(Arrays.asList("+", "-"));
        //HashSet<String> booleanOperators = new HashSet<>(Arrays.asList("and", "or"));
        handleLeftAssociativity(tree, multiplyOperators, addOperators);
        cleanTree(tree);
    }


}
