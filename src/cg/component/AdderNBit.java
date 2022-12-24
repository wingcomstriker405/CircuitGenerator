package cg.component;

import cg.common.ComplexComponent;
import cg.common.Vec;

import java.util.List;

public class AdderNBit extends ComplexComponent
{
    public AdderNBit(String name, int size)
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
}
