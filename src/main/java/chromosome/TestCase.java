package chromosome;

import chromosome.action.Action;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class TestCase {
    private String testId = "id";
    private String testedMethodName;
    private ArrayList<Action> actions;
    private ArrayList<String> values;

    public TestCase(TestCase copied) {
        this.testId = copied.getTestId();
        this.testedMethodName = copied.getTestedMethodName();
        this.actions = new ArrayList<>();
        for (Action action : copied.getActions())
            this.actions.add(action.makeCopy());
        this.values = new ArrayList<>();
        this.values.addAll(copied.getValues());
    }

    public TestCase(String testedMethodName, ArrayList<Action> actions, ArrayList<String> values) {
        this.testedMethodName = testedMethodName;
        this.actions = actions;
        this.values = values;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getTestedMethodName() {
        return testedMethodName;
    }

    public void setTestedMethodName(String testedMethodName) {
        this.testedMethodName = testedMethodName;
    }

    public ArrayList<Action> getActions() {
        return actions;
    }

    public void setActions(ArrayList<Action> actions) {
        this.actions = actions;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestCase)) return false;
        TestCase testCase = (TestCase) o;
        return Objects.equals(testedMethodName, testCase.testedMethodName) && Objects.equals(actions, testCase.actions) && Objects.equals(values, testCase.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testedMethodName, actions, values);
    }

    @Override
    public String toString() {
        StringBuilder code = new StringBuilder("""
                \t@Test
                \tvoid test_""" + this.testedMethodName + "_" + this.testId + "() {\n");
        Queue<String> valueQ = new LinkedList<>(this.values);
        for (Action action : this.actions) {
            code.append("\t\t");
            code.append(action.toString());
            int commas = action.getParamTypes().size() - 1;
            for (String parType : action.getParamTypes()) {
                String value = valueQ.remove();
                switch (parType) {
                    case "int", "Integer" -> code.append(Integer.valueOf(value));
                    case "float", "Float" -> code.append(Float.valueOf(value)).append("f");
                    case "double", "Double" -> code.append(Double.valueOf(value));
                    case "boolean", "Boolean" -> code.append(Boolean.valueOf(value));
                    case "String" -> code.append("\"").append(value).append("\"");
                    default -> code.append("null");
                }
                if (commas > 0) {
                    code.append(", ");
                    commas--;
                }
            }
            code.append(");\n");
        }
        code.append("\t}");
        return code.toString();
    }
}
