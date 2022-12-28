package cg.common;

import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.util.*;
import java.util.stream.Collectors;

import static cg.common.Assert.assertThat;

/**
 * Represents a component that only connects existing components and does not add any functional gates itself.
 * Due to the addition of the {@link DynamicComponent} with more fine-grained control and the restriction of no functional gates
 * this component is only useful for a limited number of components.
 */
public abstract class ComplexComponent extends LogicComponent
{
    private record Dock(LogicComponent component, Vec vec, int offset)
    {
    }

    private record Connection(Dock start, Dock end, int amount)
    {
    }

    private final Map<String, LogicComponent> components = new HashMap<>();
    private final GateWay in;
    private final GateWay out;
    private final List<Connection> connections = new ArrayList<>();


    /**
     * @param name the name of the component
     * @param inputs the inputs
     * @param outputs the outputs
     */
    protected ComplexComponent(String name, List<Vec> inputs, List<Vec> outputs)
    {
        super(name);
        this.in = new GateWay("<", inputs);
        this.out = new GateWay(">", outputs);
        this.components.put("<", this.in);
        this.components.put(">", this.out);
    }

    /**
     * Allows to add a component to this component.
     * @param component the component to add
     */
    protected void add(LogicComponent component)
    {
        assertThat(!this.components.containsKey(component.getName()), "Component with name '" + component.getName() + "' already added!");
        this.components.put(component.getName(), component);
    }

    /**
     * Connects each gate of the specified output to the specified input. The length of the input / output need to be the same.
     * @param startName the name of the first component
     * @param startId the name of the output of that component
     * @param endName the name of the second component
     * @param endId the name of the input of that component
     */
    protected void connect(String startName, String startId, String endName, String endId)
    {
        LogicComponent sc = get(startName);
        LogicComponent ec = get(endName);
        int ss = sc.getOutput(startId).getSize();
        int es = ec.getInput(endId).getSize();
        assertThat(ss == es, "Mismatching vector widths " + startName + "." + startId + "(" + ss + ") <-> " + endName + "." + endId + "(" + es + ")");
        Dock s = new Dock(sc, sc.getOutput(startId), 0);
        Dock e = new Dock(ec, ec.getInput(endId), 0);
        this.connections.add(new Connection(s, e, ss));
    }

    /**
     * Connect a specified number of gates in a similar manner as the other connect method does.
     * @param startName the start component
     * @param startId the output name
     * @param endName the end component
     * @param endId the input name
     * @see #connect(String, String, String, String)
     */
    protected void connect(String startName, String startId, int startOffset, String endName, String endId, int endOffset, int amount)
    {
        LogicComponent sc = get(startName);
        LogicComponent ec = get(endName);
        Dock s = new Dock(sc, sc.getOutput(startId), startOffset);
        Dock e = new Dock(ec, ec.getInput(endId), endOffset);
        this.connections.add(new Connection(s, e, amount));
    }

    /**
     * Returns a component with a given name.
     * @param name the name
     * @return the component
     */
    protected LogicComponent get(String name)
    {
        assertThat(this.components.containsKey(name), "No component with name '" + name + "'!");
        return this.components.get(name);
    }

    /**
     * Returns the outputs that the component has.
     * @return the outputs
     */
    @Override
    public List<Vec> getOutputs()
    {
        return this.out.getOutputs();
    }

    /**
     * Returns the inputs that the component has.
     * @return the inputs
     */
    @Override
    public List<Vec> getInputs()
    {
        return this.in.getInputs();
    }

    /**
     * Synthesizes a new {@link Circuit}.
     * @param context the synthesis context
     * @return the circuit
     */
    @Override
    public Circuit synthesise(SynthesisContext context)
    {
        context.push(getName());
        // synthesize all the components
        Map<String, Circuit> circuits = this.components.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().synthesise(context)));

        // collect all the components
        for (Connection connection : this.connections)
        {
            Dock start = connection.start;
            Dock end = connection.end;
            Circuit startCircuit = circuits.get(start.component.getName());
            Circuit endCircuit = circuits.get(end.component.getName());
            List<Gate> outputs = startCircuit.outputs().get(start.vec.getName());
            List<Gate> inputs = endCircuit.inputs().get(end.vec.getName());
            for (int i = 0; i < connection.amount; i++)
            {
                outputs.get(start.offset + i).outputs().add(inputs.get(end.offset + i).id());
            }
        }

        // collect all the gates
        List<Gate> gates = new ArrayList<>(circuits.values()
                .stream()
                .map(Circuit::gates)
                .flatMap(List::stream)
                .toList());

        colorize(context, gates);

        // retrieve io gates
        Map<String, List<Gate>> inputs = circuits.get("<").outputs();
        Map<String, List<Gate>> outputs = circuits.get(">").inputs();

        Circuit circuit = new Circuit(
                inputs,
                outputs,
                gates
        );
        layout(circuit, circuits);
        context.pop();
        return circuit;
    }

    /**
     * A callback to position the components.
     * @param circuit the synthesized circuit
     * @param circuits the contained circuits
     */
    protected abstract void layout(Circuit circuit, Map<String, Circuit> circuits);
}
