package cg.component;

import cg.common.*;
import cg.synthesis.Circuit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Multiplexer extends DynamicComponent
{

    public Multiplexer(String name, int size, int inputs, int bits)
    {
        super(
                name,
                constructInputs(size, inputs, bits),
                List.of(
                        new Vec("o", size)
                )
        );

        if(1 << bits < inputs)
            throw new RuntimeException("Too many inputs / too few selection bits!");

        // create the selector

        // add negations
        for (int i = 0; i < bits; i++)
            add("n", i, Operation.NOR);

        // connect to negation
        for (int i = 0; i < bits; i++)
            connect("s", i, "n", i);

        // add negations
        for (int i = 0; i < 1 << bits; i++)
            add("a", i, Operation.AND);

        // connect all the gates
        for (int i = 0; i < 1 << bits; i++)
        {
            for (int k = 0; k < bits; k++)
            {
                String source = (i >> k) % 2 == 1 ? "s" : "n";
                connect(source, k, "a", i);
            }
        }

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
                connect("a", i, s, k);
                connect(p, k, s, k);
                connect(s, k, "c", k);
            }
        }

        // connect to outputs
        for (int i = 0; i < size; i++)
            connect("c", i, "o", i);
    }

    private static List<Vec> constructInputs(int size, int inputs, int bits)
    {
        List<Vec> vectors = new ArrayList<>();
        for (int i = 0; i < inputs; i++)
            vectors.add(new Vec("i" + i + "_", size));
        vectors.add(new Vec("s", bits));
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
        }

        int bits = circuit.inputs().get("s").size();
        // move selection bits
        for (int i = 0; i < bits; i++)
            mapping.get("s" + i).point(new Point(size + i, 0, 0));

        // move negations
        for (int i = 0; i < bits; i++)
            mapping.get("n" + i).point(new Point(size + i, 1, 0));

        // move and gates of selector
        for (int i = 0; i < 1 << bits; i++)
            mapping.get("a" + i).point(new Point(size + i, 2, 0));

        // move combination
        for (int i = 0; i < size; i++)
            mapping.get("c" + i).point(new Point(i, 2, 0));

        // move outputs
        for (int i = 0; i < size; i++)
            mapping.get("o" + i).point(new Point(i, 3, 0));
    }
}
