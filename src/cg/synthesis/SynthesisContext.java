package cg.synthesis;

import cg.common.BluePrint;
import cg.common.BlueprintParser;
import cg.common.Gate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynthesisContext
{
    private Counter counter;
    private final Map<String, BluePrint> cache = new HashMap<>();
    private final List<String> names = new ArrayList<>();
    private final Map<String, String> colorings;
    public SynthesisContext(Map<String, String> colorings)
    {
        this.counter = new Counter();
        this.colorings = colorings;
    }

    public void push(String name)
    {
        this.names.add(name);
    }

    public void pop()
    {
        this.names.remove(this.names.size() - 1);
    }

    public String getColor()
    {
        String key = String.join("-", this.names);
        return this.colorings.entrySet()
                .stream()
                .filter(e -> key.matches(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("000000");
    }

    public int next()
    {
        return this.counter.next();
    }

    public BluePrint load(String path)
    {
        if (this.cache.containsKey(path))
            return this.cache.get(path);
        BluePrint normalize = normalize(BlueprintParser.parse(path));
        this.cache.put(path, normalize);
        return normalize;
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
