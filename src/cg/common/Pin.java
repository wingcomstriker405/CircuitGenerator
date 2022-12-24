package cg.common;

public class Pin extends Vec
{
    private final String color;

    public Pin(String name, String color)
    {
        super(name, 1);
        this.color = color;
    }

    public String getColor()
    {
        return this.color;
    }

    @Override
    public String toString()
    {
        return "Pin{" +
                "name='" + getName() + '\'' +
                ", size=" + getSize() +
                ", color='" + color + '\'' +
                '}';
    }
}
