package edu.washington.cse332.autograder.concurrent;

import static edu.washington.cse332.autograder.concurrent.ForkJoinEvent.*;

/**
 * A singular event in the course of an execution.
 * 
 * @author Albert Du
 */
sealed abstract class ForkJoinEvent
        permits ComputeEvent, ComputeFinishedEvent, ForkEvent, JoinEvent, EnterEvent, ExitEvent {
    private final long timestamp = System.nanoTime();
    private final long _taskId;

    protected ForkJoinEvent(long taskId) {
        _taskId = taskId;
    }

    long getTimestamp() {
        return timestamp;
    }

    long getTaskId() {
        return _taskId;
    }

    static final class ComputeEvent extends ForkJoinEvent {
        private final long _childId;

        ComputeEvent(long thisId, long childId) {
            super(thisId);
            _childId = childId;
        }

        long getChildId() {
            return _childId;
        }

        @Override
        public String toString() {
            return String.format("%d Sync Compute from %d to %d", getTimestamp(), getTaskId(), getChildId());
        }
    }

    static final class ComputeFinishedEvent extends ForkJoinEvent {
        private final long _childId;

        ComputeFinishedEvent(long thisId, long childId) {
            super(thisId);
            _childId = childId;
        }

        long getChildId() {
            return _childId;
        }

        @Override
        public String toString() {
            return String.format("%d Sync Compute finished from %d to %d", getTimestamp(), getTaskId(), getChildId());
        }
    }

    static final class ForkEvent extends ForkJoinEvent {
        private final long _childId;

        ForkEvent(long thisId, long childId) {
            super(thisId);
            _childId = childId;
        }

        long getChildId() {
            return _childId;
        }

        @Override
        public String toString() {
            return String.format("%d Forked from %d to %d", getTimestamp(), getTaskId(), getChildId());
        }
    }

    static final class JoinEvent extends ForkJoinEvent {
        private final long _childId;

        JoinEvent(long thisId, long childId) {
            super(thisId);
            _childId = childId;
        }

        long getChildId() {
            return _childId;
        }

        @Override
        public String toString() {
            return String.format("%d Joined from %d back to %d", getTimestamp(), getChildId(), getTaskId());
        }
    }

    static final class EnterEvent extends ForkJoinEvent {
        EnterEvent(long taskId) {
            super(taskId);
        }

        @Override
        public String toString() {
            return String.format("%d Entered %d", getTimestamp(), getTaskId());
        }
    }

    static final class ExitEvent extends ForkJoinEvent {
        ExitEvent(long taskId) {
            super(taskId);
        }

        @Override
        public String toString() {
            return String.format("%d Exited %d", getTimestamp(), getTaskId());
        }
    }
}