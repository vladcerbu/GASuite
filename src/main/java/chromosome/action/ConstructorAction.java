package chromosome.action;

import spoon.reflect.declaration.CtConstructor;

public class ConstructorAction extends Action {

    public ConstructorAction(CtConstructor<Object> constructor, String varName) {
        super(constructor, varName);
    }

    public ConstructorAction(CtConstructor<Object> constructor) {
        super(constructor);
    }

    public ConstructorAction() {
        super();
    }

    @Override
    public Action makeCopy() {
        ConstructorAction copy = new ConstructorAction();
        copy.setType(this.type);
        copy.setVarName(this.varName);
        copy.setValues(this.values);
        copy.setParamTypes(this.paramTypes);
        copy.setConstructorParameters(this.constructorParameters);
        return copy;
    }

    @Override
    public String toString() {
        if (this.varName != null)
            return super.getType() + " " + super.getVarName() + " = new " + super.getType() + "(" + super.parametersToString() + ");";
        else
            return "new " + super.getType() + "(" + super.parametersToString() + ")";
    }
}
