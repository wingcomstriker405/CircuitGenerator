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

        String build = build(component);
        Files.writeString(of, build.replaceAll("[ \r\t\n]", ""));
    }

    public static String build(LogicComponent component)
    {
        SynthesisContext context = new SynthesisContext();
        Circuit synthesise = component.synthesise(context);

        System.out.println("SYNTHESES RESULTS");
        System.out.println("GATES: " + synthesise.gates().size());
        System.out.println("CONNECTIONS: " + synthesise.gates().stream().map(Gate::outputs).mapToInt(List::size).sum());

        Set<Integer> io = new HashSet<>();
        synthesise.inputs().forEach((a, b) -> b.forEach(c -> io.add(c.id())));
        synthesise.outputs().forEach((a, b) -> b.forEach(c -> io.add(c.id())));

        PassThroughOptimizer.check(io, synthesise.gates());

        System.out.println("OPTIMIZATION RESULTS");
        System.out.println("GATES: " + synthesise.gates().size());
        System.out.println("CONNECTIONS: " + synthesise.gates().stream().map(Gate::outputs).mapToInt(List::size).sum());

        getRandomizedColor(synthesise.inputs());
        getRandomizedColor(synthesise.outputs());

        List<String> generated = synthesise.gates()
                .stream()
                .map(Gate::generate)
                .toList();

        String prefix = """
                {
                  "bodies": [
                    {
                      "childs": [
                """;

        String suffix = """
                ]
                    }
                  ],
                  "version": 4
                }
                """;
        String text = String.join(", ", generated);
        return prefix + text + suffix;
    }

    private static void getRandomizedColor(Map<String, List<Gate>> gates)
    {
        for (Map.Entry<String, List<Gate>> entry : gates.entrySet())
        {
            String color = getRandom();
            for (Gate gate : entry.getValue())
            {
                gate.color(color);
            }
        }
    }

    private static String getRandom()
    {
        return "%02x%02x%02x".formatted((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }
}
