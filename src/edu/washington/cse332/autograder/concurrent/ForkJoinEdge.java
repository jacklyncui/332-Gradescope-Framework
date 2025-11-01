package edu.washington.cse332.autograder.concurrent;


/**
 * A directed graph edge. start -> end iff end must wait for start to finish
 * before itself beginning.
 *
 * @param start
 * @param end
 * @param type  The reason for this edge's addition.
 */
record ForkJoinEdge(ForkJoinSegment start, ForkJoinSegment end, Type type) {
    enum Type {
        /**
         * In a single task, following computes dependent on their antecedents.
         */
        SEQUENTIAL,
        /**
         * A task calling a different task's compute method.
         */
        COMPUTE,
        /**
         * A compute finishing, the parent's next task depends on it.
         */
        RETURN,
        /**
         * A new task starts
         */
        FORK,
        /**
         * Parent must wait for child to finish
         */
        JOIN
    }
}