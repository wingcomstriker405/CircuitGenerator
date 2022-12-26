import cg.common.Gate;
import cg.common.LogicComponent;
import cg.component.CarryLookAheadAdder;
import cg.component.FullAdder;
import cg.component.Register;
import cg.component.RippleCarryAdder;
import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        // this path can be substituted for an actual blueprint file so the creation can be built directly
        Path of = Path.of("blueprint.json");
//        LogicComponent component = new CarryLookAheadAdder("my adder", 32);
//        LogicComponent component = new FullAdder("my adder");

        LogicComponent component = new Register("my register", 64);
        String build = build(component);
        Files.writeString(of, build.replaceAll("[ \r\t\n]", ""));
    }

    public static String build(LogicComponent component)
    {
        SynthesisContext context = new SynthesisContext();
        Circuit synthesise = component.synthesise(context);

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
