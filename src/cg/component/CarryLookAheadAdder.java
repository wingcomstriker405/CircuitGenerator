package cg.component;

import cg.common.*;
import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.util.ArrayList;
import java.util.List;

public class CarryLookAheadAdder extends DynamicComponent
{
    public CarryLookAheadAdder(String name, int size)
    {
        super(
                name,
                List.of(
                        new Vec("a", size),
                        new Vec("b", size)
                ),
                List.of(
                        new Vec("v", size),
                        new Vec("r", 1)
                )
        );

        // add propagators
        for (int i = 0; i < size; i++)
            add("p", i, Operation.XOR);

        // add generators
        for (int i = 0; i < size; i++)
            add("g", i, Operation.AND);

        // add summers
        for (int i = 0; i < size; i++)
            add("s", i, Operation.XOR);

        // connect propagators with summers
        for (int i = 0; i < size; i++)
            connect("p", i, "s", i);

        // connect numbers
        for (int i = 0; i < size; i++)
        {
            connect("a", i, "g", i);
            connect("b", i, "g", i);
            connect("a", i, "p", i);
            connect("b", i, "p", i);
        }

        // construct initial carry
        add("c", 0, Operation.OR);
        for (int i = 1; i <= size; i++)
        {
            // temporary name for the carry conditions
            String con = "t" + i + "_";

            // construct carry
            add("c", i, Operation.OR);

            // construct conditions
            for (int j = 0; j <= i; j++)
                add(con, j, Operation.AND);

            // connect conditions
            for (int j = 0; j <= i; j++)
                connect(con, j, "c", i);


            // construct carry from start
            connect("c", 0, con, 0);
            for (int j = 0; j < i; j++)
                connect("p", j, con, 0);

            // construct inner propagation
            for (int j = 0; j < i; j++)
            {
                // add the corresponding generator
                connect("g", j, con, j + 1);

                // add the propagators
                for (int k = j + 1; k < i; k++)
                {
                    connect("p", k, con, j + 1);
                }
            }
        }


        // connect carry results to summers
        for (int i = 0; i < size; i++)
            connect("c", i, "s", i);


        // connect summers to outputs
        for (int i = 0; i < size; i++)
            connect("s", i, "v", i);

        // connect carry output
        connect("c", size, "r", 0);
    }
}
