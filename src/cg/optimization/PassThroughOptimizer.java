package cg.optimization;

import cg.common.Gate;
import cg.common.Operation;

import java.util.*;

public class PassThroughOptimizer
{
    public static void check(Set<Integer> io, List<Gate> gates)
    {
        new PassThroughOptimizer(io, gates).optimize();
    }

    private record Entry(Gate g, Set<Integer> in, Set<Integer> out) { }

    private final Set<Integer> io;
    private final List<Gate> gates;
    private final Map<Integer, Entry> entries = new HashMap<>();
    private PassThroughOptimizer(Set<Integer> io, List<Gate> gates)
    {
        this.io = io;
        this.gates = gates;
        for (Gate gate : gates)
        {
            this.entries.put(gate.id(), new Entry(gate, new HashSet<>(), new HashSet<>(gate.outputs())));
        }

        for (Gate gate : gates)
        {
            for (Integer output : gate.outputs())
            {
                this.entries.get(output).in.add(gate.id());
            }
        }
    }

    private void optimize()
    {
        int counter = 0;
        int size;
        for (int i = 0; i < 10; i++)
        {
            do
            {
                size = this.entries.size();

                optimizePass();
                optimizeEnds();

                filterGates();
                counter++;
            }
            while (size != this.entries.size());
        }

        System.out.println("PASSES: " + counter);

        finish();
    }

    private void optimizePass()
    {
        Set<Operation> neutral = Set.of(
                Operation.AND,
                Operation.OR,
                Operation.XOR
        );

        for (Gate gate : this.gates)
        {
            if(this.entries.containsKey(gate.id()))
            {
                Entry e = this.entries.get(gate.id());
                if(neutral.contains(gate.op()) && e.in.size() == 1)
                {
                    if(isRemovable(gate.id()))
                    {
                        for (Integer a : e.in)
                        {
                            Entry p = this.entries.get(a);
                            p.out.addAll(e.out);
                            for (Integer integer : e.out)
                                this.entries.get(integer).in.add(p.g.id());
                            remove(gate.id());
                        }
                    }
                    else
                    {
                        if(e.out.size() == 2)
                            System.out.println("HERE");
                    }
                }
            }
        }
    }

    private Gate fused;

    private void optimizeEnds()
    {
        for (Gate gate : this.gates)
        {
            if(this.entries.containsKey(gate.id()))
            {
                Entry e = this.entries.get(gate.id());
                if(isRemovable(gate.id()))
                {
                    if(e.out.isEmpty())
                    {
                        remove(gate.id());
                    }
                    else if(e.in.isEmpty())
                    {
                        if(this.fused == null)
                        {
                            this.fused = e.g;
                        }
                        else if(e.g.id() != this.fused.id())
                        {
                            for (Integer output : e.out)
                                this.entries.get(output).in.add(this.fused.id());
                            this.fused.outputs().addAll(gate.outputs());
                            remove(gate.id());
                        }
                    }
                }
            }
        }
    }

    private boolean isRemovable(int id)
    {
        return !this.io.contains(id);
    }

    private void remove(int id)
    {
        Entry removed = this.entries.remove(id);
        for (Integer integer : removed.in)
        {
            this.entries.get(integer).out.remove(removed.g.id());
        }

        for (Integer integer : removed.out)
        {
            this.entries.get(integer).in.remove(removed.g.id());
        }
    }

    private void filterGates()
    {
        this.gates.removeIf(g -> !this.entries.containsKey(g.id()));
    }

    private void finish()
    {
        this.gates.forEach(g -> g.outputs(new ArrayList<>(this.entries.get(g.id()).out)));
    }
}
