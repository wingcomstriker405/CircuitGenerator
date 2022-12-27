package cg.component;

import cg.common.*;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class ALU extends ComplexComponent
{
    private static final List<String> FUNCTIONS = List.of(
            "id",
            "add",
            "neg",
            "shr",
            "shl"
    );
    public static final int FUNCTION_BITS = calcFunctionBits();

    private static int calcFunctionBits()
    {
        int number = 1;
        while(1 << number < FUNCTIONS.size())
        {
            number++;
        }
        return number;
    }

    private final int size;
    private final List<LogicComponent> operations;
    public ALU(String name, int size)
    {
        super(
                name,
                List.of(
                        new Vec("a", size),
                        new Vec("b", size),
                        new Vec("f", FUNCTION_BITS)
                ),
                List.of(
                        new Vec("o", size)
                )
        );

        this.size = size;

        add(new Multiplexer("mux", size, FUNCTIONS.size(), FUNCTION_BITS));

        this.operations = List.of(
                new Id("id", size),
                new CarryLookAheadAdder("add", size),
                new Negator("neg", size),
                new Shift("shr", size, List.of(0, 1, 2, 4, 8)),
                new Shift("shl", size, List.of(-0, -1, -2, -4, -8))
        );

        for (LogicComponent operation : this.operations)
            add(operation);

        // connect mux to output
        connect("mux", "o", ">", "o");

        // connect function to mux
        connect("<", "f", "mux", "s");

        // connect inputs (if single input is required wire only a)
        for (int i = 0; i < FUNCTIONS.size(); i++)
        {
            LogicComponent component = this.operations.get(i);
            List<Vec> inputs = component.getInputs();
            if(inputs.size() == 1)
            {
                connect("<", "a", component.getName(), inputs.get(0).getName());
            }
            else
            {
                connect("<", "a", component.getName(), inputs.get(0).getName());
                connect("<", "b", component.getName(), inputs.get(1).getName());
            }
        }

        // connect input to adder
        connect("<", "a", "add", "a");
        connect("<", "b", "add", "b");

        // connect adder to mux
        for (int i = 0; i < this.operations.size(); i++)
        {
            LogicComponent component = this.operations.get(i);
            Vec vec = component.getOutputs().get(0);
            connect(component.getName(), vec.getName(), "mux", "i" + i + "_");
        }
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Circuit> circuits)
    {
        // calculate maximum depth
        List<BoundingBox> boxes = circuits.values()
                .stream()
                .map(c -> BoundingBox.of(c.gates()))
                .toList();
        int max = boxes.stream()
                .mapToInt(b -> b.max().y() - b.min().y())
                .max()
                .orElseThrow();

        // move inputs
        List<Gate> inA = circuit.inputs().get("a");
        List<Gate> inB = circuit.inputs().get("b");
        for (int i = 0; i < inA.size(); i++)
        {
            inA.get(i).move(i, 0, 0);
            inB.get(i).move(inA.size() + i, 0, 0);
        }

        // move function
        List<Gate> f = circuit.inputs().get("f");
        for (int i = 0; i < f.size(); i++)
        {
            f.get(i).move(inA.size() * 2 + i, 0, 0);
        }

        // move outputs
        List<Gate> o = circuit.outputs().get("o");
        for (int i = 0; i < o.size(); i++)
        {
            o.get(i).move(i, max + 7, 0);
        }

        // move functions
        int start = 0;
        for (String function : FUNCTIONS)
        {
            circuits.get(function).move(start, 1, 0);
            BoundingBox of = BoundingBox.of(circuits.get(function).gates());
            start += of.max().x() - of.min().x();
        }


        // move mux
        circuits.get("mux").move(0, max + 1, 0);
    }
}
