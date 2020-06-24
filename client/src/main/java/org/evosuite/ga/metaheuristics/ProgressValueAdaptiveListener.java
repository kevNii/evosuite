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

    private final boolean DEBUG = false;

    @Override
    public void searchStarted(GeneticAlgorithm<?> algorithm) {
        Properties.MUTATION_RATE = INITIAL_MUTATION_RATE;
        Properties.CROSSOVER_RATE = INITIAL_CROSSOVER_RATE;
    }

    @Override
    public void iteration(GeneticAlgorithm<?> algorithm) {
        boolean print = DEBUG && (iterations + 1) % 100 == 0;

        // Since evosuite minimizes fitness, we need to adapt the Lin Algo to reflect that
        double crossoversDuringIteration = 0;
        double mutationProgressValue = 0;
        double crossOverProgressValue = 0;
        // best/worst instead of max/min (since max is confusing if we're minimizing)
        double f_best = Double.POSITIVE_INFINITY;
        double f_worst = 0;
        double f_avg = 0;
        double tmp;
        for (Chromosome individual: algorithm.population) {
            if(individual.getFitness() < f_best) f_best = individual.getFitness();
            if(individual.getFitness() > f_worst) f_worst = individual.getFitness();
            f_avg += individual.getFitness();
            if(individual.didMutate) {
                /* if(individual.parentFitness != individual.getPreviousFitness()) {
                    LoggingUtils.getEvoLogger().info("wtf is goin on: " + Double.toString(individual.parentFitness) + " != " + Double.toString(individual.getPreviousFitness()));
                } */
                tmp = individual.parentFitness - individual.getFitness();
                if(Double.isFinite(tmp)) {
                    mutationProgressValue += tmp;
                }
            }
            if(individual.didCrossOver) {
                // tmp = individual.parentFitnessSum - (individual.getFitness() + individual.sibling.getFitness());
                tmp = (individual.parentFitnessSum / 2) - individual.getFitness();
                if(Double.isFinite(tmp)) {
                    crossOverProgressValue += tmp;
                }
                // Counting one individual that has been crossed over as half a crossover operation as to not double the count
                crossoversDuringIteration += 0.5;
            }
        }

        f_avg = f_avg / algorithm.population.size();

        if(print) {
            LoggingUtils.getEvoLogger().info("\rMutation Score: " + Double.toString(mutationProgressValue) + " | CrossOver Score: " + Double.toString(crossOverProgressValue) + " | f_best: " + Double.toString(f_best) + " | f_avg: " + Double.toString(f_avg) + " | f_worst: " + Double.toString(f_worst));
        }

        if(mutationsDuringIteration > 0) {
            mutationProgressValue = mutationProgressValue / mutationsDuringIteration;
        }
        if(crossoversDuringIteration > 0) {
            crossOverProgressValue = crossOverProgressValue / crossoversDuringIteration;
        }

        if(print) {
            LoggingUtils.getEvoLogger().info("\rMutation PV: " + Double.toString(mutationProgressValue) + " | CrossOver PV: " + Double.toString(crossOverProgressValue));
        }

        if(mutationProgressValue > 0 && mutationProgressValue > crossOverProgressValue) {
            Properties.MUTATION_RATE = Math.min(Properties.MUTATION_RATE + getMutationIncrement(f_worst, f_avg, f_best), MAX_MUTATION_RATE);
            Properties.CROSSOVER_RATE = Math.max(Properties.CROSSOVER_RATE - getCrossoverIncrement(f_worst, f_avg, f_best), MIN_CROSSOVER_RATE);
        } else if(crossOverProgressValue > 0 && crossOverProgressValue > mutationProgressValue) {
            Properties.CROSSOVER_RATE = Math.min(Properties.CROSSOVER_RATE + getCrossoverIncrement(f_worst, f_avg, f_best), MAX_CROSSOVER_RATE);
            Properties.MUTATION_RATE = Math.max(Properties.MUTATION_RATE - getMutationIncrement(f_worst, f_avg, f_best), MIN_MUTATION_RATE);
        }

        if(print) {
            LoggingUtils.getEvoLogger().info("Mutation Rate: " + Double.toString(Properties.MUTATION_RATE) + " | CrossOver Rate: " + Double.toString(Properties.CROSSOVER_RATE));
        }

        iterations++;
        mutationsDuringIteration = 0;
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
        // only called during mutation
        mutationsDuringIteration++;
    }

    private double getMutationIncrement(double f_min, double f_avg, double f_max) {
        if(f_max > f_min && Double.isFinite(f_min) && Double.isFinite(f_avg) && Double.isFinite(f_max)) {
            return MUTATION_INCREMENT * ((f_max - f_avg) / (f_max - f_min));
        }
        return MUTATION_INCREMENT;
    }

    private double getCrossoverIncrement(double f_min, double f_avg, double f_max) {
        if(f_max > f_min && Double.isFinite(f_min) && Double.isFinite(f_avg) && Double.isFinite(f_max)) {
            return CROSSOVER_INCREMENT * ((f_max - f_avg) / (f_max - f_min));
        }
        return CROSSOVER_INCREMENT;
    }
}
