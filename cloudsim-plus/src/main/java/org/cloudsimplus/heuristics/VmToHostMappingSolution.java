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
import org.cloudbus.cloudsim.vms.Vm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A possible solution for mapping a set of Vms to a set of hosts.
 * It represents a solution generated using a {@link Heuristic} implementation.
 *
 * @author Manoel Campos da Silva Filho
 * @see Heuristic
 * @since CloudSim Plus 1.0
 */
public class VmToHostMappingSolution implements HeuristicSolution<Map<Vm, Host>> {
    /**
     * When two double values are subtracted to check if they are equal zero,
     * there may be some precision issues. This value is used to check the absolute difference between the two values
     * to avoid that solutions with little decimal difference be
     * considered different one of the other.
     */
    public static final double MIN_DIFF = 0.0001;

    /**
     * @see #getResult()
     */
    private final Map<Vm, Host> VmHostMap;

    /**
     * Indicates if the {@link #getCost() ()} has to be recomputed
     * due to changes in {@link #VmHostMap}.
     * When it is computed, its value is stored to be used
     * in subsequent calls, until the map is changed again, in order to
     * improve performance.
     */
    private boolean recomputeCost = true;

    /**
     * The last computed cost value, since the
     * last time the {@link #VmHostMap} was changed.
     * @see #getCost()
     * @see #recomputeCost
     */
    private double lastCost;

    private final Heuristic heuristic;

    /**
     * Creates a new solution for mapping a set of Vms to hosts using
     * a given heuristic implementation.
     *
     * @param heuristic the heuristic implementation used to find the solution
     * being created.
     */
    public VmToHostMappingSolution(final Heuristic heuristic){
        this(heuristic, new HashMap<>());
    }

    private VmToHostMappingSolution(final Heuristic heuristic, final Map<Vm, Host> VmHostMap){
        this.heuristic = heuristic;
        this.VmHostMap = VmHostMap;
    }

    /**
     * Clones a given solution.
     *
     * @param solution the solution to be cloned
     */
    public VmToHostMappingSolution(final VmToHostMappingSolution solution){
        this(solution.heuristic, new HashMap<>(solution.VmHostMap));
        this.recomputeCost = solution.recomputeCost;
        this.lastCost = solution.lastCost;
        this.VmHostMap.putAll(solution.VmHostMap);
    }

    /**
     * Binds a Vm to be executed by a given host
     *
     * @param vm the Vm to be added to a host
     * @param host the host to assign a Vm to
     */
    public void bindVmToHost(final Vm vm, final Host host){
        VmHostMap.put(vm, host);
        recomputeCost = true;
    }

    @Override
    public Heuristic<HeuristicSolution<Map<Vm, Host>>> getHeuristic() {
        return heuristic;
    }

    private void recomputeCostIfRequested() {
        if(!this.recomputeCost) {
            return;
        }

        this.lastCost = computeCostOfAllHosts();
        this.recomputeCost = false;
    }

    private double computeCostOfAllHosts() {
        return groupVmsByHost()
            .entrySet()
            .stream()
            .mapToDouble(this::getHostCost)
            .sum();
    }

    /**
     * Gets a Map of Cloudlets grouped by their VMs,
     * where each key is a VM and the value is a List of Map Entries
     * containing the Cloudlets running in that VM.
     * @return
     */
    private Map<Host, List<Map.Entry<Vm, Host>>> groupVmsByHost() {
        return VmHostMap.entrySet().stream()
            .collect(Collectors.groupingBy(Map.Entry::getValue));
    }

    /**
     * {@inheritDoc}
     *
     * It computes the cost of the entire mapping between Hosts and Vms
     *
     * @return {@inheritDoc}
     */
    @Override
    public double getCost() {
        recomputeCostIfRequested();
        return this.lastCost;
    }

    /**
     * It computes the costs of the entire mapping between Hosts and Vms
     *
     * @param forceRecompute indicate if the cost has to be recomputed anyway
     * @return the cost of the entire mapping between Vm's and cloudlets
     * @see #getCost()
     */
    public double getCost(final boolean forceRecompute) {
        recomputeCost |= forceRecompute;
        return getCost();
    }

    /**
     * Computes the cost of all Vms hosted by a given Host.
     * The cost is constant as of right now, but must be changed to allow any parameters to be used to compute it.
     *
     * @param entry a Map Entry where the key is a Host hosting some Vms
     *              and the value is the Vms hosted by this Host
     * @return the Host's cost to host the VMs
     */
    public double getHostCost(final Map.Entry<Host, List<Map.Entry<Vm, Host>>> entry) {
        final Host host = entry.getKey();
        final List<Vm> vms = convertListOfMapEntriesToListOfVms(entry.getValue());
        return getHostCost(host, vms);
    }

    /**
     * Computes the cost of all Vms hosted by a given Host.

     * @param host the Host to compute the cost to host some Vms
     * @param vms the list of Vms to be hosted by the host in order to compute the cost
     * @return the Host cost to host the Vms
     */
    public double getHostCost(final Host host, final List<Vm> vms) {
        return host.getNumberOfPes() - getTotalVmsPes(vms);
    }

    private List<Vm> convertListOfMapEntriesToListOfVms(final List<Map.Entry<Vm, Host>> entriesList) {
        return entriesList
            .stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Gets the total number of PEs from a list of Vms
     * @param listOfVmsForHost the list of Vms to get the total number of PEs
     * @return the total number of PEs from all given Vms
     */
    private long getTotalVmsPes(final List<Vm> listOfVmsForHost) {
        return listOfVmsForHost
            .stream()
            .mapToLong(Vm::getNumberOfPes)
            .sum();
    }

    /**
     * Compares this solution with another given one, based on the solution
     * cost. The current object is considered to be:
     * equal to the given object if they have the same cost;
     * greater than the given object if it has a lower cost;
     * lower than the given object if it has a higher cost;
     *
     * @param solution the solution to compare this instance to
     * @return {@inheritDoc}
     */

    @Override
    public int compareTo(final HeuristicSolution solution) {
        final double diff = this.getCost() - solution.getCost();

        if(Math.abs(diff) <= MIN_DIFF) {
            return 0;
        }

        return (diff > 0 ? -1 : 1);
    }

    /**
     *
     * @return the actual solution, providing the mapping between Vms and Hosts
     */
    @Override
    public Map<Vm, Host> getResult() {
        return Collections.unmodifiableMap(VmHostMap);
    }

    /**
     * Swap the Hosts of 2 randomly selected Vms
     * in the {@link #VmHostMap} in order to
     * provide a neighbor solution.
     *
     * The method change the given Map entries, moving the
     * Vm of the first entry to the Vm of the second entry
     * and vice-versa.
     *
     * @param entries a List of 2 entries containing Vms to swap their Hosts.
     * If the entries don't have 2 elements, the method will
     * return without performing any change in the entries.
     * @return true if the Hosts of the Vms were swapped, false otherwise
     */
    protected final boolean swapHostsOfTwoMapEntries(final List<Map.Entry<Vm, Host>> entries) {
        if(entries == null || entries.size() != 2 || entries.get(0) == null || entries.get(1) == null) {
            return false;
        }

        final Host host0 = entries.get(0).getValue();
        final Host host1 = entries.get(1).getValue();
        entries.get(0).setValue(host1);
        entries.get(1).setValue(host0);

        return true;
    }

    /**
     * Swap the Hosts of 2 randomly selected Vms
     * in the {@link #VmHostMap} in order to
     * provide a neighbor solution.
     *
     * The method change the given Map entries, moving the
     * Vm of the first entry to the Host of the second entry
     * and vice-versa.
     *
     * @see #swapHostsOfTwoMapEntries(List)
     * @return true if the Vms' Hosts were swapped, false otherwise
     */
    boolean swapHostsOfTwoRandomSelectedMapEntries() {
        return swapHostsOfTwoMapEntries(getRandomMapEntries());
    }

    /**
     * Try to get 2 randomly selected entries from the {@link #VmHostMap}.
     *
     * @return a List with 2 entries from the {@link #VmHostMap} if the map size is at least 2;
     *         an unitary List if the map has only 1 entry;
     *         or an empty List if there is no entry in the map.
     *
     * @see #swapHostsOfTwoMapEntries(List)
     */
    protected List<Map.Entry<Vm, Host>> getRandomMapEntries() {
        if(VmHostMap.isEmpty()) {
            return new ArrayList<>();
        }

        if(VmHostMap.size() == 1) {
            return createListWithFirstMapEntry();
        }

        return createListWithTwoRandomEntries();
    }

    /**
     * Creates a List using only the first entry in the {@link #VmHostMap}.
     * @return a single-entry List with either the first {@link #VmHostMap} entry,
     *         or an empty List in case no entry is found.
     */
    private List<Map.Entry<Vm, Host>> createListWithFirstMapEntry() {
        return VmHostMap.entrySet()
            .stream()
            .limit(1)
            .collect(Collectors.toList());
    }

    /**
     * Creates a List with 2 randomly selected entries from the {@link #VmHostMap}.
     * The way the method is called is ensured there is at least to entries
     * in the {@link #VmHostMap}.
     *
     * @return a List with the 2 randomly selected entries
     */
    private List<Map.Entry<Vm, Host>> createListWithTwoRandomEntries() {
        final int size = VmHostMap.entrySet().size();

        final int firstIdx = heuristic.getRandomValue(size);
        final int secondIdx = heuristic.getRandomValue(size);

        final List<Map.Entry<Vm, Host>> selected = new ArrayList<>(2);
        final Iterator<Map.Entry<Vm, Host>> it = VmHostMap.entrySet().iterator();

        /*
        Loop over the entries until those ones defined by the first and second index
        are found and added to the List.
        Since Map doesn't have an index, we can't access the ith entry directly.
        This loop ensures we iterate the least number of times
        until finding the required entries, without creating
        a List with all entries in order to get just two of them.
        */
        for(int i = 0; selected.size() < 2 && it.hasNext(); i++){
            final Map.Entry<Vm, Host> solution = it.next();
            if(i == firstIdx || i == secondIdx){
                selected.add(solution);
            }
        }

        return selected;
    }
}
