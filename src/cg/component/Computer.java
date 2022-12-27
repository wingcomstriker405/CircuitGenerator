package cg.component;

import cg.common.*;
import cg.synthesis.Circuit;

import java.util.List;
import java.util.Map;

public class Computer extends DynamicComponent
{
    public Computer(String name, int size)
    {

        super(
                name,
                List.of(
                        new Vec("i", size),
                        new Vec("f", ALU.FUNCTION_BITS),
                        new Vec("wm", 1),
                        new Vec("wr", 1),
                        new Vec("aa", 3),
                        new Vec("ab", 3),
                        new Vec("ad", 3),
                        new Vec("fm", 1),
                        new Vec("fa", 1)
                ),
                List.of()
        );

        /*
        i - input value of the instruction
        f - function of the alu
        wm - write to memory
        wr - write to registers
        aa - register bank output a register
        ab - register bank output b register
        ad - register bank destination register
        fm - activate output of the memory
        fa - activate output of the alu
        */

        // add alu function, memory write, register write, reg bank addresses     as the interface
        int memoryBits = 5;
        int addressBits = 3;
        add(new ALU("alu", size));
        add(new RegisterBank("reg", size, 8, addressBits));
        add(new Memory("mem", size, 32, memoryBits));

        // add activation layer to memory and alu
        for (int i = 0; i < size; i++)
        {
            connect("fm", 0, "mem_o", i);
            connect("fa", 0, "alu_o", i);
        }

        for (int i = 0; i < size; i++)
        {
            connect("alu_o", i, "reg_i", i);
            connect("reg_a", i, "alu_a", i);
            connect("reg_b", i, "alu_b", i);
        }

        for (int i = 0; i < memoryBits; i++)
        {
            connect("reg_a", i, "mem_s", i);
        }

        // connect input to reg
        // connect reg to memory
        // connect memory to reg
        for (int i = 0; i < size; i++)
        {
            connect("reg_b", i, "mem_i", i);
            connect("mem_o", i, "reg_i", i);
            connect("i", i, "reg_i", i);
        }



        // wire interface
        connect("wm", 0, "mem_w", 0);
        connect("wr", 0, "reg_w", 0);

        for (int i = 0; i < addressBits; i++)
        {
            connect("aa", 0, "reg_aa", i);
            connect("ab", 0, "reg_ab", i);
            connect("ad", 0, "reg_ad", i);
        }
    }

    @Override
    protected void layout(Circuit circuit, Map<String, Gate> mapping, Map<String, Circuit> circuits)
    {
        List<String> inputs = List.of(
                "i",
                "f",
                "wm",
                "wr",
                "aa",
                "ab",
                "ad",
                "fm",
                "fa"
        );

        int offset = 0;
        for (String input : inputs)
        {
            for (Gate gate : circuit.inputs().get(input))
            {
                gate.move(offset++, 0, 0);
            }
        }

        circuits.get("reg").move(0, 1, 0);
        BoundingBox reg = BoundingBox.of(circuits.get("reg").gates());
        circuits.get("mem").move(reg.max().x() - reg.min().x(), 1, 0);
        BoundingBox mem = BoundingBox.of(circuits.get("mem").gates());
        int max = Math.max(reg.max().y(), mem.max().y());
        circuits.get("alu").move(0, max + 1, 0);

        // HACK: change the gate mode to or to combine all the values on the bus
        int size = circuits.get("reg").inputs().get("i").size();
        for (int i = 0; i < size; i++)
        {
            circuits.get("reg").inputs().get("i").get(i).op(Operation.OR);
        }
    }
}
