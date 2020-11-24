package io.horrorshow.discordcodeformatter;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DiscordMessage {
    private static final Pattern matchCodeblocks = Pattern.compile("```[\\s\\S]*?(```)");
    private static final int MAX_LENGTH_LANG;
    private static final String[] LANGUAGES = {
            "java", "xml", "html", "bash", "cpp", "c++", "c", "cs", "css", "md", "markdown",
            "diff", "ruby", "go", "http", "toml", "ini", "java", "js", "javascript", "json",
            "kt", "kotlin", "less", "lua", "make", "make", "perl", "cfg", "config", "obj-c",
            "objective-c", "objc", "objectivec", "php", "properties", "py", "python", "python-repl",
            "rust", "scss", "shell", "sql", "swift", "yaml", "typescript", "log", "actionscript", "as",
            "ada", "angelscript", "apache", "apache-conf", "apache-config", "applescript", "arduino",
            "arm", "asc", "asciidoc", "aspectj", "autohotkey", "autoit", "awk", "basic", "brainfuck",
            "cal", "ceylon", "clean", "clojure", "cmake", "coffee", "coffeescript", "coq", "crmsh",
            "crystal", "csp", "d", "dart", "delphi", "django", "dockerfile", "batch", "dsconfig",
            "dust", "elixir", "elm", "erb", "erlang", "excel", "fix", "flix", "fortran", "fs",
            "gams", "gauss", "gherkin", "glsl", "gml", "go", "golo", "gradle", "groovy", "haml",
            "handlebars", "haskell", "haxe", "hsp", "hy", "inform7", "irpf90", "isbl", "jboss", "julia",
            "lasso", "latex", "ldif", "leaf", "lisp", "livecode", "livescript", "llvm", "lsl", "wolfram",
            "matlab", "maxima", "mel", "mercury", "mips", "mizar", "mojolicious", "monkey", "moonscript",
            "n10ql", "nginxconfig", "nim", "nix", "node", "nsis", "ocaml", "openscad", "oxygene",
            "parser3", "psql", "postresql", "pony", "powershell", "processing", "prolog", "protobuf",
            "puppet", "purebasic", "q", "qml", "r", "reasonml", "re", "roboconf", "sas", "scala",
            "scheme", "scilab", "smali", "smalltalk", "sml", "sqf", "stan", "stata", "st", "step",
            "stylus", "taggerscript", "tcl", "thrift", "tp", "twig", "ts", "vala", "v", "vb", "vbscript",
            "verilog", "vhdl", "vim", "xl", "xquery", "zephir", "befunge", "zsh"
    };

    static {
        Arrays.sort(LANGUAGES);
        MAX_LENGTH_LANG = Arrays.stream(LANGUAGES).map(String::length).max(Integer::compareTo).get();
    }

    @Getter
    private final List<MessagePart> parts = new ArrayList<>();

    public DiscordMessage(String raw) {
        var m = matchCodeblocks.matcher(raw);
        int lastEnd = 0;
        while (m.find()) {
            var start = m.start();
            var end = m.end();
            if (start > lastEnd) {
                parts.add(new MessagePart(false, null, raw.substring(lastEnd, start)));
            }
            var rawCodeBlock = raw.substring(start, end);

            MessagePart part = getMessagePart(rawCodeBlock);
            parts.add(part);

            lastEnd = end;
        }
        if (lastEnd < raw.length()) {
            parts.add(new MessagePart(false, null, raw.substring(lastEnd)));
        }
    }

    public static int startsWithAnyOf(String testString, String[] arr) {
        var s = (testString.length() > MAX_LENGTH_LANG)
                ? testString.substring(0, MAX_LENGTH_LANG)
                : testString;
        int low = 0;
        int high = arr.length - 1;
        int mid;

        while (low <= high) {
            mid = (low + high) / 2;

            if (s.startsWith(arr[mid])) {
                return mid;
            } else if (s.compareTo(arr[mid]) < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return -1;
    }

    @NotNull
    private MessagePart getMessagePart(String rawCodeBlock) {
        var content = rawCodeBlock.substring("```".length(),
                rawCodeBlock.lastIndexOf("```"));
        var langIdx = startsWithAnyOf(content, LANGUAGES);
        var lang = "";
        if (langIdx >= 0) {
            lang = LANGUAGES[langIdx];
        }
        return new MessagePart(true, (lang.isEmpty()) ? null : lang,
                content.substring(lang.length()));
    }

    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class MessagePart {
        private final boolean isCode;
        private final String lang;
        private final String text;
    }
}
