package cg.component;

import cg.common.*;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

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

    @Override
    protected void layout(Circuit circuit, Map<String, Gate> mapping)
    {
        List<Gate> a = circuit.inputs().get("a");
        List<Gate> b = circuit.inputs().get("b");
        List<Gate> v = circuit.outputs().get("v");
        int size = a.size();
        int max = size + 6;
        for (int i = 0; i < size; i++)
        {
            a.get(i).point(new Point(i, 0, 0));
            b.get(i).point(new Point(i, 0, 1));
            v.get(i).point(new Point(i, max, 0));

            mapping.get("s" + i).point(new Point(i, 1, 0));
            mapping.get("g" + i).point(new Point(i, 2, 0));
            mapping.get("p" + i).point(new Point(i, 3, 0));
            mapping.get("c" + i).point(new Point(i, 4, 0));

            for (int j = 0; j <= size; j++)
            {
                if(mapping.containsKey("t" + (i + 1) + "_" + j))
                {
                    mapping.get("t" + (i + 1) + "_" + j).point(new Point(i, 5 + j, 0));
                }
            }
        }
        circuit.outputs().get("r").get(0).point(new Point(size, max, 0));
        mapping.get("c" + size).point(new Point(size, max - 1, 0));
    }
}
