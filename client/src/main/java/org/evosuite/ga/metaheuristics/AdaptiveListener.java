package org.evosuite.ga.metaheuristics;

import org.evosuite.ga.Chromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdaptiveListener implements SearchListener {

    private final static Logger logger = LoggerFactory.getLogger(AdaptiveListener.class);
    private int fitnessEvaluations = 0;
    private int iterations = 0;

    @Override
    public void searchStarted(GeneticAlgorithm<?> algorithm) {
        logger.info("searchStarted+++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    @Override
    public void iteration(GeneticAlgorithm<?> algorithm) {
        iterations++;
        logger.info("iteration+++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    @Override
    public void searchFinished(GeneticAlgorithm<?> algorithm) {
        logger.debug("searchFinished+++++++++++++++++++++++++++++++++++++++++++++++++++");
        logger.info("interations: " + Integer.toString(iterations));
        logger.info("fitness evaluations: " + Integer.toString(fitnessEvaluations));
    }

    @Override
    public void fitnessEvaluation(Chromosome individual) {
        fitnessEvaluations++;
        logger.info("fitnessEvaluation+++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    @Override
    public void modification(Chromosome individual) {
        logger.info("searchStarted+++++++++++++++++++++++++++++++++++++++++++++++++++");
    }
}
