package cg.common;

/**
 * Extension of the {@link Vec} to associate a colored gate in a blueprint with the vector.
 */
public class Pin extends Vec
{
    private final String color;

    /**
     * @param name the name of the pin
     * @param color the color of the pin
     */
    public Pin(String name, String color)
    {
        super(name, 1);
        this.color = color;
    }

    /**
     * Returns the color of the pin.
     * @return the color
     */
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
