import cg.CircuitBuilder;
import cg.common.LogicComponent;
import cg.component.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Example
{
    public static void main(String[] args) throws IOException
    {
        // this path can be substituted for an actual blueprint file so the creation can be built directly
        Path of = Path.of("blueprint.json");
        LogicComponent adder1 = new CarryLookAheadAdder("my adder", 64);
        LogicComponent adder2 = new RippleCarryAdder("rc", 16);
        LogicComponent adder3 = new FullAdder("my adder");
        LogicComponent register = new Register("my register", 64);
        LogicComponent selector = new Selector("my selector", 4);
        LogicComponent multiplexer = new Multiplexer("my multiplexer", 8, 16, 4);
        LogicComponent memory = new Memory("my memory", 16, 128, 8);
        LogicComponent selection = new Selection("my selector", 16, 8);
        LogicComponent counter = new Counter("counter", 8);

        String build = CircuitBuilder.build(multiplexer);
        Files.writeString(of, build.replaceAll("[ \r\t\n]", ""));
    }

    public static void coloring() throws IOException
    {
        // this path can be substituted for an actual blueprint file so the creation can be built directly
        Path of = Path.of("blueprint.json");
        LogicComponent component = new RegisterBank("rb", 8, 4, 4);
        Map<String, String> colorings = new HashMap<>();
        colorings.put("rb-r.", "123456");
        colorings.put("rb-sa", "456123");
        colorings.put("rb-sb", "614523");
        colorings.put("rb-sd", "236145");
        colorings.put("rb", "612345");

        String build = CircuitBuilder.build(component, false, colorings);
        Files.writeString(of, build.replaceAll("[ \r\t\n]", ""));
    }
}
