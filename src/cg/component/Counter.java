package cg.component;

import cg.common.DynamicComponent;
import cg.common.Gate;
import cg.common.Operation;
import cg.common.Vec;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class Counter extends DynamicComponent
{
    private final int size;
    public Counter(String name, int size)
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

        this.size = size;

        // add the central register
        add(new Register("r", size));

        // add negation
        add("n", Operation.NAND);

        // connect set to inputs
        for (int i = 0; i < size; i++)
            connect("s0", "i" + i);

        // add inc gates
        for (int i = 0; i < size; i++)
            add("c", i, Operation.AND);

        // connect set to negation
        connect("s0", "n");

        // connect negation to inc gates
        for (int i = 0; i < size; i++)
            connect("n", "c" + i);

        // add combination combinations
        for (int i = 0; i < size; i++)
            add("b", i, Operation.OR);

        // connect input to combinations
        for (int i = 0; i < size; i++)
            connect("i", i, "b", i);

        // connect inc gates to register
        for (int i = 0; i < size; i++)
            connect("c", i, "b", i);

        // connect combination gates to register
        for (int i = 0; i < size; i++)
            connect("b", i, "r_i", i);

        // connect register to output
        for (int i = 0; i < size; i++)
            connect("r_i", i, "o", i);

        // add diff gates
        for (int i = 0; i < size; i++)
            add("d", i, Operation.XOR);

        // connect register to diff gates
        for (int i = 0; i < size; i++)
            connect("r_o", i, "d", i);

        // add and gates
        for (int i = 0; i < size; i++)
            add("a", i, Operation.AND);

        // connect register to and gates
        for (int i = 0; i < size; i++)
            connect("r_o", i, "a", i);

        // interconnect and gates
        for (int i = 0; i < size - 1; i++)
            connect("a", i, "a", i + 1);

        // add true source
        add("t0", Operation.AND);
        add("t1", Operation.NOR);

        connect("t0", "t1");

        // connect true to first diff
        connect("t1", "d0");

        // connect and to next diff gate
        for (int i = 0; i < size - 1; i++)
            connect("a", i, "d", i + 1);

        // connect diff to inc gates
        for (int i = 0; i < size; i++)
            connect("d", i, "c", i);
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Gate> mapping, Map<String, Circuit> circuits)
    {
        // move s
        mapping.get("s0").move(this.size, 0, 0);

        // move n
        mapping.get("n").move(this.size, 1, 0);

        // move true
        mapping.get("t0").move(this.size, 2, 0);
        mapping.get("t1").move(this.size, 3, 0);

        // move inputs
        for (int i = 0; i < this.size; i++)
            mapping.get("i" + i).move(i, 0, 0);

        // move inc gates
        for (int i = 0; i < this.size; i++)
            mapping.get("c" + i).move(i, 1, 0);

        // move combination gates
        for (int i = 0; i < this.size; i++)
            mapping.get("b" + i).move(i, 2, 0);

        // move diff gates
        for (int i = 0; i < this.size; i++)
            mapping.get("d" + i).move(i, 7, 0);

        // move and gates
        for (int i = 0; i < this.size; i++)
            mapping.get("a" + i).move(i, 8, 0);

        // move output gates
        for (int i = 0; i < this.size; i++)
            mapping.get("o" + i).move(i, 9, 0);

        // move register
        circuits.get("r").move(0, 3, 0);
    }
}
