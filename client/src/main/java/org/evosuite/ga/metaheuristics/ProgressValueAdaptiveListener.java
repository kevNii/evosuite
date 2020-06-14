package org.evosuite.ga.metaheuristics;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressValueAdaptiveListener implements SearchListener {

    private final static Logger logger = LoggerFactory.getLogger(ProgressValueAdaptiveListener.class);
    private int fitnessEvaluations = 0;
    private int iterations = 0;
    private int mutationsDuringIteration = 0;

    public static double INITIAL_MUTATION_RATE = 0.5;
    public static double INITIAL_CROSSOVER_RATE = 0.5;
    public static double MIN_MUTATION_RATE = 0.001;
    public static double MAX_MUTATION_RATE = 0.5;
    public static double MUTATION_INCREMENT = 0.01;
    public static double MIN_CROSSOVER_RATE = 0.001;
    public static double MAX_CROSSOVER_RATE = 0.99;
    public static double CROSSOVER_INCREMENT = 0.01;

    boolean logOnePerInstance = true;

    @Override
    public void searchStarted(GeneticAlgorithm<?> algorithm) {
        if(logOnePerInstance) {
            logger.debug("+++++++++searchStarted+++++++++");
            logOnePerInstance = false;
        }
        Properties.MUTATION_RATE = INITIAL_MUTATION_RATE;
        Properties.CROSSOVER_RATE = INITIAL_CROSSOVER_RATE;
    }

    @Override
    public void iteration(GeneticAlgorithm<?> algorithm) {
        boolean print = (iterations + 1) % 100 == 0;

        double crossoversDuringIteration = 0;
        double mutationProgressValue = 0;
        double crossOverProgressValue = 0;
        double f_max = 0;
        double f_min = Double.POSITIVE_INFINITY;
        double f_avg = 0;
        for (Chromosome individual: algorithm.population) {
            if(individual.getFitness() > f_max) f_max = individual.getFitness();
            if(individual.getFitness() < f_min) f_min = individual.getFitness();
            f_avg += individual.getFitness();
            if(individual.didMutate) {
                mutationProgressValue += individual.getFitness() - individual.getPreviousFitness();
            }
            if(individual.didCrossOver) {
                crossOverProgressValue += individual.getFitness() + individual.sibling.getFitness() - individual.parentFitnessSum;
                // Counting one individual that has been crossed over as half a crossover operation as to not double the count
                crossoversDuringIteration += 0.5;
            }
        }

        f_avg = f_avg / algorithm.population.size();

        if(print) {
            LoggingUtils.getEvoLogger().info("\rMutation Score: " + Double.toString(mutationProgressValue) + " | CrossOver Score: " + Double.toString(crossOverProgressValue));
        }

        mutationProgressValue = mutationProgressValue / mutationsDuringIteration;
        crossOverProgressValue = crossoversDuringIteration / crossoversDuringIteration;

        if(print) {
            LoggingUtils.getEvoLogger().info("\rMutation Progress Value: " + Double.toString(mutationProgressValue) + " | CrossOver Progress Value: " + Double.toString(crossOverProgressValue));
        }

        if(mutationProgressValue > crossOverProgressValue) {
            Properties.MUTATION_RATE = Math.min(Properties.MUTATION_RATE + getMutationIncrement(f_min, f_avg, f_max), MAX_MUTATION_RATE);
            Properties.CROSSOVER_RATE = Math.max(Properties.CROSSOVER_RATE - getCrossoverIncrement(f_min, f_avg, f_max), MIN_CROSSOVER_RATE);
        } else if(crossOverProgressValue > mutationProgressValue) {
            Properties.CROSSOVER_RATE = Math.min(Properties.CROSSOVER_RATE + getCrossoverIncrement(f_min, f_avg, f_max), MAX_CROSSOVER_RATE);
            Properties.MUTATION_RATE = Math.max(Properties.MUTATION_RATE - getMutationIncrement(f_min, f_avg, f_max), MIN_MUTATION_RATE);
        }

        if(print) {
            LoggingUtils.getEvoLogger().info("Mutation Rate: " + Double.toString(Properties.MUTATION_RATE) + " | CrossOver Rate: " + Double.toString(Properties.CROSSOVER_RATE));
        }

        iterations++;
        mutationsDuringIteration = 0;
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
        // only called during mutation
        mutationsDuringIteration++;
    }

    private double getMutationIncrement(double f_min, double f_avg, double f_max) {
        if(f_max > f_min) {
            return MUTATION_INCREMENT * ((f_max - f_avg) / (f_max - f_min));
        }
        return MUTATION_INCREMENT;
    }

    private double getCrossoverIncrement(double f_min, double f_avg, double f_max) {
        if(f_max > f_min) {
            return CROSSOVER_INCREMENT * ((f_max - f_avg) / (f_max - f_min));
        }
        return CROSSOVER_INCREMENT;
    }
}
