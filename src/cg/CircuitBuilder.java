package cg;

import cg.common.Gate;
import cg.common.LogicComponent;
import cg.optimization.PassThroughOptimizer;
import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CircuitBuilder
{
    private CircuitBuilder(){}

    public static String build(LogicComponent component)
    {
        return build(component, false);
    }

    public static String build(LogicComponent component, boolean optimize)
    {
        SynthesisContext context = new SynthesisContext();
        Circuit synthesise = component.synthesise(context);

        System.out.println("SYNTHESES RESULTS");
        System.out.println("GATES: " + synthesise.gates().size());
        System.out.println("CONNECTIONS: " + synthesise.gates().stream().map(Gate::outputs).mapToInt(List::size).sum());

        if(optimize)
        {
            Set<Integer> io = new HashSet<>();
            synthesise.inputs().forEach((a, b) -> b.forEach(c -> io.add(c.id())));
            synthesise.outputs().forEach((a, b) -> b.forEach(c -> io.add(c.id())));

            PassThroughOptimizer.check(io, synthesise.gates());

            System.out.println("OPTIMIZATION RESULTS");
            System.out.println("GATES: " + synthesise.gates().size());
            System.out.println("CONNECTIONS: " + synthesise.gates().stream().map(Gate::outputs).mapToInt(List::size).sum());
        }

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
