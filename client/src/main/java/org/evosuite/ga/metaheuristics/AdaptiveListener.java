package org.evosuite.ga.metaheuristics;

import org.evosuite.ga.Chromosome;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdaptiveListener implements SearchListener {

    private final static Logger logger = LoggerFactory.getLogger(AdaptiveListener.class);
    private int fitnessEvaluations = 0;
    private int iterations = 0;

    @Override
    public void searchStarted(GeneticAlgorithm<?> algorithm) {
        LoggingUtils.getEvoLogger().info("searchStarted+++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    @Override
    public void iteration(GeneticAlgorithm<?> algorithm) {
        iterations++;
        LoggingUtils.getEvoLogger().info("iteration+++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    @Override
    public void searchFinished(GeneticAlgorithm<?> algorithm) {
        LoggingUtils.getEvoLogger().info("searchFinished+++++++++++++++++++++++++++++++++++++++++++++++++++");
        LoggingUtils.getEvoLogger().info("interations: " + Integer.toString(iterations));
        LoggingUtils.getEvoLogger().info("fitness evaluations: " + Integer.toString(fitnessEvaluations));
    }

    @Override
    public void fitnessEvaluation(Chromosome individual) {
        fitnessEvaluations++;
        LoggingUtils.getEvoLogger().info("fitnessEvaluation+++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    @Override
    public void modification(Chromosome individual) {
        LoggingUtils.getEvoLogger().info("searchStarted+++++++++++++++++++++++++++++++++++++++++++++++++++");
    }
}
