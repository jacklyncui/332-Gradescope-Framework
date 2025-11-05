package edu.washington.cse332.autograder.concurrent;

import java.util.Map;

/**
 * A result of parallelism analysis.
 *
 * @param graph        the task DAG
 * @param taskCount    number of new Recursive Actions or Tasks
 * @param perTaskCount task counts, broken down by name of class.
 * @param computeRatio ratio of compute calls to compute + fork calls
 * @author Albert Du
 */
public record ParallelismResult(ForkJoinGraph graph, long taskCount, Map<String, Long> perTaskCount, double computeRatio, Map<String, Double> perTaskComputeRatio, long poolInvokes) {
    public boolean allForkedTasksJoined() {
        return taskCount == 0 || graph.finalSegmentCount() == 1;
    }
}
