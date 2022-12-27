package cg.component;

import cg.common.DynamicComponent;
import cg.common.Gate;
import cg.common.Operation;
import cg.common.Vec;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class SingleAdd extends DynamicComponent
{

    public SingleAdd(String name, int size)
    {
        super(
                name,
                List.of(
                        new Vec("i", size)
                ),
                List.of(
                        new Vec("o", size)
                )
        );

        // add true
        add("t0", Operation.AND);
        add("t1", Operation.NOR);

        connect("t0", "t1");

        // add diff
        for (int i = 0; i < size; i++)
            add("d", i, Operation.XOR);
        // add ands
        for (int i = 0; i < size; i++)
            add("a", i, Operation.AND);

        // connect true to first diff
        connect("t1", "d0");

        // connect input to diff
        for (int i = 0; i < size; i++)
            connect("i", i, "d", i);

        // connect input to ands
        for (int i = 0; i < size; i++)
            connect("i", i, "a", i);

        // interconnect ands
        for (int i = 0; i < size - 1; i++)
            connect("a", i, "a", i + 1);

        // connect ands to next diff
        for (int i = 0; i < size - 1; i++)
            connect("a", i, "d", i + 1);

        // connect diff to output
        for (int i = 0; i < size; i++)
            connect("d", i, "o", i);
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Gate> mapping, Map<String, Circuit> circuits)
    {
        int size = circuit.inputs().get("i").size();
        for (int i = 0; i < size; i++)
        {
            mapping.get("i" + i).move(i, 0, 0);
            mapping.get("d" + i).move(i, 1, 0);
            mapping.get("a" + i).move(i, 2, 0);
            mapping.get("o" + i).move(i, 3, 0);
        }
        mapping.get("t0").move(size, 1, 0);
        mapping.get("t1").move(size, 2, 0);
    }
}
