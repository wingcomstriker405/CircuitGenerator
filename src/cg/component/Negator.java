package cg.component;

import cg.common.ComplexComponent;
import cg.common.Gate;
import cg.common.Vec;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class Negator extends ComplexComponent
{
    public Negator(String name, int size)
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

        add(new Flip("f", size));
        add(new SingleAdd("a", size));

        connect("<", "i", "f", "i");
        connect("f", "o", "a", "i");
        connect("a", "o", ">", "o");
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Circuit> circuits)
    {
        int size = circuit.inputs().get("i").size();
        List<Gate> i = circuit.inputs().get("i");
        List<Gate> o = circuit.outputs().get("o");
        for (int k = 0; k < size; k++)
        {
            i.get(k).move(k, 0, 0);
            o.get(k).move(k, 7, 0);
        }

        circuits.get("f").move(0, 1, 0);
        circuits.get("a").move(0, 4, 0);
    }
}
