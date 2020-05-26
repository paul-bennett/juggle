import java.util.*;
import java.util.jar.*;
import java.lang.reflect.*;
import java.io.*;

public class MethodMatcher {

    public static void main(String... args) throws Exception {

        // where to load some classes from (could be a list of classes to 
        // search from)..
        //        String pathToJar = "/usr/lib/jvm/java-6-sun-1.6.0.22/jre/lib/rt.jar";
        String pathToJar = args[0];

        MethodMatcher m = new MethodMatcher(pathToJar, 
                "java.io", "java.lang", "java.math", "java.net", 
                "java.nio", "java.text", "java.util");

        // print some examples
        m.printExampleSearch(Integer.class, new int[0].getClass());
        m.printExampleSearch(String.class, String.class, Character.class, Character.class);
        m.printExampleSearch(Integer.class, String.class);
        m.printExampleSearch(Void.class, List.class);
    }

    public void printExampleSearch(Class<?> returnType, Class<?>... arguments) {

        for (int i = 0; i < arguments.length; i++)
            System.out.print((i == 0 ? "":", ") + arguments[i].getSimpleName());

        System.out.println(" -> " + returnType.getSimpleName());

        Set<Method> methods = findMethods(returnType, arguments);

        for (Method method : methods)
            System.out.println("\t" + method);

        System.out.println();
    }



    private final List<MethodFinder> klasses;

    public MethodMatcher(String jarFile, String... allowedPackages) 
    throws IOException, ClassNotFoundException {

        klasses = loadClasses(jarFile, allowedPackages);
    }

    /**
     * Finds a set of methods
     * @param returnType the return type
     * @param arguments the arguments (in any order)
     * @return a set of methods
     */
    public Set<Method> findMethods(Class<?> returnType,
            Class<?>... arguments) {

        Set<Method> methods = new LinkedHashSet<Method>();

        if (arguments.length > 0) {
            MethodFinder instance = new MethodFinder(arguments[0]);

            Class<?>[] rest = new Class<?>[arguments.length - 1];
            System.arraycopy(arguments, 1, rest, 0, rest.length);

            methods.addAll(instance.findInstanceMethods(returnType, rest));
        }
        else {
            for (MethodFinder k : klasses)
                methods.addAll(k.findInstanceMethods(returnType, arguments));
        }

        for (MethodFinder k : klasses)
            methods.addAll(k.findStaticMethods(returnType, arguments));

        return methods;
    }

    /**
     * A method finder class
     */
    static class MethodFinder {

        public final Class<?> klass;

        /**
         * Constructs the method finder (doh)
         * @param klass the class
         */
        public MethodFinder(Class<?> klass) {
            this.klass = klass;
        }

        /**
         * Finds instance method matches
         * @param returnType the return type
         * @param arguments the arguments (in any order)
         * @return
         */
        public List<Method> findInstanceMethods(Class<?> returnType, 
                Class<?>... arguments) {

            List<Method> matches = new LinkedList<Method>();

            for (Method method : klass.getMethods()) {
                if ((method.getModifiers() & Modifier.STATIC) == 0) 
                    if (testMethod(method, returnType, arguments))
                        matches.add(method);
            }

            return matches;        
        }

        /**
         * Finds static method matches
         * @param returnType the return type
         * @param arguments the arguments (in any order)
         * @return
         */
        public List<Method> findStaticMethods(Class<?> returnType,
                Class<?>... arguments) {

            List<Method> matches = new LinkedList<Method>();

            for (Method method : klass.getMethods()) 
                if ((method.getModifiers() & Modifier.STATIC) != 0) 
                    if (testMethod(method, returnType, arguments))
                        matches.add(method);

            return matches;        
        }

        /**
         * Tests a method if it is a match
         * @param method the method to test
         * @param returnType the return type
         * @param arguments the arguments (in any order)
         * @return true if it matches
         */
        private boolean testMethod(Method method, 
                Class<?> returnType, 
                Class<?>... arguments) {

            boolean returnTypeIsOk = false;
            for (Class<?> ic : getInterchangable(returnType))
                if (ic.isAssignableFrom(method.getReturnType()))
                    returnTypeIsOk = true;

            if (!returnTypeIsOk)
                return false;

            Class<?>[] methodArguments = method.getParameterTypes();

            if (methodArguments.length != arguments.length)
                return false;

            if (methodArguments.length == 0) {
                return true;
            }
            else {
                Permutations permutations = new Permutations(arguments);

                outer: for (Class<?>[] permutation : permutations) {
                    for (int i = 0; i < methodArguments.length; i++) {

                        boolean canAssign = false;
                        for (Class<?> ic : getInterchangable(permutation[i])) 
                            if (methodArguments[i].isAssignableFrom(ic))
                                canAssign = true;

                        if (!canAssign)
                            continue outer;
                    }
                    return true;
                }

                return false;
            }
        }

        /**
         * Returns the autoboxing types
         * @param type the type to autobox :)
         * @return a list of types that it could be
         */
        private static Class<?>[] getInterchangable(Class<?> type) {

            if (type == Boolean.class || type == Boolean.TYPE)
                return new Class<?>[] { Boolean.class, Boolean.TYPE };
            if (type == Character.class || type == Character.TYPE)
                return new Class<?>[] { Character.class, Character.TYPE };
            if (type == Short.class || type == Short.TYPE)
                return new Class<?>[] { Short.class, Short.TYPE };
            if (type == Integer.class || type == Integer.TYPE)
                return new Class<?>[] { Integer.class, Integer.TYPE };
            if (type == Float.class || type == Float.TYPE)
                return new Class<?>[] { Float.class, Float.TYPE };
            if (type == Double.class || type == Double.TYPE)
                return new Class<?>[] { Double.class, Double.TYPE };
            if (type == Void.class || type == Void.TYPE)
                return new Class<?>[] { Void.class, Void.TYPE };

            return new Class<?>[] { type };
        }


        /**
         * Creates a permutation list of all different combinations
         */
        @SuppressWarnings("serial")
        private class Permutations extends LinkedList<Class<?>[]> {

            /**
             * Creates a permutation list
             * @param list the list to be permutated
             */
            public Permutations(Class<?>[] list) {
                permutate(new LinkedList<Class<?>>(Arrays.asList(list)),
                        new LinkedList<Class<?>>());
            }

            // ugly, there is better ways of doing this...
            private void permutate(List<Class<?>> tail, List<Class<?>> choosen) {

                if (tail.isEmpty()) {
                    add(choosen.toArray(new Class<?>[0]));
                    return;
                }

                ListIterator<Class<?>> it = tail.listIterator();

                while (it.hasNext()) {

                    Class<?> current = it.next();

                    choosen.add(current);
                    it.remove();

                    permutate(new LinkedList<Class<?>>(tail), choosen);

                    choosen.remove(current);
                    it.add(current);
                }
            }
        }
    }

    /**
     * A hack to read some classes from some allowed packages
     * @param jarFile the jar file to read from
     * @param allowedPackages the allowed packages
     * @return a list of MethodFinders
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static List<MethodFinder> loadClasses(
            String jarFile, 
            String... allowedPackages) throws IOException, ClassNotFoundException {

        List<MethodFinder> klasses = new LinkedList<MethodFinder>();

        JarFile file = new JarFile(jarFile);
        try {
            Enumeration<JarEntry> enumerator = file.entries();

            while (enumerator.hasMoreElements()) {

                String name = enumerator.nextElement().getName();

                if (!name.endsWith(".class")) 
                    continue;

                name = name.substring(0, name.length() - 6).replace('/', '.');

                boolean allowed = false;
                for (String pkg : allowedPackages)
                    allowed |= name.startsWith(pkg);

                if (-1 != name.indexOf('$'))	// Disallow inner classes; Class.forName would fail anyway
                    allowed = false;

                if (allowed)
                    klasses.add(new MethodFinder(Class.forName(name)));
            }
        } 
        finally {
            if (file != null)
                file.close();
        }

        return klasses;
    }
}
