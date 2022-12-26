package cg.component;

import cg.common.*;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class RegisterBank extends DynamicComponent
{
    private final int size;
    private final int amount;
    private final int bits;

    public RegisterBank(String name, int size, int amount, int bits)
    {
        super(
                name,
                List.of(
                        new Vec("i", size),
                        new Vec("aa", bits),
                        new Vec("ab", bits),
                        new Vec("ad", bits),
                        new Vec("w", 1)
                ),
                List.of(
                        new Vec("a", size),
                        new Vec("b", size)
                )
        );

        this.size = size;
        this.amount = amount;
        this.bits = bits;

        // add registers
        for (int i = 0; i < amount; i++)
            add(new Register("r" + i, size));

        // add a, b, d selectors
        add(new Selector("sa", bits));
        add(new Selector("sb", bits));
        add(new Selector("sd", bits));

        // add ac gates
        for (int i = 0; i < amount; i++)
            for (int j = 0; j < size; j++)
                add("ac" + i + "_", j, Operation.AND);
        // connect registers to ac
        for (int i = 0; i < amount; i++)
            for (int j = 0; j < size; j++)
                connect("r" + i + "_o", j, "ac" + i + "_", j);

        // add combiner a
        for (int i = 0; i < size; i++)
            add("ca", i, Operation.OR);

        // connect combiner a to a
        for (int i = 0; i < size; i++)
            connect("ca", i, "a", i);

        // connect selector a to ac
        for (int i = 0; i < amount; i++)
            for (int j = 0; j < size; j++)
                connect("sa_o", i, "ac" + i + "_", j);

        // connect ac to a (output)
        for (int i = 0; i < amount; i++)
            for (int j = 0; j < size; j++)
                connect("ac" + i + "_", j, "ca", j);


        // add bc gates
        for (int i = 0; i < amount; i++)
            for (int j = 0; j < size; j++)
                add("bc" + i + "_", j, Operation.AND);
        // connect registers to bc
        for (int i = 0; i < amount; i++)
            for (int j = 0; j < size; j++)
                connect("r" + i + "_o", j, "bc" + i + "_", j);

        // add combiner b
        for (int i = 0; i < size; i++)
            add("cb", i, Operation.OR);

        // connect combiner b to b
        for (int i = 0; i < size; i++)
            connect("cb", i, "b", i);

        // connect selector b to bc
        for (int i = 0; i < amount; i++)
            for (int j = 0; j < size; j++)
                connect("sb_o", i, "bc" + i + "_", j);

        // connect bc to b (output)
        for (int i = 0; i < amount; i++)
            for (int j = 0; j < size; j++)
                connect("bc" + i + "_", j, "cb", j);

        // connect i to registers
        for (int i = 0; i < amount; i++)
            for (int j = 0; j < size; j++)
                connect("i", j, "r" + i + "_i", j);

        // connect write to registers
        for (int i = 0; i < amount; i++)
            connect("w", 0, "r" + i + "_s", 0);

        // connect destination to registers
        for (int i = 0; i < amount; i++)
        {
            connect("sd_o", i, "r" + i + "_s", 0);
        }

        // connect address input with selectors
        for (int i = 0; i < amount; i++)
        {
            for (int j = 0; j < bits; j++)
            {
                connect("aa", j, "sa_i", j);
                connect("ab", j, "sb_i", j);
                connect("ad", j, "sd_i", j);
            }
        }
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Gate> mapping, Map<String, Circuit> circuits)
    {
        // move registers
        for (int i = 0; i < this.amount; i++)
            circuits.get("r" + i).move(0, 1 + 4 * i, 0);

        // move selectors
        circuits.get("sa").move(this.size, 1, 0);
        circuits.get("sb").move(this.size + this.bits, 1, 0);
        circuits.get("sd").move(this.size + this.bits * 2, 1, 0);

        // move input
        for (int i = 0; i < this.size; i++)
            mapping.get("i" + i).move(i, 0, 0);

        // move address
        for (int i = 0; i < this.bits; i++)
        {
            mapping.get("aa" + i).move(this.size + i, 0, 0);
            mapping.get("ab" + i).move(this.size + this.bits + i, 0, 0);
            mapping.get("ad" + i).move(this.size + this.bits * 2 + i, 0, 0);
        }

        // move ac and bc gates
        for (int i = 0; i < this.amount; i++)
        {
            for (int j = 0; j < this.size; j++)
            {
                mapping.get("ac" + i + "_" + j).move(j, this.amount * 4 + 1 + i, 0);
                mapping.get("bc" + i + "_" + j).move(this.size + j, this.amount * 4 + 1 + i, 0);
            }
        }

        // move outputs
        for (int i = 0; i < this.size; i++)
        {
            mapping.get("a" + i).move(i, this.amount * 5 + 2, 0);
            mapping.get("b" + i).move(this.size + i, this.amount * 5 + 2, 0);
        }

        // move combiners
        for (int i = 0; i < this.size; i++)
        {
            mapping.get("ca" + i).move(i, this.amount * 5 + 1, 0);
            mapping.get("cb" + i).move(this.size + i, this.amount * 5 + 1, 0);
        }

        // move set input
        mapping.get("w0").move(this.size + this.bits * 3, 0, 0);
    }
}
