package cg.common;

import json.JsonElement;
import json.JsonList;
import json.JsonNumber;
import json.JsonParser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static cg.common.Assert.assertThat;

/**
 * A parser to load a blueprint file. The parser assumes that all the parts in the blueprint file are logic gates.
 */
public class BlueprintParser
{
    private BlueprintParser()
    {
    }

    /**
     * Parses a blueprint into a {@link BluePrint}.
     * @param path the path of the file
     * @return the parsed blueprint
     */
    public static BluePrint parse(String path)
    {
        List<Gate> gates = new ArrayList<>();
        JsonList bodies = JsonParser.parse(Path.of(path)).getList("bodies");
        assertThat(bodies.size() == 1, "Expected exactly one body in blueprint!");
        JsonList childs = bodies.get(0).getList("childs");
        for (JsonElement child : childs)
        {
            JsonElement controller = child.get("controller");
            int mode = asInt(controller.getNumber("mode"));
            String color = child.getString("color").getValue();
            Operation operation = Operation.ofMode(mode);
            int id = asInt(controller.getNumber("id"));
            List<Integer> outputs = new ArrayList<>();
            if (controller.getList("controllers") != null)
            {
                for (JsonElement c : controller.getList("controllers"))
                {
                    outputs.add(asInt(c.getNumber("id")));
                }
            }
            Point p = new Point(
                    asInt(child.get("pos").getNumber("x")),
                    asInt(child.get("pos").getNumber("y")),
                    asInt(child.get("pos").getNumber("z"))
            );
            gates.add(new Gate(operation, id, color, outputs, p));
        }
        BluePrint bp = new BluePrint(gates);
        bp.normalize();
        return bp;
    }

    private static int asInt(JsonNumber num)
    {
        return (int) (double) num.getValue();
    }
}
