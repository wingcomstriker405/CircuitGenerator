package cg.common;

import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        return instantiate(context, this.file, this.inputs, this.outputs);
    }

    public Circuit instantiate(SynthesisContext context, String path, List<Pin> ins, List<Pin> outs)
    {
        BluePrint bp = context.load(path);
        int[] ids = IntStream.range(0, bp.gates().size())
                .map(i -> context.next())
                .toArray();
        List<Gate> instances = new ArrayList<>();
        for (Gate gate : bp.gates())
        {
            instances.add(new Gate(
                    gate.op(),
                    ids[gate.id()],
                    gate.color(),
                    new ArrayList<>(gate.outputs()
                            .stream()
                            .map(i -> ids[i])
                            .toList()),
                    gate.point()
            ));
        }

        context.push(getName());
        colorize(context, instances);
        context.pop();

        Map<String, List<Gate>> a = ins.stream()
                .collect(Collectors.toMap(Vec::getName, p -> instances.stream()
                        .filter(i -> i.color().equals(p.getColor()))
                        .toList()));

        Map<String, List<Gate>> b = outs.stream()
                .collect(Collectors.toMap(Vec::getName, p -> instances.stream()
                        .filter(i -> i.color().equals(p.getColor()))
                        .toList()));

        return new Circuit(a, b, instances);
    }
}
