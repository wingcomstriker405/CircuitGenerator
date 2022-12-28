package cg.common;

import cg.synthesis.Circuit;
import cg.synthesis.SynthesisContext;

import java.util.List;

/**
 * Represents component in the circuit.
 * @see ComplexComponent
 * @see BluePrintComponent
 * @see DynamicComponent
 */
public abstract class LogicComponent
{
    private final String name;

    /**
     * @param name the name of the component
     */
    protected LogicComponent(String name)
    {
        this.name = name;
    }

    /**
     * Returns the name of the component
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Returns the outputs that the component offers.
     * @return the outputs
     */
    public abstract List<Vec> getOutputs();

    /**
     * Returns the inputs that the component offers.
     * @return the inputs
     */
    public abstract List<Vec> getInputs();

    /**
     * Finds a {@link Vec} with a given name in the outputs.
     * @param name name of the {@link Vec}
     */
    public Vec getOutput(String name)
    {
        return find(name, getOutputs());
    }

    /**
     * Finds a {@link Vec} with a given name in the inputs.
     * @param name name of the {@link Vec}
     */
    public Vec getInput(String name)
    {
        return find(name, getInputs());
    }

    /**
     * Finds a {@link Vec} with a given name in the provided list.
     * @param name name of the {@link Vec}
     * @param vecs list to search in
     */
    protected Vec find(String name, List<Vec> vecs)
    {
        return vecs
                .stream()
                .filter(v -> v.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(name + " <-> " + vecs));
    }

    /**
     * Changes the color of the gates which have the color 000000 if the current component has a color associated with it.
     * @param context the synthesis context
     * @param gates the gates apply the color
     */
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

    /**
     * Method to generate a {@link Circuit} out of the component.
     * @param context the synthesis context
     * @return the generated circuit
     */
    public abstract Circuit synthesise(SynthesisContext context);
}
