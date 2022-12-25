package cg.common;

import java.util.List;

public class Gate
{
    private Operation op;
    private int id;
    private String color;
    private List<Integer> outputs;
    private Point point;

    public Gate(Operation op, int id, String color, List<Integer> outputs)
    {
        this(op, id, color, outputs, new Point(0, 0, 0));
    }

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

    public void move(int dx, int dy, int dz)
    {
        point(point().move(dx, dy, dz));
    }
}
