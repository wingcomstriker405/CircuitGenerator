package cg.component;

import cg.common.*;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class Shift extends DynamicComponent
{
    private static int getNumberOfBits(int amount)
    {
        int count = 1;
        while (1 << count < amount)
            count++;
        return count;
    }

    private final List<Integer> steps;
    public Shift(String name, int size, List<Integer> steps)
    {
        super(
                name,
                List.of(
                        new Vec("i", size),
                        new Vec("a", size)
                ),
                List.of(
                        new Vec("o", size)
                )
        );

        this.steps = steps;

        int bits = getNumberOfBits(steps.size());
        // add multiplexer
        add(new Multiplexer("mux", size, steps.size(), bits));

        // add shift gates
        for (int i = 0; i < steps.size(); i++)
            for (int j = 0; j < size; j++)
                add("s" + i + "_", j, Operation.AND);

        // connect input to shift gates
        for (int i = 0; i < steps.size(); i++)
            for (int j = 0; j < size; j++)
                if(0 <= j + steps.get(i) && j + steps.get(i) < size)
                {
                    connect("i", j, "s" + i + "_", j + steps.get(i));
                }

        // connect shift to mux
        for (int i = 0; i < bits; i++)
            connect("a", i, "mux_s", i);
        // connect to output
        for (int i = 0; i < size; i++)
            connect("mux_o", i, "o", i);
        // connect shift gates to mux
        for (int i = 0; i < steps.size(); i++)
            for (int j = 0; j < size; j++)
                if(0 <= j + steps.get(i) && j + steps.get(i) < size)
                {
                    connect("s" + i + "_", j, "mux_i" + i + "_", j);
                }

    }

    @Override
    protected void layout(Circuit circuit, Map<String, Gate> mapping, Map<String, Circuit> circuits)
    {
        int dy = 0;
        List<Gate> n = circuit.inputs().get("i");
        int size = n.size();

        // move input
        for (int i = 0; i < size; i++)
            n.get(i).move(i, dy, 0);

        List<Gate> a = circuit.inputs().get("a");
        int bits = a.size();
        for (int i = 0; i < a.size(); i++)
            a.get(i).move(i, 0, 1);


        dy++;

        // move shift gates
        for (int i = 0; i < this.steps.size(); i++)
        {
            for (int j = 0; j < size; j++)
            {
                mapping.get("s" + i + "_" + j).move(j, dy, 0);
            }
            dy++;
        }


        // move mux
        circuits.get("mux").move(0, dy, 0);
        BoundingBox box = BoundingBox.of(circuits.get("mux").gates());
        dy += box.max().y() - box.min().y() + 1;

        // move output
        for (int i = 0; i < size; i++)
            mapping.get("o" + i).move(i, dy, 0);
    }
}
