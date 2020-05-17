package org.evosuite.ga.metaheuristics;

import org.evosuite.ga.Chromosome;
import org.evosuite.Properties;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdaptiveListener implements SearchListener {

    private final static Logger logger = LoggerFactory.getLogger(AdaptiveListener.class);
    private int fitnessEvaluations = 0;
    private int iterations = 0;
    private int modificationsPerIteration = 0;
    private int improvementPerIteration = 0;

    boolean logOnePerInstance = true;

    @Override
    public void searchStarted(GeneticAlgorithm<?> algorithm) {
        if(logOnePerInstance) {
            logger.debug("+++++++++searchStarted+++++++++");
            logOnePerInstance = false;
        }
    }

    @Override
    public void iteration(GeneticAlgorithm<?> algorithm) {
        // Properties.MUTATION_RATE -= 0.0001;
        if((iterations + 1) % 100 == 0) {
            LoggingUtils.getEvoLogger().info("\nMutation Rate: " + Double.toString(Properties.MUTATION_RATE));
        }

        // More than half the mutated elements improved
        if(improvementPerIteration * 2 > modificationsPerIteration) {
            Properties.MUTATION_RATE = Math.min(Properties.MUTATION_RATE + 0.001, 1);
        } else {
            Properties.MUTATION_RATE -= Math.max(Properties.MUTATION_RATE - 0.001, 0);
        }

        iterations++;
        improvementPerIteration = 0;
        modificationsPerIteration = 0;
    }

    @Override
    public void searchFinished(GeneticAlgorithm<?> algorithm) {
        LoggingUtils.getEvoLogger().info("\n# interations: " + Integer.toString(iterations));
        LoggingUtils.getEvoLogger().info("# fitness evaluations: " + Integer.toString(fitnessEvaluations));
    }

    @Override
    public void fitnessEvaluation(Chromosome individual) {
        fitnessEvaluations++;
    }

    @Override
    public void modification(Chromosome individual) {
        modificationsPerIteration++;
        double fitness = 0;
        double previousFitness = 0;

        for(double f : individual.getFitnessValues().values()) {
            fitness += f;
        }
        for(double f : individual.getPreviousFitnessValues().values()) {
            previousFitness += f;
        }

        if(fitness < previousFitness) {
            improvementPerIteration++;
        }
    }
}
