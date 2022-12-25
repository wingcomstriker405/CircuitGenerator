package cg.common;

public record Point(int x, int y, int z)
{
    public Point move(int dx, int dy, int dz)
    {
        return new Point(this.x + dx, this.y + dy, this.z + dz);
    }
}
