package io.horrorshow.codey.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;


public class SourceProcessing {

    private static final List<Function<String, BlockStmt>> parsers = List.of(
            (text) -> {
                var statement = StaticJavaParser.parseStatement(text);
                return new BlockStmt().addStatement(statement);
            },
            (text) -> {
                var expression = StaticJavaParser.parseExpression(text);
                return new BlockStmt().addStatement(expression);
            }
    );

    public static String processSource(@NotNull String source) {
        for (var parser : parsers) {
            var clazzSource = wrapInClass(source, parser);
            if (clazzSource != null) return clazzSource;
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
    public static ClassOrInterfaceDeclaration toPsvmInClass(BlockStmt body) {
        var stringArgs = new Parameter().setType("String[]").setName("args");
        var myClass = new CompilationUnit().addClass("CodeyClass").setPublic(true);
        myClass.addMethod("main", Modifier.Keyword.STATIC)
                .setStatic(true).setPublic(true)
                .setParameters(NodeList.nodeList(stringArgs))
                .setBody(body);
        return myClass;
    }
}
