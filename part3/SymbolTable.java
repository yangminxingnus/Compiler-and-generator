import java.util.ArrayList;

public class SymbolTable {

    private ArrayList<ArrayList<String>> valueStack;

    SymbolTable() {
        this.valueStack = new ArrayList<ArrayList<String>>();
    }

    public ArrayList<ArrayList<String>> getValueStack() {
        return this.valueStack;
    }

    public void newSymbol(String sym) {
        ArrayList<String> tmp = this.valueStack.get(this.valueStack.size() - 1);
        tmp.add(sym);
        this.valueStack.remove(this.valueStack.size() - 1);
        this.valueStack.add(tmp);
    }

    public void exitBlock() {
        this.valueStack.remove(this.valueStack.size() - 1);
    }

    public void newBlcok() {
        ArrayList<String> newList = new ArrayList<String>();
        this.valueStack.add(newList);
    }

    public boolean search(String vRef) {
        for(int i = 0; i < valueStack.size(); i++) {
            ArrayList<String> tmp = valueStack.get(i);
            for(int j = 0; j < tmp.size(); j++) {
                if(tmp.get(j).equals(vRef)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean searchInCurrentBlock(String vRef) {
        ArrayList<String> currentLevel = valueStack.get(valueStack.size() - 1);
        for(int i = 0; i < currentLevel.size(); i++) {
            if(currentLevel.get(i).equals(vRef)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkRefID(int level, String symbol) {
        if(level == -1) {
            ArrayList<String> levelBlock = valueStack.get(0);
            for(int i = 0; i < levelBlock.size(); i++) {
                if(levelBlock.get(i).equals(symbol))
                    return true;
            }
            return false;
        } else {
            if(valueStack.size() - 1 - level < 0) {
                return false;
            }
            ArrayList<String> levelBlock = valueStack.get(valueStack.size() - 1 - level);
            for(int i = 0; i < levelBlock.size(); i++) {
                if(levelBlock.get(i).equals(symbol))
                    return true;
            }
        }
        return false;
    }
}
