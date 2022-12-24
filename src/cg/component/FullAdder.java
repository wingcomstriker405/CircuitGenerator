package cg.component;

import cg.common.BluePrintComponent;
import cg.common.Pin;

import java.util.List;

public class FullAdder extends BluePrintComponent
{
    public FullAdder(String name)
    {
        super(
                name,
                "./data/Adder.json",
                List.of(
                        new Pin("a", "0A3EE2"),
                        new Pin("b", "D02525"),
                        new Pin("c", "E2DB13")
                ),
                List.of(
                        new Pin("c", "19E753"),
                        new Pin("v", "2CE6E6")
                )
        );
    }
}
