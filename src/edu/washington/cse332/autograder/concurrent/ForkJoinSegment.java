package edu.washington.cse332.autograder.concurrent;

/**
 * Represents a single execution segment.
 * Enter Exit, and Joins form boundaries for each task id
 */
class ForkJoinSegment {
    private final long _taskId;
    private final int segmentId;
    private final ForkJoinEvent _startEvent;
    private final ForkJoinEvent _finishEvent;

    ForkJoinSegment(ForkJoinEvent startEvent, ForkJoinEvent finishEvent, int segId) {
        assert startEvent.getTaskId() != finishEvent.getTaskId();

        _taskId = startEvent.getTaskId();
        _startEvent = startEvent;
        _finishEvent = finishEvent;
        segmentId = segId;
    }

    long getTaskId() {
        return _taskId;
    }

    long getSegmentId() {
        return segmentId;
    }

    long getNanoSeconds() {
        return _finishEvent.getTimestamp() - _startEvent.getTimestamp();
    }

    long getStartTime() {
        return _startEvent.getTimestamp();
    }

    long getEndTime() {
        return _finishEvent.getTimestamp();
    }

    @Override
    public int hashCode() {
        return (int) _taskId ^ (int) (_taskId << 32) ^ segmentId;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ForkJoinSegment seg
                && seg.segmentId == segmentId
                && seg._taskId == _taskId;
    }

    @Override
    public String toString() {
        return _taskId + "_" + segmentId;
    }
}