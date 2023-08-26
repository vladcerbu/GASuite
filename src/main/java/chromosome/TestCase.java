package chromosome;

import chromosome.action.Action;

import java.util.ArrayList;
import java.util.Objects;

public class TestCase {
    private String testId = "id";
    private String testedMethodName;
    private ArrayList<Action> actions;

    public TestCase(TestCase copied) {
        this.testId = copied.getTestId();
        this.testedMethodName = copied.getTestedMethodName();
        this.actions = new ArrayList<>();
        for (Action action : copied.getActions())
            this.actions.add(action.makeCopy());
    }

    public TestCase(String testedMethodName, ArrayList<Action> actions) {
        this.testedMethodName = testedMethodName;
        this.actions = actions;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestCase)) return false;
        TestCase testCase = (TestCase) o;
        return Objects.equals(testedMethodName, testCase.testedMethodName) && Objects.equals(actions, testCase.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testedMethodName, actions);
    }

    @Override
    public String toString() {
        StringBuilder code = new StringBuilder("""
                \t@Test
                \tvoid test_""" + this.testedMethodName + "_" + this.testId + "() {\n");
        for (Action action : this.actions) {
            code.append("\t\t");
            code.append(action.toString());
            code.append("\n");
        }
        code.append("\t}");
        return code.toString();
    }
}
