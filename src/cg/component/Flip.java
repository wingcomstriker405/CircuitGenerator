package cg.component;

import cg.common.DynamicComponent;
import cg.common.Gate;
import cg.common.Operation;
import cg.common.Vec;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class Flip extends DynamicComponent
{
    public Flip(String name, int size)
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

        // add flip gates
        for (int i = 0; i < size; i++)
            add("f", i, Operation.NOR);

        // connect inputs to flip gates
        for (int i = 0; i < size; i++)
            connect("i", i, "f", i);

        // connect flip gates to outputs
        for (int i = 0; i < size; i++)
            connect("f", i, "o", i);
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Gate> mapping, Map<String, Circuit> circuits)
    {
        int size = circuit.inputs().get("i").size();
        // move all gates
        for (int i = 0; i < size; i++)
        {
            mapping.get("i" + i).move(i, 0, 0);
            mapping.get("f" + i).move(i, 1, 0);
            mapping.get("o" + i).move(i, 2, 0);
        }
    }
}
