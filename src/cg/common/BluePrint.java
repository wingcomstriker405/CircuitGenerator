package cg.common;

import java.util.List;

public record BluePrint(List<Gate> gates)
{
    public void normalize()
    {
        GateNormalizer.normalize(this.gates);
    }
}
