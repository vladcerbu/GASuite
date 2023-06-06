package parser;

import spoon.Launcher;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.*;

public class SpoonParser {

    private final Launcher launcher = new Launcher();
    private final String classPath;
    private String className;
    private Set<CtMethod<?>> methods;
    private Set<CtConstructor<Object>> constructors;

    public SpoonParser(String classPath) {
        this.classPath = classPath;
        this.initialization();
    }

    private void initialization() {
        launcher.addInputResource(classPath);
        launcher.setSourceOutputDirectory(System.getProperty("java.io.tmpdir") + "/ga_suite/spooned");
//        launcher.setSourceOutputDirectory("src/main/resources/spooned");
        launcher.run();

        String[] splitedPath = classPath.split("\\\\");
        this.className = splitedPath[splitedPath.length - 1].split("\\.")[0];
        Factory factory = launcher.getFactory();
        this.methods = factory.Class().get(className).getMethods();
        ArrayList<CtMethod<?>> methodsArray = new ArrayList<>(factory.Class().get(className).getMethods());
        methodsArray.removeIf(method -> !method.isPublic());
        this.methods = new LinkedHashSet<>(methodsArray);
        this.constructors = factory.Class().get(className).getConstructors();
    }

    public String getClassName() {
        return className;
    }

    public Set<CtMethod<?>> getMethods() {
        return methods;
    }

    public Set<CtConstructor<Object>> getConstructors() {
        return constructors;
    }

    public CtMethod<?> getRandomMethod() {
        Random rand = new Random();
        int index = rand.nextInt(methods.size());
        Iterator<CtMethod<?>> iter = methods.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        return iter.next();
    }

    public CtConstructor<Object> getDefaultConstructor() {
        for (CtConstructor<Object> cons : constructors) {
            if (cons.getParameters().size() == 0)
                return cons;
        }
        return null;
    }

    public CtConstructor<Object> getConstructor() {
        int max = 0;
        CtConstructor<Object> constructor = null;
        for (CtConstructor<Object> cons : constructors) {
            if (cons.isPublic() && cons.getParameters().size() >= max) {
                max = cons.getParameters().size();
                constructor = cons;
            }
        }
        return constructor;
    }
}
