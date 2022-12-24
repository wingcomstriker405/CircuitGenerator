import cg.common.Gate;
import cg.common.LogicComponent;
import cg.component.AdderNBit;
import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        // this path can be substituted for an actual blueprint file so the creation can be built directly
        Path of = Path.of("blueprint.json");
        LogicComponent component = new AdderNBit("my adder", 8);
        String build = build(component);
        Files.writeString(of, build);
    }

    public static String build(LogicComponent component)
    {
        SynthesisContext context = new SynthesisContext();
        Circuit synthesise = component.synthesise(context);

        Set<Integer> removed = new HashSet<>();

        List<Gate> inputs = getRandomizedColor(removed, synthesise.inputs());
        List<Gate> outputs = getRandomizedColor(removed, synthesise.outputs());

        List<String> generated = new ArrayList<>();
        IntStream.range(0, inputs.size())
                .mapToObj(i -> inputs.get(i).generate(i, 0, 0))
                .forEach(generated::add);

        int couter = 0;
        for (Gate gate : synthesise.gates())
        {
            if (!removed.contains(gate.id()))
            {
                generated.add(gate.generate(couter, 1, 0));
                couter++;
            }
        }

        IntStream.range(0, outputs.size())
                .mapToObj(i -> outputs.get(i).generate(i, 2, 0))
                .forEach(generated::add);

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

    private static List<Gate> getRandomizedColor(Set<Integer> removed, Map<String, List<Gate>> gates)
    {
        List<Gate> changed = new ArrayList<>();
        for (Map.Entry<String, List<Gate>> entry : gates.entrySet())
        {
            String color = getRandom();
            for (Gate gate : entry.getValue())
            {
                changed.add(new Gate(gate.op(), gate.id(), color, gate.outputs()));
                removed.add(gate.id());
            }
        }
        return changed;
    }

    private static String getRandom()
    {
        return "%02x%02x%02x".formatted((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }
}
