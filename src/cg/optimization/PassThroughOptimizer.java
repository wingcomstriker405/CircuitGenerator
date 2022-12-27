package cg.optimization;

import cg.common.Gate;
import cg.common.Operation;

import java.util.*;

public class PassThroughOptimizer
{
    public static void check(Set<Integer> io, List<Gate> gates)
    {
        gates.sort(Comparator.comparingInt(Gate::id));

        List<List<Gate>> in = new ArrayList<>();
        List<List<Gate>> out = new ArrayList<>();

        // prepare the input lists
        for (int i = 0; i < gates.size(); i++)
        {
            in.add(new ArrayList<>());
            out.add(new ArrayList<>());
        }

        // compute the inputs of the gates
        for (Gate g : gates)
        {
            List<Integer> outputs = g.outputs();
            for (Integer output : outputs)
            {
                in.get(gates.get(output).id()).add(g);
            }
            out.get(g.id()).addAll(outputs.stream().map(gates::get).toList());
        }

        Set<Operation> neutral = Set.of(
                Operation.AND,
                Operation.OR,
                Operation.XOR
        );

        Set<Integer> removed = new HashSet<>();
        boolean changed;
        do
        {
            changed = false;


            List<Integer> ends = new ArrayList<>();
            for (Gate gate : gates)
            {
                boolean inEmpty = in.get(gate.id()).isEmpty();
                boolean outEmpty = out.get(gate.id()).isEmpty();
                if(!io.contains(gate.id()) && !removed.contains(gate.id()))
                {
                    if(outEmpty)
                    {
                        for (Gate gate1 : in.get(gate.id()))
                        {
                            gate1.outputs().removeIf(i -> i == gate.id());
                            out.get(gate1.id()).removeIf(g -> g.id() == gate.id());
                        }
                        removed.add(gate.id());
                    }
                    else if(inEmpty)
                    {
                        ends.add(gate.id());
                    }
                }
            }

            if(ends.size() > 1)
            {
                Gate first = gates.get(ends.get(0));
                for (int i = 1; i < ends.size(); i++)
                {
                    Gate current = gates.get(ends.get(i));
                    first.outputs().addAll(current.outputs());
                    out.get(first.id()).addAll(out.get(current.id()));
                    // remove the current gate from all the input lists
                    for (Integer output : current.outputs())
                    {
                        in.get(output).removeIf(g -> g.id() == current.id());
                        in.get(output).add(first);
                    }
                    removed.add(current.id());
                }
                changed = true;
            }

            for (int i = 0; i < gates.size(); i++)
            {
                Gate current = gates.get(i);
                if(removed.contains(current.id()) || io.contains(current.id())) continue;

                if (in.get(current.id()).size() == 1 && !out.get(current.id()).isEmpty() && neutral.contains(current.op()))
                {
                    // change the gate itself
                    Gate gate = in.get(current.id()).get(0);
                    // remove the current gate
                    gate.outputs().removeIf(k -> k == current.id());
                    // add the outputs
                    gate.outputs().addAll(current.outputs());

                    // update the in and out lists

                    // - remove the current gate
                    for (Integer output : current.outputs())
                        in.get(output).remove(current);
                    out.get(gate.id()).remove(current);

                    // - add the previous gate to the current outputs
                    for (Integer output : current.outputs())
                        in.get(output).add(gate);
                    out.get(gate.id()).addAll(current.outputs().stream().map(gates::get).toList());

                    removed.add(current.id());
                    changed = true;
                }
            }
        }
        while (changed);

        gates.removeIf(g -> removed.contains(g.id()));
    }
}
