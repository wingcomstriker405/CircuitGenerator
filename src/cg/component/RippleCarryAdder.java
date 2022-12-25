package cg.component;

import cg.common.ComplexComponent;
import cg.common.Gate;
import cg.common.Vec;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class RippleCarryAdder extends ComplexComponent
{
    public RippleCarryAdder(String name, int size)
    {
        // configure io
        super(
                name,
                List.of(
                        new Vec("a", size),
                        new Vec("b", size)
                ),
                List.of(
                        new Vec("v", size),
                        new Vec("c", 1)
                )
        );

        // generate full adder
        for (int i = 0; i < size; i++)
            add(new FullAdder("a" + i));

        // wire numbers
        for (int i = 0; i < size; i++)
        {
            connect("<", "a", i, "a" + i, "a", 0, 1);
            connect("<", "b", i, "a" + i, "b", 0, 1);
            connect("a" + i, "v", 0, ">", "v", i, 1);
        }
        // wire carries
        for (int i = 0; i < size - 1; i++)
            connect("a" + i, "c", "a" + (i + 1), "c");

        // wire output carry
        connect("a" + (size - 1), "c", ">", "c");
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Circuit> components)
    {
        int size = circuit.inputs().get("a").size();
        for (int i = 0; i < size; i++)
        {
            Circuit c = components.get("a" + i);
            c.move(i * 3, 1, 0);
        }

        int start = 0;
        for (Vec vec : getInputs())
        {
            List<Gate> gates = circuit.inputs().get(vec.getName());
            for (int i = 0; i < vec.getSize(); i++)
            {
                gates.get(i).move(start++, 0, 0);
            }
        }

        int end = 0;
        for (Vec vec : getOutputs())
        {
            List<Gate> gates = circuit.outputs().get(vec.getName());
            for (int i = 0; i < vec.getSize(); i++)
            {
                gates.get(i).move(end++, 4, 0);
            }
        }
    }
}
