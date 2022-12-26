package cg.component;

import cg.common.*;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class Register extends DynamicComponent
{
    public Register(String name, int size)
    {
        super(
                name,
                List.of(
                        new Vec("i", size),
                        new Vec("s", 1)
                ),
                List.of(
                        new Vec("o", size)
                )
        );

        // add the memory cells
        for (int i = 0; i < size; i++)
            add("c", i, Operation.XOR);

        // add difference checkers
        for (int i = 0; i < size; i++)
            add("d", i, Operation.XOR);

        // add gatekeepers
        for (int i = 0; i < size; i++)
            add("g", i, Operation.AND);

        // connect the set bit with the gatekeepers
        for (int i = 0; i < size; i++)
            connect("s0", "g" + i);

        // self wire all the cells
        for (int i = 0; i < size; i++)
            connect("c", i, "c", i);

        // connect the ring
        for (int i = 0; i < size; i++)
        {
            connect("c", i, "d", i);
            connect("d", i, "g", i);
            connect("g", i, "c", i);
        }

        // connect the io
        for (int i = 0; i < size; i++)
        {
            connect("i", i, "d", i);
            connect("c", i, "o", i);
        }
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Gate> mapping, Map<String, Circuit> circuits)
    {
        int size = circuit.inputs().get("i").size();
        for (int i = 0; i < size; i++)
        {
            mapping.get("i" + i).point(new Point(i, 0, 0));
            mapping.get("d" + i).point(new Point(i, 1, 0));
            mapping.get("g" + i).point(new Point(i, 1, 1));
            mapping.get("c" + i).point(new Point(i, 2, 0));
            mapping.get("o" + i).point(new Point(i, 3, 0));
        }
        mapping.get("s0").point(new Point(-1, 1, 1));
        mapping.get("s0").color("123456");
    }
}
