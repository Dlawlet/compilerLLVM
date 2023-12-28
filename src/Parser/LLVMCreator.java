package Parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Main.FileCreator;
import Main.LexicalUnit;
import Main.Symbol;

public class LLVMCreator{
    ParseTree parseTree;
    Map<String, String> operationsMap;
    Set<String> variables;
    String code = "";
    int tmpCounter = 0;
    int condCounter = 0;


    public LLVMCreator(ParseTree parseTree){
        this.parseTree = parseTree;
        this.variables = new HashSet<>();
        buildOperationsMap();
        buildCode();
        mapping(parseTree);
        this.code += "ret i32 0\n" + "}\n";
    }


    //Getters
    public String getCode(){return code;}


    private void buildOperationsMap(){
        this.operationsMap = new HashMap<>(){{
            put("+", "add");
            put("-", "sub");
            put("*", "mul");
            put("/", "sdiv");
            put("=","eq");
            put("<","slt");
            put("and","and");  
            put("or","or");           
        }};
    }


    private void buildCode(){
        this.code += "@.strR = private unnamed_addr constant [3 x i8] c\"%d\\00\", align 1\n" +
                "\n" +
                "; Function Attrs: nounwind uwtable\n" +
                "define i32 @readInt() #0 {\n" +
                "  %x = alloca i32, align 4\n" +
                "  %1 = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %x)\n" +
                "  %2 = load i32, i32* %x, align 4\n" +
                "  ret i32 %2\n" +
                "}\n" +
                "\n" +
                "declare i32 @__isoc99_scanf(i8*, ...) #1\n" +
                "\n" +
                "@.strP = private unnamed_addr constant [4 x i8] c\"%d\\0A\\00\", align 1\n" +
                "\n" +
                "; Function Attrs: nounwind uwtable\n" +
                "define void @println(i32 %x) #0 {\n" +
                "  %1 = alloca i32, align 4\n" +
                "  store i32 %x, i32* %1, align 4\n" +
                "  %2 = load i32, i32* %1, align 4\n" +
                "  %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)\n" +
                "  ret void\n" +
                "}\n" +
                "\n" +
                "declare i32 @printf(i8*, ...) #1\n" +
                "\n" +
                "\n" +
                "\n" +
                "define i32 @main() { \nentry:\n" +
                "       ; read a and b\n";
    }


    /**
     * This method produces a variable name for a new temporary variable
     *
     * @return varName the variable name produced for the new temporary variable
     */
    private String produceNewVarName(){
        String varName = tmpCounter + "";
        tmpCounter++;
        condCounter = 0;
        return varName;
    }


    /**
     * This method loads the integer value pointed by value into the integer variable varName
     * 
     * @param varName the name of the variable in which the value is loaded
     * @param value the object pointing to the value which is loaded in the variable
     */
    private void loadVariable(String varName, Object value){
        // if value = %condX then it is a binary condition then no need to load it
        if (value.toString().contains("cond")) {

        }
        else {
            this.code += "%" + varName + " = load i32, i32* %" + value + "\n";
        }
    }


    /**
     * This method allocates a new integer whose name is specified by varName
     *
     * @param varName the name of the allocated variable
     */
    private void allocateVariable(String varName){this.code += "%" + varName + " = alloca i32\n";}


    /**
     * This method stores a given integer value in an integer variable pointer
     *
     * @param pointer the name of the pointer in which the value is stored
     * @param value the value that is stored in the variable pointer
     */
    private void storeInteger(String pointer, Object value){
        this.code += "store  i32 " + value +  ", i32* %" + pointer + "\n";
    }


    /**
     * This method stores the value of a given variable in another variable pointer
     *
     * @param pointer the name of the pointer in which the value will be stored
     * @param value the name of the variable which contained the value to store in another variable
     */
    private void storeVariable(String pointer, String value){storeInteger(pointer, "%" + value);}


    /**
     * Helper method that evaluates an expression and returns the name of the variable that contains the result of the evaluation
     * 
     * @param tree the parse tree containing the expression to be evaluated
     * @return the name of the variable that contains the result of the evaluation
     * 
     */
    private String evaluateExpression(ParseTree tree){
        String varName = null;
        if(tree.getChildren().size() == 1){
            String tmpVar1 = evaluateExpression(tree.getChildren().get(0));
            String tmpVar2 = produceNewVarName();
            String tmpVar3 = produceNewVarName();
            loadVariable(tmpVar2, tmpVar1);
            this.code += "%" + tmpVar3 + " = " + operationsMap.get(tree.getLabel().getValue()) + " i32 0" +  " , %" + tmpVar2 + "\n";
            varName = produceNewVarName();
            allocateVariable(varName);
            storeVariable(varName, tmpVar3);
        }
        else if(tree.getChildren().size() > 1){
            String tmpVar2 = evaluateExpression(tree.getChildren().get(1));
            String tmpVar1 = evaluateExpression(tree.getChildren().get(0));
            String tmpVar3 = produceNewVarName();
            String tmpVar4 = produceNewVarName();
            String tmpVar5 = produceNewVarName();
            varName = produceNewVarName();
            loadVariable(tmpVar3, tmpVar2);
            loadVariable(tmpVar4, tmpVar1);
            this.code += "%" + tmpVar5 + " = " + operationsMap.get(tree.getLabel().getValue()) + " i32 %" + tmpVar4 + " , %" + tmpVar3 + "\n";
            allocateVariable(varName);
            storeVariable(varName, tmpVar5);
        }
        else{
            if(tree.getLabel().getType() == LexicalUnit.NUMBER){
                varName = produceNewVarName();
                allocateVariable(varName);
                storeInteger(varName, tree.getLabel().getValue());
            }
            else{varName = tree.getLabel().getValue() + "";}
        }
        return varName;
    } 

    /**
     * This method tries to allocate a new variable if its name was not already met before
     *
     * @param varName the name of the variable that may be allocated
     */
    private void tryAllocateVariable(String varName){
        if(!variables.contains(varName)){
            allocateVariable(varName);
            variables.add(varName);
        }
    }


    /**
     * This method writes the code of an Assign statement
     *
     * @param tree the parse tree containing the Assign instruction
     */
    private void assignStatement(ParseTree tree){
        String varName = tree.getChildren().get(0).getLabel().getValue() + "";
        String tmpVar = evaluateExpression(tree.getChildren().get(1));
        String tmpVar3 = produceNewVarName();
        loadVariable(tmpVar3, tmpVar);
        tryAllocateVariable(varName);
        storeVariable(varName, tmpVar3);
    }


    /**
     * This method writes the code of a Print statement
     *
     * @param tree the parse tree containing the Print instruction
     */
    private void printStatement(ParseTree tree){
        String varName = tree.getChildren().get(0).getLabel().getValue() + "";
        String tmpVar3 = produceNewVarName();
        loadVariable(tmpVar3, varName);
        this.code += "call void @println(i32 %" + tmpVar3 +  ")" + "\n";
    }


    /**
     * This method writes the code of a Read statement
     *
     * @param tree the parse tree containing the Read instruction
     */
    private void readStatement(ParseTree tree){
        String tmpVar = produceNewVarName();
        this.code += "%" + tmpVar + " = call i32 @readInt()\n";
        String varName = tree.getChildren().get(0).getLabel().getValue() + "";
        tryAllocateVariable(varName);
        this.code += "store i32 %" + tmpVar + ", i32* %" + varName + "\n";
    }


    /**
     * This method writes the code that evaluates a boolean expression
     * and returns the name of the variable that contains the evaluation of the condition
     *
     * @param condition the parse tree containing the condition to be evaluated
     * @return the name of the variable that contains the evaluation of the boolean expression
     */
    private String evaluateCondition(ParseTree condition){
        ParseTree leftTree = condition.getChildren().get(0);
        ParseTree rightTree = condition.getChildren().get(1);

        // Check if the children are conditions themselves
        String left, right;
        if (leftTree.getLabel().getValue().equals("and") || leftTree.getLabel().getValue().equals("or") || leftTree.getLabel().getValue().equals("=") || leftTree.getLabel().getValue().equals("<")) {
            left = evaluateCondition(leftTree);
            } 
        else {
                left = evaluateExpression(leftTree);
            }
        if (rightTree.getLabel().getValue().equals("and") || rightTree.getLabel().getValue().equals("or") || rightTree.getLabel().getValue().equals("=") || rightTree.getLabel().getValue().equals("<")) {
            right = evaluateCondition(rightTree);
            } 
        else {
                right = evaluateExpression(rightTree);
            }
        
        if (condition.getLabel().getValue().equals("and") || condition.getLabel().getValue().equals("or")) {
            this.condCounter++;
            this.code += "%cond" + tmpCounter + condCounter + " = " + operationsMap.get(condition.getLabel().getValue()) + " i1 %" + left + " , %" + right + "\n";
            return "cond" + tmpCounter + condCounter;
        } 
        else {
            String newLeft =  produceNewVarName();
            loadVariable(newLeft, left);
            String newRight = produceNewVarName();
            loadVariable(newRight, right);
            this.code += "%cond" + tmpCounter + " = icmp " + operationsMap.get(condition.getLabel().getValue()) + " i32 %" + newLeft + " , %" + newRight + "\n";
            return "cond" + tmpCounter;
        }
        
    } 


    /**
     * This method writes the code that makes a conditional jump
     *
     * @param conditionName the name of the variable containing the condition of the jump
     * @param ifLabel the label name of the if branch
     * @param elseLabel the label name of the else branch
     */
    private void condjump(String conditionName, String ifLabel, String elseLabel){
        this.code += "br i1 %" + conditionName + ", label %" + ifLabel +  " ,label %" + elseLabel + "\n";
    }


    /**
     * This method writes the code that produces a new label
     *
     * @param labelName the name of the label that is produced
     */
    private void produceLabel(String labelName){this.code += labelName + ":" + "\n";}


    /**
     * This method writes the code that makes an unconditional jump
     *
     * @param jumpLabel the name of the label to which the unconditional jump is made
     */
    private void unCondJump(String jumpLabel){this.code += "br label %" + jumpLabel + "\n";}


    /**
     * This method writes the code of an If statement
     *
     * @param tree the parse tree containing the If instruction
     */
    private void ifStatement(ParseTree tree){
        ParseTree condition = tree.getChildren().get(0);
        String conditionName = evaluateCondition(condition);
        int last = tree.getChildren().size();
        if(tree.getChildren().get(tree.getChildren().size() - 1).getLabel().getValue().equals("<If>'")){
            last--;
        }
        String ifLabel = "if" + tmpCounter;
        String elseLabel = "else" + tmpCounter;
        String exitLabel = "exitIf" + tmpCounter;
        if(last != tree.getChildren().size()){condjump(conditionName, ifLabel, elseLabel);}
        else{condjump(conditionName, ifLabel, exitLabel);}
        produceLabel(ifLabel);
        for(int i = 1; i < last; i++){
            ParseTree child = tree.getChildren().get(i);
            mapping(child);
        }
        unCondJump(exitLabel);
        if(last != tree.getChildren().size()){
            produceLabel(elseLabel);
            ParseTree elseStatements = tree.getChildren().get(tree.getChildren().size() - 1);
            for(int i = 0; i < elseStatements.getChildren().size(); i++){
                ParseTree child = elseStatements.getChildren().get(i);
                mapping(child);
            }
            unCondJump(exitLabel);
        }
        produceLabel(exitLabel);
    }


    /**
     * This method writes the code of a While statement
     *
     * @param tree the parse tree containing the While instruction
     */
    private void whileStatement(ParseTree tree){
        ParseTree condition = tree.getChildren().get(0);
        String whileLabel = "while" + tmpCounter;
        String exitLabel = "exitWhile" + tmpCounter;
        String conditionName = evaluateCondition(condition);
        condjump(conditionName, whileLabel, exitLabel);
        produceLabel(whileLabel);
        for(int i = 1; i < tree.getChildren().size(); i++){
            ParseTree child = tree.getChildren().get(i);
            mapping(child);
        }
        conditionName = evaluateCondition(condition);
        condjump(conditionName, whileLabel, exitLabel);
        produceLabel(exitLabel);
    } //

    /** 
     * This method writes the code of a For statement
     * 
     * @param tree the parse tree containing the For instruction
     */
     private void forStatement(ParseTree tree){
        // "for", "<Assign>", "<ExprArith>", "Cond", "do", "<Instruction>"
        // first we call initial assignStatement
        assignStatement(tree.getChildren().get(0));
        ParseTree condition = tree.getChildren().get(2);
        String forLabel = "for" + tmpCounter;
        String exitLabel = "exitFor" + tmpCounter;
        String conditionName = evaluateCondition(condition);
        condjump(conditionName, forLabel, exitLabel);
        produceLabel(forLabel);
        for(int i = 3; i < tree.getChildren().size(); i++){
            ParseTree child = tree.getChildren().get(i);
            mapping(child);
        } // this is the set of instructions

        assignStatement(tree.getChildren().get(1));

        // then we evaluate the condition again
        conditionName = evaluateCondition(condition);
        condjump(conditionName, forLabel, exitLabel);
        produceLabel(exitLabel);

        } 
        
    /**
     * This method traverses a given parse tree and writes the corresponding llvm code
     *
     * @param tree the parse tree whose llvm code is written
     */
    private void mapping(ParseTree tree){
        if(tree.getLabel().getValue().equals("<Assign>")){assignStatement(tree);}
        else if(tree.getLabel().getValue().equals("<Print>")){printStatement(tree);}
        else if(tree.getLabel().getValue().equals("<Read>")){readStatement(tree);}
        else if(tree.getLabel().getValue().equals("<If>")){ifStatement(tree);}
        else if(tree.getLabel().getValue().equals("<While>")){whileStatement(tree);}
        else if(tree.getLabel().getValue().equals("<For>")){forStatement(tree);}
        else{
            for(int i = 0; i < tree.getChildren().size(); i++){
                ParseTree child = tree.getChildren().get(i);
                mapping(child);
            }
        }
    }


}
