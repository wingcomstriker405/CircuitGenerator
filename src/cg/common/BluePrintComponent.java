package cg.common;

import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a component that is provided by a blueprint file.
 * This serves for injecting hand build components into the generator.
 */
public abstract class BluePrintComponent extends LogicComponent
{
    private final String file;
    private final List<Pin> inputs;
    private final List<Pin> outputs;

    /**
     * @param name    the name of the component
     * @param file    the blueprint file
     * @param inputs  the input pins
     * @param outputs the output pins
     * @see Pin
     */
    protected BluePrintComponent(String name, String file, List<Pin> inputs, List<Pin> outputs)
    {
        super(name);
        this.file = file;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    /**
     * Returns the outputs that the blueprint has.
     *
     * @return the outputs
     */
    @Override
    public List<Vec> getOutputs()
    {
        return Collections.unmodifiableList(this.outputs);
    }

    /**
     * Returns the inputs that the blueprint has.
     *
     * @return the inputs
     */
    @Override
    public List<Vec> getInputs()
    {
        return Collections.unmodifiableList(this.inputs);
    }

    /**
     * Synthesizes a new instance of the blueprint.
     *
     * @return the outputs
     */
    @Override
    public Circuit synthesise(SynthesisContext context)
    {
        return instantiate(context, this.file, this.inputs, this.outputs);
    }

    private Circuit instantiate(SynthesisContext context, String path, List<Pin> ins, List<Pin> outs)
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
