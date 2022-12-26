package cg.synthesis;

import cg.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SynthesisContext
{
    private Counter counter;
    private final Map<String, BluePrint> cache = new HashMap<>();

    public SynthesisContext()
    {
        this.counter = new Counter();
    }

    public int next()
    {
        return this.counter.next();
    }

    private BluePrint load(String path)
    {
        if (this.cache.containsKey(path))
            return this.cache.get(path);
        BluePrint normalize = normalize(BlueprintParser.parse(path));
        this.cache.put(path, normalize);
        return normalize;
    }

    public Circuit instantiate(String path, List<Pin> ins, List<Pin> outs)
    {
        BluePrint bp = load(path);
        int[] ids = IntStream.range(0, bp.gates().size())
                .map(i -> this.counter.next())
                .toArray();
        List<Gate> instances = new ArrayList<>();
        for (Gate gate : bp.gates())
        {
            instances.add(new Gate(
                    gate.op(),
                    ids[gate.id()],
                    gate.color(),
                    new ArrayList<>(gate.outputs()
                            .stream()
                            .map(i -> ids[i])
                            .toList()),
                    gate.point()
            ));
        }

        Map<String, List<Gate>> a = ins.stream()
                .collect(Collectors.toMap(Vec::getName, p -> instances.stream()
                        .filter(i -> i.color().equals(p.getColor()))
                        .toList()));

        Map<String, List<Gate>> b = outs.stream()
                .collect(Collectors.toMap(Vec::getName, p -> instances.stream()
                        .filter(i -> i.color().equals(p.getColor()))
                        .toList()));

        return new Circuit(a, b, instances);
    }

    private BluePrint normalize(BluePrint bp)
    {
        Map<Integer, Integer> mapping = new HashMap<>();
        for (Gate gate : bp.gates())
            mapping.put(gate.id(), mapping.size());
        List<Gate> normalized = new ArrayList<>();
        for (Gate gate : bp.gates())
        {
            normalized.add(new Gate(
                    gate.op(),
                    mapping.get(gate.id()),
                    gate.color(),
                    gate.outputs()
                            .stream()
                            .map(mapping::get)
                            .toList(),
                    gate.point()
            ));
        }
        return new BluePrint(normalized);
    }
}
