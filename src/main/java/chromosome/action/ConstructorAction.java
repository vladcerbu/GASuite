package chromosome.action;

import spoon.reflect.declaration.CtConstructor;

public class ConstructorAction extends Action {

    public ConstructorAction(CtConstructor<Object> constructor, String varName) {
        super(constructor, varName);
    }

    private ConstructorAction() {
        super();
    }

    @Override
    public Action makeCopy() {
        ConstructorAction copy = new ConstructorAction();
        copy.setType(this.type);
        copy.setVarName(this.varName);
        copy.setParamTypes(this.paramTypes);
        return copy;
    }

    @Override
    public String toString() {
        return super.getType() + " " + super.getVarName() + " = new " + super.getType() + "(";
    }
}
