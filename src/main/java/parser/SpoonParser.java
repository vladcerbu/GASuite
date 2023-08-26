package parser;

import spoon.Launcher;
import spoon.reflect.declaration.*;

import java.io.*;
import java.util.*;

public class SpoonParser {
    private final String origDirectory;
    private final String spoonDirectory;
    private final String fullPath;
    private final String rootPath;
    private Launcher launcher;
    private String className;
    private CtClass testedClass;
    private Set<CtConstructor<?>> constructors;
    private HashMap<String, CtClass<?>> nestedClasses = new HashMap<>();
    private Set<CtMethod<?>> methods;
    private Set<CtMethod<?>> testableMethods;
    private Set<CtField<?>> fields;

    public SpoonParser(String rootPath, String fullPath, String origDirectory, String spoonDirectory) throws IOException {
        this.rootPath = rootPath;
        this.fullPath = fullPath;
        this.origDirectory = origDirectory;
        this.spoonDirectory = spoonDirectory;
        this.initialization();
    }

    private void initialization() throws IOException {
        // Step 1:
        ArrayList<String> splitedPath = new ArrayList<>(List.of(this.fullPath.split("\\\\")));
        this.className = splitedPath.get(splitedPath.size() - 1).split("\\.")[0];
        copyClassFile(this.fullPath, this.className);
        // Step 2:
        parseTestClass();
        ArrayList<CtConstructor<?>> constructorsArray = new ArrayList<>(this.testedClass.getConstructors());
        this.constructors = new LinkedHashSet<>(constructorsArray);
        ArrayList<CtMethod<?>> methodsArray = new ArrayList<>(this.testedClass.getMethods());
        this.methods = new LinkedHashSet<>(methodsArray);
        methodsArray.removeIf(method -> !method.isPublic());
        this.testableMethods = new LinkedHashSet<>(methodsArray);
        ArrayList<CtField<?>> fieldsArray = new ArrayList<>(this.testedClass.getFields());
        this.fields = new LinkedHashSet<>(fieldsArray);

        // Step 3:
        verifyDependecies();
        // Step 4:
        nestClasses();
        // Step 5:
        parseTestClass();
    }

    public String getClassName() {
        return this.className;
    }

    public Set<String> getNestedClasses() {
        return this.nestedClasses.keySet();
    }

    public Set<CtMethod<?>> getTestableMethods() {
        return this.testableMethods;
    }

    public CtMethod<?> getRandomMethod() {
        Random rand = new Random();
        int index = rand.nextInt(this.testableMethods.size());
        Iterator<CtMethod<?>> iter = this.testableMethods.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        return iter.next();
    }

    public CtConstructor<Object> getTestedClassConstructor() {
        return getConstructorByClassName(this.className);
    }

    public CtConstructor<Object> getConstructorByClassName(String className) {
        CtClass<?> ctClass;
        if (Objects.equals(className, this.className))
            ctClass = this.testedClass;
        else
            ctClass = this.nestedClasses.get(className);
        Set<? extends CtConstructor<?>> classConstructors = ctClass.getConstructors();
        int max = 0;
        CtConstructor<Object> constructor = null;
        for (CtConstructor<?> cons : classConstructors) {
            if (cons.isPublic() && cons.getParameters().size() >= max) {
                max = cons.getParameters().size();
                constructor = (CtConstructor<Object>) cons;
            }
        }
        return constructor;
    }

    private void copyClassFile(String fullPath, String className) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fullPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(this.origDirectory + "\\" + className + ".java"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Check if the line starts with the specified word
                if (line.trim().startsWith("import") || line.trim().startsWith("package")) {
                    // If yes, continue
                    continue;
                }
                // Else, write it to the temporary file
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private void parseTestClass() {
        this.launcher = new Launcher();
        this.launcher.addInputResource(this.origDirectory + "\\" + this.className + ".java");
        this.launcher.setSourceOutputDirectory(this.spoonDirectory);
        this.launcher.run();
        this.testedClass = this.launcher.getFactory().Class().get(this.className);
        this.launcher = null;
    }

    private void verifyDependecies() throws IOException {
        ArrayList<String> primitives = new ArrayList<>(Arrays.asList("void", "int", "Integer", "float", "Float", "double", "Double", "boolean", "Boolean", "String"));
        for (CtConstructor<?> constructor : this.constructors) {
            for (CtParameter<?> param : constructor.getParameters()) {
                String paramName = param.getType().getSimpleName();
                if (!primitives.contains(paramName) && !this.nestedClasses.containsKey(paramName)) {
                    parseDependencyClass(paramName);
                }
            }
        }

        for (CtMethod<?> method : this.methods) {
            String name = method.getType().getSimpleName();
            if (!primitives.contains(name) && !this.nestedClasses.containsKey(name)) {
                parseDependencyClass(name);
            }
            for (CtParameter<?> param : method.getParameters()) {
                String paramName = param.getType().getSimpleName();
                if (!primitives.contains(paramName) && !this.nestedClasses.containsKey(paramName)) {
                    parseDependencyClass(paramName);
                }
            }
        }

        for (CtField<?> field : this.fields) {
            String name = field.getType().getSimpleName();
            if (!primitives.contains(name) && !this.nestedClasses.containsKey(name)) {
                parseDependencyClass(name);
            }
        }
    }

    private void parseDependencyClass(String dependencyClassName) throws IOException {
        String pathToClass = getFullPathToClass(this.rootPath, dependencyClassName);
        copyClassFile(pathToClass, dependencyClassName);
        this.launcher = new Launcher();
        this.launcher.addInputResource(this.origDirectory + "\\" + dependencyClassName + ".java");
        this.launcher.setSourceOutputDirectory(this.spoonDirectory);
        this.launcher.run();
        this.nestedClasses.put(dependencyClassName, this.launcher.getFactory().Class().get(dependencyClassName));
        this.launcher = null;
    }

    private void nestClasses() throws IOException {
        for (String className : this.nestedClasses.keySet()) {
            StringBuilder sourceContent = new StringBuilder();
            try (BufferedReader sourceReader = new BufferedReader(new FileReader(this.origDirectory + "\\" + className + ".java"))) {
                String line;
                while ((line = sourceReader.readLine()) != null) {
                    sourceContent.append(line).append("\n");
                }
            }

            StringBuilder targetContent = new StringBuilder();
            try (BufferedReader targetReader = new BufferedReader(new FileReader(this.origDirectory + "\\" + this.className + ".java"))) {
                String line;
                while ((line = targetReader.readLine()) != null) {
                    targetContent.append(line).append("\n");
                    if (line.contains("class " + this.className + " {")) {
                        targetContent.append(sourceContent).append("\n");
                    }
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.origDirectory + "\\" + this.className + ".java"))) {
                writer.write(targetContent.toString());
            }
        }
    }

    private String getFullPathToClass(String currentDirectory, String className) throws IOException {
        File directory = new File(currentDirectory);
        File file = findFile(directory, className + ".java");

        if (file == null) {
            throw new IllegalArgumentException("File " + className + ".java not found in the directory: " + currentDirectory);
        }

        return file.getCanonicalPath();
    }

    private static File findFile(File directory, String fileName) {
        // Recursively search for the file within the directory and its subdirectories
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File foundFile = findFile(file, fileName);
                    if (foundFile != null) {
                        return foundFile;
                    }
                } else if (file.getName().equals(fileName)) {
                    return file;
                }
            }
        }
        return null;
    }
}
