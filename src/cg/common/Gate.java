package cg.common;

import java.util.List;

public class Gate
{
    private Operation op;
    private int id;
    private String color;
    private List<Integer> outputs;
    private Point point;

    /**
     * Alternative constructor that defaults the point to (0, 0, 0).
     * @param op the operation of the gate
     * @param id the unique id that identifies the gate
     * @param color the color as a six letter hex string
     * @param outputs the ids of all the gates the gate outputs to
     * @see Operation
     */
    public Gate(Operation op, int id, String color, List<Integer> outputs)
    {
        this(op, id, color, outputs, new Point(0, 0, 0));
    }

    /**
     * @param op the operation of the gate
     * @param id the unique id that identifies the gate
     * @param color the color as a six letter hex string
     * @param outputs the ids of all the gates the gate outputs to
     * @param point the position of the gate
     * @see Operation
     */
    public Gate(Operation op, int id, String color, List<Integer> outputs, Point point)
    {
        this.op = op;
        this.id = id;
        this.color = color;
        this.outputs = outputs;
        this.point = point;
    }


    public Operation op()
    {
        return this.op;
    }

    public void op(Operation op)
    {
        this.op = op;
    }
    public int id()
    {
        return this.id;
    }

    public void id(int id)
    {
        this.id = id;
    }
    public String color()
    {
        return this.color;
    }

    public void color(String color)
    {
        this.color = color;
    }
    public List<Integer> outputs()
    {
        return this.outputs;
    }

    public void outputs(List<Integer> outputs)
    {
        this.outputs = outputs;
    }
    public Point point()
    {
        return this.point;
    }

    public void point(Point point)
    {
        this.point = point;
    }



    /**
     * Generates the string representation of the gate for blueprint files.
     */
    public String generate()
    {
        return """
                {
                          "color": "COLOR",
                          "controller": {
                            "active": false,
                            "controllers": [
                              OUTPUTS
                            ],
                            "id": ID,
                            "joints": null,
                            "mode": MODE
                          },
                          "pos": {
                            "x": XXX,
                            "y": YYY,
                            "z": ZZZ
                          },
                          "shapeId": "9f0f56e8-2c31-4d83-996c-d00a9b296c3f",
                          "xaxis": 1,
                          "zaxis": -2
                        }
                """
                .replace("XXX", "" + this.point.x())
                .replace("YYY", "" + this.point.y())
                .replace("ZZZ", "" + this.point.z())
                .replace("ID", "" + this.id)
                .replace("MODE", "" + this.op.id)
                .replace("COLOR", this.color)
                .replace("OUTPUTS", String.join(", ", this.outputs.stream().map(i -> "{ \"id\": " + i + " }").toList()))
                ;
    }

    /**
     * Moves the gate by the specified amount.
     * @param dx the amount to move in the x direction
     * @param dy the amount to move in the y direction
     * @param dz the amount to move in the z direction
     */
    public void move(int dx, int dy, int dz)
    {
        point(point().move(dx, dy, dz));
    }

    @Override
    public String toString()
    {
        return "Gate{" +
                "point=" + point +
                '}';
    }
}
