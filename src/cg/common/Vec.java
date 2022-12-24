package cg.common;

public class Vec
{
    private final String name;
    private final int size;

    public Vec(String name, int size)
    {
        this.name = name;
        this.size = size;
    }

    public String getName()
    {
        return this.name;
    }

    public int getSize()
    {
        return this.size;
    }

    @Override
    public String toString()
    {
        return "Vec{" +
                "name='" + name + '\'' +
                ", size=" + size +
                '}';
    }
}
