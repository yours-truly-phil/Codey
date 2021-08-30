package io.horrorshow.codey.api.piston;

import java.util.Map;


public record CompilerInfo(Map<String, PistonRuntime> compilerMap) {

}
