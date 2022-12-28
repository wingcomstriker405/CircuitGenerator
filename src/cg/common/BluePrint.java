package cg.common;

import java.util.List;

/**
 * Represents a blueprint used by the {@link BluePrintComponent}.
 */
public record BluePrint(List<Gate> gates)
{
    /**
     * Normalizes the blueprint. The origin of the blueprint will be at (0, 0, 0) to make positioning easier.
     */
    public void normalize()
    {
        GateNormalizer.normalize(this.gates);
    }
}
