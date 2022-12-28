package cg.common;

import java.util.List;

/**
 * Utility class to move all gates in order to have the corner start in the origin.
 */
public class GateNormalizer
{
    private GateNormalizer()
    {
    }

    /**
     * Normalizes all gates to let the corner be located at (0, 0, 0).
     * @param gates the gates to normalize
     */
    public static void normalize(List<Gate> gates)
    {
        BoundingBox box = BoundingBox.of(gates);
        int dx = -box.min().x();
        int dy = -box.min().y();
        int dz = -box.min().z();
        gates.forEach(g -> g.move(dx, dy, dz));
    }
}
