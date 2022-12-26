package cg.component;

import cg.common.*;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class Memory extends DynamicComponent
{
    private final int length;
    public Memory(String name, int size, int length, int bits)
    {
        super(
                name,
                List.of(
                        new Vec("i", size),
                        new Vec("s", bits),
                        new Vec("w", 1)
                ),
                List.of(
                        new Vec("o", size)
                )
        );

        this.length = length;

        if (1 << bits < length)
            throw new RuntimeException("Too many cells / too few selection bits!");

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

        // connect to output
        for (int i = 0; i < size; i++)
            connect("c", i, "o", i);

        for (int i = 0; i < length; i++)
        {
            for (int k = 0; k < size; k++)
            {
                // self wired xor
                String c = "self" + i + "_" + k;
                add(c, Operation.XOR);
                connect(c, c);

                // difference
                String d = "diff" + i + "_" + k;
                add(d, Operation.XOR);

                // gatekeepers
                String g = "gate" + i + "_" + k;
                add(g, Operation.AND);

                // connect write
                connect("w0", g);
                // connect selection to gatekeepers
                connect("a" + i, g);

                // connect cycle
                connect(c, d);
                connect(d, g);
                connect(g, c);

                // result
                String r = "res" + i + "_" + k;
                add(r, Operation.AND);
                connect(c, r);

                // connect result to combiner
                connect(r, "c" + k);
                // connect input to difference
                connect("i" + k, d);
                // connect selection to result
                connect("a" + i, r);
            }
        }
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Gate> mapping, Map<String, Circuit> circuits)
    {
        int size = circuit.inputs().get("i").size();
        int bits = circuit.inputs().get("s").size();

        // input
        for (int i = 0; i < size; i++)
            mapping.get("i" + i).point(new Point(i, 0, 0));
        // address
        for (int i = 0; i < bits; i++)
            mapping.get("s" + i).point(new Point(size + i, 0, 0));
        // - negation
        for (int i = 0; i < bits; i++)
            mapping.get("n" + i).point(new Point(size + i, 1, 0));
        // - selection
        for (int i = 0; i < 1 << bits; i++)
            mapping.get("a" + i).point(new Point(size + i, 2, 0));
        // write
        mapping.get("w0").point(new Point(size + bits, 0, 0));
        // cells
        for (int i = 0; i < this.length; i++)
        {
            for (int k = 0; k < size; k++)
            {
                String d = "diff" + i + "_" + k;
                String g = "gate" + i + "_" + k;
                String c = "self" + i + "_" + k;
                String r = "res" + i + "_" + k;
                mapping.get(d).point(new Point(k, i + 1, 0));
                mapping.get(g).point(new Point(k, i + 1, 1));
                mapping.get(c).point(new Point(k, i + 1, 2));
                mapping.get(r).point(new Point(k, i + 1, 3));
            }
        }

        for (int i = 0; i < size; i++)
            mapping.get("c" + i).point(new Point(i, this.length + 1, 0));

        // output
        for (int i = 0; i < size; i++)
            mapping.get("o" + i).point(new Point(i, this.length + 2, 0));
    }
}
