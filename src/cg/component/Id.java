package cg.component;

import cg.common.DynamicComponent;
import cg.common.Gate;
import cg.common.Vec;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class Id extends DynamicComponent
{
    public Id(String name, int size)
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

        for (int i = 0; i < size; i++)
            connect("i", i, "o", i);
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Gate> mapping, Map<String, Circuit> circuits)
    {
        int size = circuit.inputs().get("i").size();
        for (int i = 0; i < size; i++)
        {
            mapping.get("i" + i).move(i, 0, 0);
            mapping.get("o" + i).move(i, 1, 0);
        }
    }
}
