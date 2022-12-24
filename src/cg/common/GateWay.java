package cg.common;

import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GateWay extends LogicComponent
{
    private final List<Vec> vectors;

    protected GateWay(String name, List<Vec> vectors)
    {
        super(name);
        this.vectors = vectors;
    }

    @Override
    public List<Vec> getInputs()
    {
        return this.vectors;
    }

    @Override
    public Circuit synthesise(SynthesisContext context)
    {
        List<Gate> gates = new ArrayList<>();
        Map<String, List<Gate>> io = new HashMap<>();
        for (Vec vector : this.vectors)
        {
            List<Gate> current = new ArrayList<>();
            for (int i = 0; i < vector.getSize(); i++)
            {
                Gate g = new Gate(
                        Operation.AND,
                        context.next(),
                        "000000",
                        new ArrayList<>()
                );
                current.add(g);
                gates.add(g);
            }
            io.put(vector.getName(), current);
        }
        return new Circuit(io, io, gates);
    }

    @Override
    public List<Vec> getOutputs()
    {
        return this.vectors;
    }
}
