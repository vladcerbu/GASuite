package chromosome.action;

import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Action {
    protected String type;
    protected String varName;
    protected ArrayList<String> paramTypes = new ArrayList<>();

    protected Action() {}

    public abstract Action makeCopy();

    protected Action(CtMethod<?> method, String varName) {
        this.type = method.getType().getSimpleName();
        this.varName = varName;
        for (CtParameter<?> parameter : method.getParameters()) {
            this.paramTypes.add(parameter.getType().getSimpleName());
        }
    }

    protected Action(CtConstructor<Object> constructor, String varName) {
        this.type = constructor.getType().getSimpleName();
        this.varName = varName;
        for (CtParameter<?> parameter : constructor.getParameters()) {
            this.paramTypes.add(parameter.getType().getSimpleName());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Action)) return false;
        Action action = (Action) o;
        return Objects.equals(type, action.type) && Objects.equals(varName, action.varName) && Objects.equals(paramTypes, action.paramTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, varName, paramTypes);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(List<String> paramTypes) {
        this.paramTypes = new ArrayList<>(paramTypes);
    }
}
