package io.horrorshow.codey.compiler;

import io.horrorshow.codey.api.WandboxResponse;

import java.util.ArrayList;
import java.util.List;

import static io.horrorshow.codey.discordutil.DiscordUtils.CHAR_LIMIT;


public class WandboxDiscordUtils {

    public static List<String> formatWandboxResponse(WandboxResponse wandboxResponse) {
        List<String> result = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        if (wandboxResponse.getStatus() != 0) {
            sb.append("```\nstatus code: ")
                    .append(wandboxResponse.getStatus())
                    .append("```\n");
        }
        appendAOrBTruncated(wandboxResponse.getCompiler_error(),
                wandboxResponse.getCompiler_message(), sb);

        var compilerStatusErrors = sb.toString();
        if (!compilerStatusErrors.isBlank()) {
            result.add(compilerStatusErrors);
        }

        var sb2 = new StringBuilder();
        appendAOrBTruncated(wandboxResponse.getProgram_message(),
                wandboxResponse.getProgram_output(), sb2);

        var output = sb2.toString();
        result.add((output.isBlank())
                ? "no output returned" : output);

        return result;
    }


    private static void appendAOrBTruncated(String a, String b, StringBuilder sb) {
        if (a != null) {
            sb.append("```\n");
            sb.append(truncateMessage(a, getRemainingChars(sb)));
            sb.append("```\n");
        } else if (b != null) {
            sb.append("```\n");
            sb.append(truncateMessage(b, getRemainingChars(sb)));
            sb.append("```\n");
        }
    }


    private static String truncateMessage(String msg, int remainder) {
        if (msg.length() > remainder) {
            return msg.substring(0, remainder);
        } else {
            return msg;
        }
    }


    private static int getRemainingChars(StringBuilder sb) {
        return CHAR_LIMIT - sb.length() - "```\n".length();
    }
}
