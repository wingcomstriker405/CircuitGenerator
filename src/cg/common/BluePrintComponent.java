package cg.common;

import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.util.Collections;
import java.util.List;

public abstract class BluePrintComponent extends LogicComponent
{
    private final String file;
    private final List<Pin> inputs;
    private final List<Pin> outputs;

    protected BluePrintComponent(String name, String file, List<Pin> inputs, List<Pin> outputs)
    {
        super(name);
        this.file = file;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    @Override
    public List<Vec> getOutputs()
    {
        return Collections.unmodifiableList(this.outputs);
    }

    @Override
    public List<Vec> getInputs()
    {
        return Collections.unmodifiableList(this.inputs);
    }

    @Override
    public Circuit synthesise(SynthesisContext context)
    {
        return context.instantiate(this.file, this.inputs, this.outputs);
    }
}
