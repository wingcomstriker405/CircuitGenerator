package cg.common;

import java.util.List;

public record Gate(Operation op, int id, String color, List<Integer> outputs)
{
    public String generate(int x, int y, int z)
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
                .replace("XXX", "" + x)
                .replace("YYY", "" + y)
                .replace("ZZZ", "" + z)
                .replace("ID", "" + this.id)
                .replace("MODE", "" + this.op.id)
                .replace("COLOR", this.color)
                .replace("OUTPUTS", String.join(", ", this.outputs.stream().map(i -> "{ \"id\": " + i + " }").toList()))
                ;
    }
}
