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

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.distributions.ContinuousDistribution;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.List;

/**
 * A heuristic that uses <a href="http://en.wikipedia.org/wiki/Simulated_annealing">Simulated Annealing</a>
 * to find a sub-optimal mapping among a set of Vms and Hosts
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.0
 */
public class VmToHostMappingSimulatedAnnealing
    extends SimulatedAnnealing<VmToHostMappingSolution>
    implements VmToHostMappingHeuristic
{
    private VmToHostMappingSolution initialSolution;

    /** @see #getHostList() */
    private List<Host> hostList;

    /** @see #getVmList() */
    private List<Vm> vmList;

    /**
     * Creates a new Simulated Annealing Heuristic for solving Vms to Hosts mapping.
     *
     * @param initialTemperature the system initial temperature
     * @param random a random number generator
     * @see #setColdTemperature(double)
     * @see #setCoolingRate(double)
     */
    public VmToHostMappingSimulatedAnnealing(final double initialTemperature, final ContinuousDistribution random) {
        super(random, VmToHostMappingSolution.class);
        setCurrentTemperature(initialTemperature);
        initialSolution = new VmToHostMappingSolution(this);
    }

    private VmToHostMappingSolution generateRandomSolution() {
        final VmToHostMappingSolution solution = new VmToHostMappingSolution(this);
        vmList.forEach(cloudlet -> solution.bindVmToHost(cloudlet, getRandomHost()));
        return solution;
    }

    private boolean isReadToGenerateInitialSolution(){
        return !vmList.isEmpty() && !hostList.isEmpty();
    }

    private boolean isThereInitialSolution(){
        return !initialSolution.getResult().isEmpty();
    }

    @Override
    public VmToHostMappingSolution getInitialSolution() {
        if(!isThereInitialSolution() && isReadToGenerateInitialSolution()) {
            initialSolution = generateRandomSolution();
        }

        return initialSolution;
    }

    @Override
    public List<Host> getHostList() {
        return hostList;
    }

    @Override
    public void setHostList(List<Host> hostList) {
        this.hostList = hostList;
    }

    @Override
    public List<Vm> getVmList() {
        return vmList;
    }

    @Override
    public void setVmList(final List<Vm> vmList) {
        this.vmList = vmList;
    }

    /**
     * @return a random Host from the {@link #getHostList() available Host list}.
     */
    private Host getRandomHost() {
        final int idx = getRandomValue(hostList.size());
        return hostList.get(idx);
    }

    @Override
    public VmToHostMappingSolution createNeighbor(final VmToHostMappingSolution source) {
        final VmToHostMappingSolution clone = new VmToHostMappingSolution(source);
        clone.swapHostsOfTwoRandomSelectedMapEntries();
        return clone;
    }

}
