/*
 * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2018 Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudsimplus.heuristics;

import org.cloudbus.cloudsim.distributions.ContinuousDistribution;

/**
 * A base class for implementation of
 * <a href="http://en.wikipedia.org/wiki/Simulated_annealing">Hill Climbing / Gravity Algorithm</a>
 * algorithms used to find a suboptimal solution for a problem defined by sub-classes of this one.
 *
 * Gravity is a heuristic that starts with a random solution
 * and iteratively generates a random neighbor solution that its fitness
 * is assessed in order to reach a sub-optimal result.
 * The algorithm tries to avoid local minima by using a repeated number of iterations.
 *
 * <p>The algorithm basically works as follows:
 * <ol>
 *  <li>Starts generating a random solution as you wish;</li>
 *  <li>Computes its fitness using some function (defined by the developer implementing the heuristic);</li>
 *  <li>Generates a neighbor random solution from the current solution and computes its fitness;</li>
 *  <li>Assesses the neighbor and current solution (the conditions below are ensured by the {@link #getAcceptanceProbability()} method):
 *      <ul>
 *          <li>{@code if neighbor.getFitness() > current.getFitness()} then move to the new solution;</li>
 *          <li>{@code if neighbor.getFitness() < current.getFitness()} does not move to the new solution;</li>
 *      </ul>
 *  </li>
 *  <li>Repeat steps 3 to 4 until an acceptable solution is found or some number
 * of iterations or time is reached. These conditions are defined by the developer
 * implementing the heuristic.</li>
 * </ol>
 * </p>
 *
 * @param <S> the class of solutions the heuristic will deal with, starting with a random solution
 *           and execute the solution search in order to achieve a satisfying solution (defined by a stop criteria)
 * @author Manoel Campos da Silva Filho
 * @see <a href="https://doi.org/10.1109/101.17235">[1] R. A. Rutenbar,
 * “Simulated Annealing Algorithms: An overview,” IEEE Circuits Devices Mag., vol. 1, no. 5,
 * pp. 19–26, 1989.</a>
 * @since CloudSim Plus 1.0
 */


public abstract class Gravity<S extends HeuristicSolution<?>> extends HeuristicAbstract<S> {
    /**
     * @see #getMaxIter() ()
     */
    private double max_num_iterations;

    /**
     * @see #getCurrentIter() ()
     */
    private double current_iteration;

    /**
     * Instantiates a gravity heuristic.
     *
     * @param random a pseudo random number generator
     * @param solutionClass reference to the generic class that will be used to instantiate heuristic solutions
     */
    Gravity(final ContinuousDistribution random, final Class<S> solutionClass){
        super(random, solutionClass);
    }

    @Override
    public double getAcceptanceProbability() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @return true if the maximum number of iterations have been reached and solution search can be
     * stopped, false otherwise
     */
    @Override
    public boolean isToStopSearch() {
        return current_iteration <= max_num_iterations;
    }

    /**
     * {@inheritDoc}
     *
     * Updates system to go to next iteration
     * @see #getCurrentIter()
     */
    @Override
    public void updateSystemState() {
        current_iteration+=1;
        LOGGER.debug(
            "{}: Best solution cost so far is {}, current system temperature is {}",
            System.currentTimeMillis(), getBestSolutionSoFar().getCost(), getCurrentIter());
    }

    /**
     * Gets the current iteration at the time of the method call
     *
     * @return the current iteration
     */
    public double getCurrentIter() {
        return current_iteration;
    }

    /**
     * Sets the current iteration
     * @param current_iteration the current iteration
     */
    protected void setCurrentIter(final double current_iteration) {
        this.current_iteration=current_iteration;
    }

    /**
     *
     * @return the maximum number of hill climbing iterations
     */
    public double getMaxIter() {
        return max_num_iterations;
    }

    /**
     * Sets the maximum number of hill climbing iterations after which the best solution should be considered
     *
     * @param max_num_iterations the maximum number of iterations
     */
    public void setMaxIter(final double max_num_iterations) {
        this.max_num_iterations = max_num_iterations;
    }

}
