package io.horrorshow.codey.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;


public class JavaSourceProcessor implements SourceProcessor {

    @Override
    public ProcessResult process(String source) {
        String finalSource = source;
        try {
            finalSource = wrapInClass(source).toString();
        } catch (Exception ignored) {
        }

        return staticCodeAnalysis(finalSource);
    }


    private ProcessResult staticCodeAnalysis(String source) {
        try {
            StaticJavaParser.parse(source);
        } catch (Exception e) {
            var message = e.getMessage().lines().limit(2).collect(Collectors.joining("\n\n"));
            return new ProcessResult(source, false, message);
        }
        return new ProcessResult(source, true, null);
    }


    private BlockStmt parseAsStatement(String text) {
        var statement = StaticJavaParser.parseStatement("{ " + text + " }");
        return new BlockStmt().addStatement(statement);
    }


    private CompilationUnit wrapInClass(String source) {
        var body = parseAsStatement(source);
        return toPsvmInClass(body);
    }


    @NotNull
    public CompilationUnit toPsvmInClass(BlockStmt body) {
        var compilationUnit = new CompilationUnit();
        compilationUnit.addImport("java.util.*");
        var stringArgs = new Parameter().setType("String[]").setName("args");
        var myClass = compilationUnit.addClass("CodeyClass").setPublic(true);
        myClass.addMethod("main", Modifier.Keyword.STATIC)
                .setStatic(true).setPublic(true)
                .setParameters(NodeList.nodeList(stringArgs))
                .setBody(body);
        return compilationUnit;
    }
}
