package cg.synthesis;

import cg.common.Gate;

import java.util.List;
import java.util.Map;

public record Circuit(Map<String, List<Gate>> inputs, Map<String, List<Gate>> outputs, List<Gate> gates)
{
    public void move(int dx, int dy, int dz)
    {
        this.gates.forEach(g -> g.move(dx, dy, dz));
    }
}
