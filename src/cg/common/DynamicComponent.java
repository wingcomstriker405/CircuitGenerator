package cg.common;

import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A component with fine-grained control over the connection between individual gates.
 * This component type is a hybrid between {@link ComplexComponent} and {@link BluePrintComponent}.
 */
public abstract class DynamicComponent extends LogicComponent
{

    private record Connection(String start, String end)
    {
    }

    private final GateWay in;
    private final GateWay out;
    private final List<Connection> connections = new ArrayList<>();
    private final Map<String, Operation> names = new HashMap<>();
    private final Map<String, LogicComponent> components = new HashMap<>();

    /**
     * @param name the name of the component
     * @param inputs the inputs
     * @param outputs the outputs
     */
    protected DynamicComponent(String name, List<Vec> inputs, List<Vec> outputs)
    {
        super(name);
        this.in = new GateWay("<", inputs);
        this.out = new GateWay(">", outputs);
    }

    /**
     * Adds a components to this component.
     * The inputs and outputs of that component will be exposed by adding the name of the component as prefix separated
     * by an underscore ('_') to the input and output names (e.g. comp_inp2).
     * @param component the component to add
     */
    protected void add(LogicComponent component)
    {
        this.components.put(component.getName(), component);
    }

    /**
     * Adds a new gate to this component
     * @param name the name of the gate
     * @param operation the operation of the gate
     * @see Operation
     */
    protected void add(String name, Operation operation)
    {
        this.names.put(name, operation);
    }

    /**
     * Adds a new gate to this component
     * @param name the name of the gate
     * @param index an index that is appended to the name
     * @param operation the operation of the gate
     * @see Operation
     */
    protected void add(String name, int index, Operation operation)
    {
        this.names.put(name + index, operation);
    }

    /**
     * Connects two gates.
     * @param start the source gate
     * @param end the destination gate
     */
    protected void connect(String start, String end)
    {
        this.connections.add(new Connection(start, end));
    }

    /**
     * Adds a components to this component
     * @param start the source gate
     * @param startIndex the appended index
     * @param end the destination gate
     * @param endIndex the appended index
     */
    protected void connect(String start, int startIndex, String end, int endIndex)
    {
        this.connections.add(new Connection(start + startIndex, end + endIndex));
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
     * Synthesizes an instance of the component. This generates the gates and applies all the connections.
     * @param context the synthesis context
     * @return the circuit
     */
    @Override
    public Circuit synthesise(SynthesisContext context)
    {
        context.push(getName());
        Map<String, Gate> collect = new HashMap<>();

        Map<String, List<Gate>> inputs = fromGateWay(context, this.in);
        Map<String, List<Gate>> outputs = fromGateWay(context, this.out);
        addAll(collect, this.in, inputs);
        addAll(collect, this.out, outputs);


        Map<String, Circuit> circuits = this.components.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().synthesise(context)));
        List<Gate> rest = new ArrayList<>();
        addAll(collect, circuits, rest);

        this.names.forEach((a, b) -> collect.put(a,
                new Gate(
                        b,
                        context.next(),
                        "000000",
                        new ArrayList<>()
                )));

        for (Connection connection : this.connections)
        {
            if(!collect.containsKey(connection.end))
                throw new RuntimeException("No gate with name " + connection.end);
            if(!collect.containsKey(connection.start))
                throw new RuntimeException("No gate with name " + connection.start);
            int id = collect.get(connection.end).id();
            collect.get(connection.start).outputs().add(id);
        }
        List<Gate> all = new ArrayList<>(collect.values());
        all.addAll(rest);
        colorize(context, all);
        Circuit circuit = new Circuit(inputs, outputs, all);
        layout(circuit, collect, circuits);
        context.pop();
        return circuit;
    }

    /**
     * A callback to position the components.
     * @param circuit the synthesized circuit
     * @param mapping the map of all available gates
     * @param circuits the contained circuits
     */
    protected abstract void layout(Circuit circuit, Map<String, Gate> mapping, Map<String, Circuit> circuits);

    private static Map<String, List<Gate>> fromGateWay(SynthesisContext context, GateWay way)
    {
        Map<String, List<Gate>> inputs = new HashMap<>();
        for (Vec output : way.getOutputs())
        {
            List<Gate> current = new ArrayList<>();
            for (int i = 0; i < output.getSize(); i++)
            {
                Gate g = new Gate(
                        Operation.AND,
                        context.next(),
                        "000000",
                        new ArrayList<>()
                );
                current.add(g);
            }
            inputs.put(output.getName(), current);
        }
        return inputs;
    }

    private static void addAll(Map<String, Gate> all, GateWay way, Map<String, List<Gate>> mapping)
    {
        way.getOutputs().forEach(n -> {
            List<Gate> gates = mapping.get(n.getName());
            for (int i = 0; i < gates.size(); i++)
            {
                all.put(n.getName() + i, gates.get(i));
            }
        });
    }

    private static void addAll(Map<String, Gate> all, Map<String, Circuit> components, List<Gate> rest)
    {

        components.forEach((a, b) -> {
            Set<Integer> ids = new HashSet<>();
            b.inputs().forEach((c, d) -> {
                for (int i = 0; i < d.size(); i++)
                {
                    all.put(a + "_" + c + i, d.get(i));
                    ids.add(d.get(i).id());
                }
            });
            b.outputs().forEach((c, d) -> {
                for (int i = 0; i < d.size(); i++)
                {
                    all.put(a + "_" + c + i, d.get(i));
                    ids.add(d.get(i).id());
                }
            });
            b.gates()
                    .stream()
                    .filter(g -> !ids.contains(g.id()))
                    .forEach(rest::add);
        });
    }
}
