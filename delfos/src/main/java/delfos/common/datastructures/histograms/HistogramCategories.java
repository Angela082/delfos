package delfos.common.datastructures.histograms;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.parameters.ParameterOwner;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 9-sept-2014
 */
public class HistogramCategories<E> {

    Map<E, Integer> histogram = new TreeMap<>();

    private int numValuesAdded = 0;

    public HistogramCategories() {
    }

    public synchronized void addValue(E value) {
        if (value == null) {
            throw new IllegalStateException("Null Value added to HistogramCategories.");
        } else {
            if (!histogram.containsKey(value)) {
                histogram.put(value, 1);
            } else {
                Integer frequency = histogram.get(value);
                frequency++;
                histogram.put(value, frequency);
            }
        }
        numValuesAdded++;
    }

    public void printHistogram(PrintStream stream) {

        stream.println("Histogram:");
        for (Map.Entry<E, Integer> entry : histogram.entrySet()) {
            E key = entry.getKey();

            String keyString;
            if (key instanceof ParameterOwner) {
                ParameterOwner parameterOwner = (ParameterOwner) key;
                keyString = parameterOwner.getAlias();
            } else {
                keyString = key.toString();
            }
            Integer frequency = entry.getValue();
            stream.println(keyString + " --> " + frequency);
        }
    }

    public void printHistogram(Writer stream) throws IOException {
        stream.write("Histogram:\n");
        for (Map.Entry<E, Integer> entry : histogram.entrySet()) {
            E key = entry.getKey();
            Integer frequency = entry.getValue();
            String keyString;
            if (key instanceof ParameterOwner) {
                ParameterOwner parameterOwner = (ParameterOwner) key;
                keyString = parameterOwner.getAlias();
            } else {
                keyString = key.toString();
            }
            stream.write(keyString + " --> " + frequency + "\n");
        }
    }

    public int getNumValues() {
        return numValuesAdded;
    }

    public synchronized int getNumBins() {
        return histogram.size();
    }
}
