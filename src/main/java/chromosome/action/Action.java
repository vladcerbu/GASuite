package chromosome.action;

import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

import java.util.*;

public abstract class Action {
    protected String type = null;
    protected String varName = null;
    protected ArrayList<String> paramTypes = new ArrayList<>();
    protected ArrayList<String> values = new ArrayList<>();
    protected ArrayList<ConstructorAction> constructorParameters = new ArrayList<>();

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

    protected Action(CtConstructor<Object> constructor) {
        this.type = constructor.getType().getSimpleName();
        for (CtParameter<?> parameter : constructor.getParameters()) {
            this.paramTypes.add(parameter.getType().getSimpleName());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Action)) return false;
        Action action = (Action) o;
        return Objects.equals(type, action.type) && Objects.equals(varName, action.varName)
                && Objects.equals(paramTypes, action.paramTypes) && Objects.equals(values, action.values)
                && Objects.equals(constructorParameters, action.constructorParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, varName, paramTypes, values, constructorParameters);
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

    public ArrayList<String> getValues() {
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    public ArrayList<ConstructorAction> getConstructorParameters() {
        return this.constructorParameters;
    }

    public void setConstructorParameters(ArrayList<ConstructorAction> constructorParameters) {
        this.constructorParameters = constructorParameters;
    }

    protected String parametersToString() {
        StringBuilder code = new StringBuilder();
        int commas = this.getParamTypes().size() - 1;
        Queue<String> valueQ = new LinkedList<>(this.values);
        for (String parType : this.getParamTypes()) {
            String value = valueQ.remove();
            switch (parType) {
                case "int", "Integer" -> code.append(Integer.valueOf(value));
                case "float", "Float" -> code.append(Float.valueOf(value)).append("f");
                case "double", "Double" -> code.append(Double.valueOf(value));
                case "boolean", "Boolean" -> code.append(Boolean.valueOf(value));
                case "String" -> code.append("\"").append(value).append("\"");
                default -> {
                    if (value.contains("constructor_"))
                    {
                        int constructorIndex = Integer.parseInt(value.split("_")[1]);
                        code.append(this.constructorParameters.get(constructorIndex).toString());
                    }
                    else
                        code.append("null");
                }
            }
            if (commas > 0) {
                code.append(", ");
                commas--;
            }
        }
        return code.toString();
    }
}
