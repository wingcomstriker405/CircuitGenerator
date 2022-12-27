package cg.common;

import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.util.*;
import java.util.stream.Collectors;

import static cg.common.Assert.assertThat;

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


    protected ComplexComponent(String name, List<Vec> inputs, List<Vec> outputs)
    {
        super(name);
        this.in = new GateWay("<", inputs);
        this.out = new GateWay(">", outputs);
        this.components.put("<", this.in);
        this.components.put(">", this.out);
    }

    protected void add(LogicComponent component)
    {
        assertThat(!this.components.containsKey(component.getName()), "Component with name '" + component.getName() + "' already added!");
        this.components.put(component.getName(), component);
    }

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

    protected void connect(String startName, String startId, int startOffset, String endName, String endId, int endOffset, int amount)
    {
        LogicComponent sc = get(startName);
        LogicComponent ec = get(endName);
        Dock s = new Dock(sc, sc.getOutput(startId), startOffset);
        Dock e = new Dock(ec, ec.getInput(endId), endOffset);
        this.connections.add(new Connection(s, e, amount));
    }

    protected LogicComponent get(String name)
    {
        assertThat(this.components.containsKey(name), "No component with name '" + name + "'!");
        return this.components.get(name);
    }

    @Override
    public List<Vec> getOutputs()
    {
        return this.out.getOutputs();
    }

    @Override
    public List<Vec> getInputs()
    {
        return this.in.getInputs();
    }

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

    protected abstract void layout(Circuit circuit, Map<String, Circuit> circuits);
}
