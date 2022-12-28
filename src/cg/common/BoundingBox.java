package cg.common;

import java.util.List;

/**
 * Represents the bounding box of an object.
 */
public record BoundingBox(Point min, Point max)
{
    /**
     * Creates a bounding box that contains all the gates.
     * @param gates the gates
     * @return the bounding box
     */
    public static BoundingBox of(List<Gate> gates)
    {
        return gates.stream()
                .map(Gate::point)
                .map(BoundingBox::of)
                .reduce(BoundingBox::merge)
                .orElseThrow();
    }

    /**
     * Creates a bounding box for one point.
     * @param point the point
     * @return the bounding box
     */
    public static BoundingBox of(Point point)
    {
        return new BoundingBox(point, point);
    }

    /**
     * Merges two bounding boxes. The resulting box will contain both bounding boxes.
     * @param b1 the first bounding box
     * @param b2 the second bounding box
     * @return the merged bounding box
     */
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
