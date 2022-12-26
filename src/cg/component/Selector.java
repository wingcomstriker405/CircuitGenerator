package cg.component;

import cg.common.*;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class Selector extends DynamicComponent
{
    public Selector(String name, int bits)
    {
        super(
                name,
                List.of(
                        new Vec("i", bits)
                ),
                List.of(
                        new Vec("o", 1 << bits)
                )
        );

        // add negations
        for (int i = 0; i < bits; i++)
            add("n", i, Operation.NOR);

        // connect to negations
        for (int i = 0; i < bits; i++)
            connect("i", i, "n", i);

        // connect all the gates
        for (int i = 0; i < 1 << bits; i++)
        {
            for (int k = 0; k < bits; k++)
            {
                String source = (i >> k) % 2 == 1 ? "i" : "n";
                connect(source, k, "o", i);
            }
        }
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Gate> mapping, Map<String, Circuit> circuits)
    {
        int bits = circuit.inputs().get("i").size();
        for (int i = 0; i < bits; i++)
            mapping.get("i" + i).point(new Point(i, 0, 0));

        for (int i = 0; i < bits; i++)
            mapping.get("n" + i).point(new Point(i, 1, 0));

        for (int i = 0; i < 1 << bits; i++)
            mapping.get("o" + i).point(new Point(i % bits, 2 + i / bits, 0));
    }
}
