package cg.common;

import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.util.List;

public abstract class LogicComponent
{
    private final String name;

    protected LogicComponent(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public abstract List<Vec> getOutputs();

    public abstract List<Vec> getInputs();

    public Vec getOutput(String name)
    {
        return find(name, getOutputs());
    }

    public Vec getInput(String name)
    {
        return find(name, getInputs());
    }

    protected Vec find(String name, List<Vec> vecs)
    {
        return vecs
                .stream()
                .filter(v -> v.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    protected void colorize(SynthesisContext context, List<Gate> gates)
    {
        String color = context.getColor();
        for (Gate gate : gates)
        {
            if(gate.color().equals("000000"))
            {
                gate.color(color);
            }
        }
    }

    public abstract Circuit synthesise(SynthesisContext context);
}
