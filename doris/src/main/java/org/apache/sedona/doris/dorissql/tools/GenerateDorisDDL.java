package org.apache.sedona.doris.dorissql.tools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.*;

/**
 * This class is used to generate Doris DDL statements from SedonaDorisFunctions.java
 */
public class GenerateDorisDDL {
    
    private static boolean isGlobal = false;
    private static PrintWriter out = new PrintWriter(System.out);
    private static boolean isOutputToFile = false;
    private static String jarPath = "sedona-doris-1.8.0-SNAPSHOT.jar";
    private static boolean generateDrop = false;
    private static boolean generateShow = false;

    private static String mapJavaTypeToDorisType(String javaType) {
        switch (javaType) {
            case "String":
                return "string";
            case "double":
            case "Double":
                return "double";
            case "int":
            case "Integer":
                return "int";
            case "boolean":
            case "Boolean":
                return "boolean";
            case "long":
            case "Long":
                return "bigint";
            case "float":
            case "Float":
                return "float";
            case "ArrayList<Long>":
                return "array<bigint>";
            case "ArrayList<Double>":
                return "array<double>";
            case "ArrayList<String>":
                return "array<string>";
            default:
                return "string"; // Default to string for unknown types
        }
    }

    private static String getReturnTypeName(Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(ArrayList.class)) {
            // Check generic type
            java.lang.reflect.Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) genericReturnType;
                java.lang.reflect.Type[] typeArgs = pt.getActualTypeArguments();
                if (typeArgs.length > 0) {
                    if (typeArgs[0].equals(Long.class)) {
                        return "ArrayList<Long>";
                    } else if (typeArgs[0].equals(Double.class)) {
                        return "ArrayList<Double>";
                    } else if (typeArgs[0].equals(String.class)) {
                        return "ArrayList<String>";
                    }
                }
            }
        }
        return returnType.getSimpleName();
    }

    private static String generateDDL(String className, String methodSignature, String returnType) {
        // Extract parameters from method signature
        String paramsStr = methodSignature.substring(methodSignature.indexOf("(") + 1, methodSignature.indexOf(")"));
        String[] params = paramsStr.split(",");
        
        // Build parameter list for DDL
        StringBuilder paramList = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            String param = params[i].trim();
            String paramType = param.split("\\s+")[0];
            if (i > 0) {
                paramList.append(", ");
            }
            paramList.append(mapJavaTypeToDorisType(paramType));
        }

        if (generateShow) {
            // Generate SHOW CREATE FUNCTION statement
            return String.format(
                "SHOW CREATE %sFUNCTION sdn_%s(%s);\n",
                isGlobal ? "GLOBAL " : "",
                className,
                paramList.toString()
            );
        } else if (generateDrop) {
            // Generate DROP FUNCTION statement
            return String.format(
                "DROP %sFUNCTION sdn_%s(%s);\n",
                isGlobal ? "GLOBAL " : "",
                className,
                paramList.toString()
            );
        } else {
            // Generate CREATE FUNCTION statement
            return String.format(
                "CREATE %sFUNCTION sdn_%s(%s) RETURNS %s PROPERTIES (\n" +
                "    \"file\" = \"%s\",\n" +
                "    \"symbol\" = \"org.apache.sedona.doris.dorissql.SedonaDorisFunctions$%s\",\n" +
                "    \"always_nullable\" = \"true\",\n" +
                "    \"type\" = \"JAVA_UDF\"\n" +
                ");\n",
                isGlobal ? "GLOBAL " : "",
                className,
                paramList.toString(),
                mapJavaTypeToDorisType(returnType),
                jarPath,
                className
            );
        }
    }

    private static void scanAndGenerateDDL() throws ClassNotFoundException {
        // Get the main class
        Class<?> mainClass = Class.forName("org.apache.sedona.doris.dorissql.SedonaDorisFunctions");
        
        // Get all declared classes (including inner classes)
        Class<?>[] declaredClasses = mainClass.getDeclaredClasses();
        
        for (Class<?> innerClass : declaredClasses) {
            // Check if it's a static class
            if (Modifier.isStatic(innerClass.getModifiers())) {
                String className = innerClass.getSimpleName();
                
                // Get all methods
                Method[] methods = innerClass.getDeclaredMethods();
                for (Method method : methods) {
                    // Check if it's an evaluate method
                    if (method.getName().equals("evaluate")) {
                        // Build method signature
                        StringBuilder signature = new StringBuilder();
                        signature.append("evaluate(");
                        
                        Class<?>[] paramTypes = method.getParameterTypes();
                        for (int i = 0; i < paramTypes.length; i++) {
                            if (i > 0) {
                                signature.append(", ");
                            }
                            signature.append(paramTypes[i].getSimpleName());
                            signature.append(" param").append(i);
                        }
                        signature.append(")");
                        
                        // Generate and print DDL
                        out.println(generateDDL(className, signature.toString(), getReturnTypeName(method)));
                    }
                }
            }
        }
        out.flush();
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("GenerateDorisDDL", options);
    }

    public static void main(String[] args) {
        // Create Options object
        Options options = new Options();
        options.addOption("g", "global", false, "Generate GLOBAL functions");
        options.addOption("o", "outfile", true, "Output file path");
        options.addOption("j", "jarpath", true, "Path to the Sedona jar file (default: sedona-doris-1.8.0-SNAPSHOT.jar)");
        options.addOption("d", "drop", false, "Generate DROP FUNCTION statements instead of CREATE FUNCTION");
        options.addOption("s", "show", false, "Generate SHOW CREATE FUNCTION statements instead of CREATE FUNCTION");

        // Create parser
        CommandLineParser parser = new DefaultParser();
        try {
            // Parse the command line arguments
            CommandLine cmd = parser.parse(options, args);
            
            // Set global flag
            isGlobal = cmd.hasOption("global");

            // Set drop flag
            generateDrop = cmd.hasOption("drop");

            // Set show flag
            generateShow = cmd.hasOption("show");

            // Set jar path if specified
            if (cmd.hasOption("jarpath")) {
                jarPath = cmd.getOptionValue("jarpath");
            }

            // Set output file if specified
            if (cmd.hasOption("outfile")) {
                String outfile = cmd.getOptionValue("outfile");
                out = new PrintWriter(new FileWriter(outfile));
                isOutputToFile = true;
            }

            System.out.println("Generating Doris DDL statements...");
            scanAndGenerateDDL();
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            printHelp(options);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (isOutputToFile) {
                out.close();
            }
        }
    }
} 