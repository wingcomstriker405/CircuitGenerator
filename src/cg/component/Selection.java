package cg.component;

import cg.common.*;
import cg.synthesis.Circuit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Selection extends DynamicComponent
{
    public Selection(String name, int size, int inputs)
    {
        super(
                name,
                constructInputs(size, inputs),
                List.of(
                        new Vec("o", size)
                )
        );

        // add combination
        for (int i = 0; i < size; i++)
            add("c", i, Operation.OR);

        // create the selection
        for (int i = 0; i < inputs; i++)
        {
            String p = "i" + i + "_";
            String s = "p" + i + "_";
            for (int k = 0; k < size; k++)
            {
                add(s, k, Operation.AND);
                connect("s", i, s, k);
                connect(p, k, s, k);
                connect(s, k, "c", k);
            }
        }

        // connect to outputs
        for (int i = 0; i < size; i++)
            connect("c", i, "o", i);
    }

    private static List<Vec> constructInputs(int size, int inputs)
    {
        List<Vec> vectors = new ArrayList<>();
        for (int i = 0; i < inputs; i++)
            vectors.add(new Vec("i" + i + "_", size));
        vectors.add(new Vec("s", inputs));
        return vectors;
    }
    @Override
    protected void layout(Circuit circuit, Map<String, Gate> mapping)
    {
        int inputs = circuit.inputs().size() - 1;
        int size = circuit.inputs().get("i0_").size();

        // move the inputs and selection
        for (int i = 0; i < inputs; i++)
        {
            for (int k = 0; k < size; k++)
            {
                mapping.get("i" + i + "_" + k).point(new Point(k, 0, i));
                mapping.get("p" + i + "_" + k).point(new Point(k, 1, i));
            }
            mapping.get("s" + i).point(new Point(size, 0, i));
        }

        // move combination
        for (int i = 0; i < size; i++)
            mapping.get("c" + i).point(new Point(i, 2, 0));

        // move outputs
        for (int i = 0; i < size; i++)
            mapping.get("o" + i).point(new Point(i, 3, 0));
    }
}
