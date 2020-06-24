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

    public static double MIN_MUTATION_RATE = 0.01;
    public static double MAX_MUTATION_RATE = 0.5;
    public static double MUTATION_INCREMENT = 0.001;
    public static double MIN_CROSSOVER_RATE = 0.5;
    public static double MAX_CROSSOVER_RATE = 0.99;
    public static double CROSSOVER_INCREMENT = 0.001;
    public static double MAX_SCORE_INCREMENT_PER_INDIVIDUAL = 1000;

    boolean logOnePerInstance = true;
    private final boolean DEBUG = false;

    @Override
    public void searchStarted(GeneticAlgorithm<?> algorithm) {
        if(logOnePerInstance && DEBUG) {
            logger.debug("+++++++++searchStarted+++++++++");
            logOnePerInstance = false;
        }
    }

    @Override
    public void iteration(GeneticAlgorithm<?> algorithm) {
        boolean print = DEBUG && (iterations + 1) % 100 == 0;

        int mutationIterationScore = 0;
        int crossOverIterationScore = 0;
        double ftns, parentFtns, increment;
        int mutations = 0,
            crossovers = 0;
        for (Chromosome individual: algorithm.population) {
            ftns = individual.getFitness();
            parentFtns = individual.parentFitness;
            if(individual.didMutate) {
                increment = Math.min(parentFtns - ftns, MAX_SCORE_INCREMENT_PER_INDIVIDUAL);
                mutationIterationScore += increment;
                mutations++;
            }
            if(individual.didCrossOver) {
                increment = Math.min(parentFtns - ftns, MAX_SCORE_INCREMENT_PER_INDIVIDUAL);
                crossOverIterationScore += increment;
                crossovers++;
            }
        }

        if(print) {
            LoggingUtils.getEvoLogger().info("\rMutations: " + Integer.toString(mutations) + " Mutation Score: " + Integer.toString(mutationIterationScore) + " | Crossovers: " + Integer.toString(crossovers) + " CrossOver Score: " + Integer.toString(crossOverIterationScore));
        }

        // Normalize Scores
        if(mutations > 0 && Double.isFinite(mutationIterationScore)) {
            mutationIterationScore = mutationIterationScore / mutations;
        }
        if(crossovers > 0 && Double.isFinite(crossOverIterationScore)) {
            crossOverIterationScore = crossOverIterationScore / crossovers;
        }

        // Increase Operators that caused net-improvement
        if(mutationIterationScore > 0) {
            Properties.MUTATION_RATE = Math.min(Math.max(Properties.MUTATION_RATE + MUTATION_INCREMENT, MIN_MUTATION_RATE), MAX_MUTATION_RATE);
        }
        if(crossOverIterationScore > 0) {
            Properties.CROSSOVER_RATE = Math.min(Math.max(Properties.CROSSOVER_RATE + CROSSOVER_INCREMENT, MIN_CROSSOVER_RATE), MAX_CROSSOVER_RATE);
        }

        if(print) {
            LoggingUtils.getEvoLogger().info("Mutation Rate: " + Double.toString(Properties.MUTATION_RATE) + " | CrossOver Rate: " + Double.toString(Properties.CROSSOVER_RATE));
        }

        iterations++;
        improvementPerIteration = 0;
        modificationsPerIteration = 0;
    }

    @Override
    public void searchFinished(GeneticAlgorithm<?> algorithm) {
        if(DEBUG) {
            LoggingUtils.getEvoLogger().info("\n# interations: " + Integer.toString(iterations));
            LoggingUtils.getEvoLogger().info("# fitness evaluations: " + Integer.toString(fitnessEvaluations));
        }
    }

    @Override
    public void fitnessEvaluation(Chromosome individual) {
        fitnessEvaluations++;
    }

    @Override
    public void modification(Chromosome individual) {

    }
}
