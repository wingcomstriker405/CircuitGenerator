package cg.common;

import java.util.List;

public class GateNormalizer
{
    public static void normalize(List<Gate> gates)
    {
        BoundingBox box = BoundingBox.of(gates);
        int dx = -box.min().x();
        int dy = -box.min().y();
        int dz = -box.min().z();
        gates.forEach(g -> g.move(dx, dy, dz));
    }
}
