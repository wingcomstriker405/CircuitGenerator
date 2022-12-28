package cg.common;

/**
 * Represents a position in 3D space.
 */
public record Point(int x, int y, int z)
{
    /**
     * Moves the point by the specified amount.
     * @param dx the amount in x direction
     * @param dy the amount in y direction
     * @param dz the amount in z direction
     * @return a new point moved by the amount
     */
    public Point move(int dx, int dy, int dz)
    {
        return new Point(this.x + dx, this.y + dy, this.z + dz);
    }
}
