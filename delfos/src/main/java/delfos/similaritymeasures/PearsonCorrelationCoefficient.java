package delfos.similaritymeasures;

import java.util.List;
import java.util.ListIterator;
import delfos.common.exceptions.CouldNotComputeSimilarity;

/**
 * Medida de similitud que utiliza el coeficiente de correlación de pearson para
 * comparar la relación de variación de dos vectores. El valor devuelto está en
 * el intervalo [0,1] ya que una relación de variación inversa perfecta
 * (coeficiente de pearson = -1) indicaría que son completamente distintos
 * (similitud = 0)
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class PearsonCorrelationCoefficient extends WeightedSimilarityMeasureAdapter {

    @Override
    public float similarity(List<Float> v1, List<Float> v2) throws CouldNotComputeSimilarity {
        double pcc = pearsonCorrelationCoefficient(v1, v2);

        return (float) pcc;
    }

    @Override
    public float weightedSimilarity(List<Float> v1, List<Float> v2, List<Float> weights) throws CouldNotComputeSimilarity {
        double pcc = pearsonCorrelationCoefficient_weighted(v1, v2, weights);

        return (float) pcc;
    }

    /**
     * Devuelve el PCC de las listas de valores. El valor está entre -1 y 1. Si
     * las listas están vacías, lanza una excepción
     * {@link CouldNotComputeSimilarity}.
     *
     * @param v1
     * @param v2
     * @return
     * @throws CouldNotComputeSimilarity Si las listas están vacías.
     */
    public double pearsonCorrelationCoefficient(List<? extends Number> v1, List<? extends Number> v2) throws CouldNotComputeSimilarity {

        validateInputParameters(v1, v2);

        double avg1 = 0;
        double avg2 = 0;

        ListIterator<? extends Number> i1 = v1.listIterator();
        ListIterator<? extends Number> i2 = v2.listIterator();
        do {

            double value1 = i1.next().doubleValue();
            double value2 = i2.next().doubleValue();

            avg1 += value1 / v1.size();
            avg2 += value2 / v2.size();
        } while (i1.hasNext());

        double numerador = 0;
        double denominador1 = 0;
        double denominador2 = 0;
        i1 = v1.listIterator();
        i2 = v2.listIterator();
        do {
            double value1 = i1.next().doubleValue();
            double value2 = i2.next().doubleValue();

            numerador += (value1 - avg1) * (value2 - avg2);
            denominador1 += (value1 - avg1) * (value1 - avg1);
            denominador2 += (value2 - avg2) * (value2 - avg2);
        } while (i1.hasNext());

        //Cálculo del denominador
        double denominador = Math.sqrt(denominador1 * denominador2);

        double ret = 0;
        if (denominador == 0) {
            throw new CouldNotComputeSimilarity("Cannot compute PCC between " + v1.toString() + " and " + v2.toString());
        } else {
            ret = numerador / denominador;
        }

        return ret;
    }

    public double weightedMean(List<? extends Number> v, List<? extends Number> weights) {
        double numerator = 0;
        double denominator = 0;

        for (int i = 0; i < v.size(); i++) {
            numerator += v.get(i).doubleValue() * weights.get(i).doubleValue();
            denominator += weights.get(i).doubleValue();
        }

        return numerator / denominator;
    }

    public double weightedCovariance(List<? extends Number> v1, List<? extends Number> v2, List<? extends Number> weights) {
        double weightedMean_v1 = weightedMean(v1, weights);
        double weightedMean_v2 = weightedMean(v2, weights);

        double numerator = 0;
        double denominator = 0;

        ListIterator<? extends Number> i1 = v1.listIterator(), i2 = v2.listIterator(), iw = weights.listIterator();

        do {
            double value1 = i1.next().doubleValue();
            double value2 = i2.next().doubleValue();
            double weight = iw.next().doubleValue();

            numerator += weight * (value1 - weightedMean_v1) * (value2 - weightedMean_v2);
            denominator += weight;
        } while (i1.hasNext());

        return numerator / denominator;
    }

    public double pearsonCorrelationCoefficient_weighted(List<? extends Number> v1, List<? extends Number> v2, List<? extends Number> weights) throws IllegalArgumentException, CouldNotComputeSimilarity {
        validateInputParameters(v1, v2, weights);

        double numerator = weightedCovariance(v1, v2, weights);

        double denominator1 = weightedCovariance(v1, v1, weights);
        double denominator2 = weightedCovariance(v2, v2, weights);

        double denominator = Math.sqrt(denominator1 * denominator2);

        if (denominator == 0) {
            throw new CouldNotComputeSimilarity("Cannot compute PCC between " + v1.toString() + " and " + v2.toString() + "with weigts " + weights.toString());
        }
        double pcc_weighted = numerator / denominator;

        return pcc_weighted;
    }

    private void validateInputParameters(List<? extends Number> v1, List<? extends Number> v2) throws IllegalArgumentException, CouldNotComputeSimilarity {
        if (v1 == null) {
            throw new IllegalArgumentException("The list v1 cannot be null.");
        }
        if (v2 == null) {
            throw new IllegalArgumentException("The list v2 cannot be null.");
        }
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("The lists have different size: " + v1.size() + " != " + v2.size());
        }
        if (v1.isEmpty()) {
            throw new CouldNotComputeSimilarity("The lists are empty, cannot compute pearson correlation coefficient.");
        }
    }

    private void validateInputParameters(List<? extends Number> v1, List<? extends Number> v2, List<? extends Number> weights) throws IllegalArgumentException, CouldNotComputeSimilarity {
        if (v1 == null) {
            throw new IllegalArgumentException("The list v1 cannot be null.");
        }
        if (v2 == null) {
            throw new IllegalArgumentException("The list v2 cannot be null.");
        }
        if (weights == null) {
            throw new IllegalArgumentException("The weights list cannot be null.");
        }
        if (v1.size() != v2.size()
                || v1.size() != weights.size()
                || v2.size() != weights.size()) {
            throw new IllegalArgumentException("The lists have different size: " + v1.size() + " != " + v2.size() + " != " + weights.size());
        }
        if (v1.isEmpty()) {
            throw new CouldNotComputeSimilarity("The lists are empty, cannot compute pearson correlation coefficient.");
        }
    }
}