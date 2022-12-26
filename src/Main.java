import cg.CircuitBuilder;
import cg.common.Gate;
import cg.common.LogicComponent;
import cg.component.*;
import cg.optimization.PassThroughOptimizer;
import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        // this path can be substituted for an actual blueprint file so the creation can be built directly
        Path of = Path.of("blueprint.json");
        LogicComponent component = new CarryLookAheadAdder("my adder", 120);
//        LogicComponent component = new FullAdder("my adder");
//        LogicComponent component = new Register("my register", 64);
//        LogicComponent component = new Selector("my selector", 4);
//        LogicComponent component = new Multiplexer("my selector", 8, 16, 4);
//        LogicComponent component = new Memory("my memory", 16, 128, 8);
//        LogicComponent component = new Selection("my selector", 16, 8);
//        LogicComponent component = new Counter("counter", 8);
//        LogicComponent component = new RippleCarryAdder("rc", 16);

        String build = CircuitBuilder.build(component);
        Files.writeString(of, build.replaceAll("[ \r\t\n]", ""));
    }
}
