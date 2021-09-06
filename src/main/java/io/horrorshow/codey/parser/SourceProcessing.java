package io.horrorshow.codey.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class SourceProcessing {

    private static final Map<String, List<Function<String, BlockStmt>>> parsers = Map.of(
            "java", List.of(SourceProcessing::parseAsStatement)
    );


    private static BlockStmt parseAsStatement(String text) {
        var statement = StaticJavaParser.parseStatement("{ " + text + " }");
        return new BlockStmt().addStatement(statement);
    }


    public static String processSource(@NotNull String source, @NotNull String lang) {
        if (parsers.containsKey(lang)) {
            for (var parser : parsers.get(lang)) {
                var clazzSource = wrapInClass(source, parser);
                if (clazzSource != null) {
                    return clazzSource;
                }
            }
        }
        return source;
    }


    private static String wrapInClass(String source, Function<String, BlockStmt> parser) {
        try {
            var body = parser.apply(source);
            return toPsvmInClass(body).toString();
        } catch (Exception ignored) {
            return null;
        }
    }


    @NotNull
    public static CompilationUnit toPsvmInClass(BlockStmt body) {
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
