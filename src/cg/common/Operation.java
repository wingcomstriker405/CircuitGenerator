package cg.common;

/**
 * Represents the mode of a gate.
 */
public enum Operation
{
    AND(0),
    OR(1),
    XOR(2),
    NAND(3),
    NOR(4),
    XNOR(5),
    ;

    public final int id;

    Operation(int id)
    {
        this.id = id;
    }

    public static Operation ofMode(int mode)
    {
        return Operation.values()[mode];
    }
}
