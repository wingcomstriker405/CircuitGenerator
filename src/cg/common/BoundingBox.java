package cg.common;

import java.util.List;

public record BoundingBox(Point min, Point max)
{
    public static BoundingBox of(List<Gate> gates)
    {
        return gates.stream()
                .map(Gate::point)
                .map(BoundingBox::of)
                .reduce(BoundingBox::merge)
                .orElseThrow();
    }

    public static BoundingBox of(Point point)
    {
        return new BoundingBox(point, point);
    }

    public static BoundingBox merge(BoundingBox b1, BoundingBox b2)
    {
        return new BoundingBox(
                new Point(
                        Math.min(b1.min.x(), b2.min.x()),
                        Math.min(b1.min.y(), b2.min.y()),
                        Math.min(b1.min.z(), b2.min.z())
                ),
                new Point(
                        Math.max(b1.max.x(), b2.max.x()),
                        Math.max(b1.max.y(), b2.max.y()),
                        Math.max(b1.max.z(), b2.max.z())
                )
        );
    }
}
