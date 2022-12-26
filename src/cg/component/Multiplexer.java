package cg.component;

import cg.common.*;
import cg.synthesis.Circuit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Multiplexer extends ComplexComponent
{

    private final int size;
    private final int inputs;
    private final int bits;

    public Multiplexer(String name, int size, int inputs, int bits)
    {
        super(
                name,
                constructInputs(size, inputs, bits),
                List.of(
                        new Vec("o", size)
                )
        );

        this.size = size;
        this.inputs = inputs;
        this.bits = bits;

        if(1 << bits < inputs)
            throw new RuntimeException("Too many inputs / too few selection bits!");

        // add selector and selection
        add(new Selection("selection", size, inputs));
        add(new Selector("selector", bits));

        // wire inputs
        for (int i = 0; i < inputs; i++)
            connect("<" , "i" + i + "_", "selection", "i" + i + "_");

        // wire outputs
        connect("selection", "o", ">", "o");

        // wire address
        connect("<", "s",  "selector", "i");

        // wire selector and selection
        connect("selector", "o", "selection", "s");
    }

    private static List<Vec> constructInputs(int size, int inputs, int bits)
    {
        List<Vec> vectors = new ArrayList<>();
        for (int i = 0; i < inputs; i++)
            vectors.add(new Vec("i" + i + "_", size));
        vectors.add(new Vec("s", bits));
        return vectors;
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Circuit> circuits)
    {
        // move selector and selection
        circuits.get("selection").move(0, 1, 0);
        circuits.get("selector").move(this.size + 1, 1, 0);

        // move address
        List<Gate> s = circuit.inputs().get("s");
        for (int i = 0; i < s.size(); i++)
            s.get(i).move(this.size + 1 + i, 0, 0);

        // move inputs
        for (int i = 0; i < this.inputs; i++)
        {
            List<Gate> gates = circuit.inputs().get("i" + i + "_");
            for (int k = 0; k < gates.size(); k++)
            {
                gates.get(k).move(k, 0, i);
            }
        }

        // move outputs
        List<Gate> o = circuit.outputs().get("o");
        for (int i = 0; i < o.size(); i++)
            o.get(i).move(i, 5, 0);
    }
}
