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
        int optimize = 0;
        boolean changed;
        do
        {

            changed = false;

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
                    optimize++;
                    changed = true;
                }
            }
        }
        while (changed);

        gates.removeIf(g -> removed.contains(g.id()));
    }
}
