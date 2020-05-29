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
    public static double MIN_CROSSOVER_RATE = 0.6;
    public static double MAX_CROSSOVER_RATE = 0.99;

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
        boolean print = (iterations + 1) % 100 == 0;

        int mutationIterationScore = 0;
        int crossOverIterationScore = 0;
        double ftns, prevFtns;
        for (Chromosome individual: algorithm.population) {
            ftns = individual.getFitness();
            prevFtns = individual.getPreviousFitness();
            if(ftns < prevFtns){
                // improvement
                if(individual.didMutate) {
                    mutationIterationScore++;
                }
                if(individual.didCrossOver) {
                    crossOverIterationScore++;
                }
            } else if(ftns > prevFtns) {
                // deterioration
                if(individual.didMutate) {
                    mutationIterationScore--;
                }
                if(individual.didCrossOver) {
                    crossOverIterationScore--;
                }
            }
        }

        if(print) {
            LoggingUtils.getEvoLogger().info("\rMutation Score: " + Integer.toString(mutationIterationScore) + " | CrossOver Score: " + Integer.toString(crossOverIterationScore));
        }

        crossOverIterationScore *= 2;

        // Increase Operator that improved more individuals and reduce the other
        if(mutationIterationScore > 0 && mutationIterationScore > crossOverIterationScore) {
            Properties.MUTATION_RATE = Math.min(Properties.MUTATION_RATE + 0.001, MAX_MUTATION_RATE);
            Properties.CROSSOVER_RATE = Math.max(Properties.CROSSOVER_RATE - 0.001, MIN_CROSSOVER_RATE);
        } else if (crossOverIterationScore > 0 && crossOverIterationScore > mutationIterationScore) {
            Properties.MUTATION_RATE = Math.max(Properties.MUTATION_RATE - 0.001, MIN_MUTATION_RATE);
            Properties.CROSSOVER_RATE = Math.min(Properties.CROSSOVER_RATE + 0.001, MAX_CROSSOVER_RATE);
        } else if(crossOverIterationScore < 0 && mutationIterationScore < 0) {
            // Both suck
            Properties.MUTATION_RATE = Math.max(Properties.MUTATION_RATE - 0.001, MIN_MUTATION_RATE);
            Properties.CROSSOVER_RATE = Math.max(Properties.CROSSOVER_RATE - 0.001, MIN_CROSSOVER_RATE);
        } else {
            // both at least kinda good
            Properties.MUTATION_RATE = Math.min(Properties.MUTATION_RATE + 0.001, MAX_MUTATION_RATE);
            Properties.CROSSOVER_RATE = Math.min(Properties.CROSSOVER_RATE + 0.001, MAX_CROSSOVER_RATE);
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
