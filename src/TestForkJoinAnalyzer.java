
import edu.washington.cse332.autograder.concurrent.*;

void main() {
    testLiterallyNoForkJoin();
    testCorrectForkJoin();
    testBadForkJoin();
    testDoubleForkForkJoin();
    testInvalidForkFork();
    testInvalidForkCompute();
    testJoinJoin();
    testInvalidJoinFork();
    testInvalidJoinCompute();
    testInvalidComputeFork();
    testInvalidComputeJoin();
    testInvalidComputeCompute();
    testInvalidJoinNoFork();
}

void testLiterallyNoForkJoin() {
    var result = ForkJoinAnalyzer.analyze(() -> {
        int sum = 0;
        for (int i = 0; i < 100; i++) {
            sum += i;
        }
    });

    assert result.allForkedTasksJoined();
    assert result.perTaskCount().keySet().size() == 0;
    assert result.taskCount() == 0;
    assert result.computeRatio() == 1.0;
    assert result.poolInvokes() == 0;
    System.out.println(result.graph().toDOT()); // make sure this doesn't crash
    System.out.println(result.graph().empiricalSpeedup());

    System.out.println(result.graph().structuralSpeedup());
}

void testCorrectForkJoin() {
    int n = 10_000;
    int[] a = new int[n];
    int[] b = new int[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i;
    }

    var result = ForkJoinAnalyzer.analyze(() -> {
        GoodDotProduct task = new GoodDotProduct(a, b, 0, n);
        var dotProduct = ForkJoinPool.commonPool().invoke(task);
        System.out.println("Dot product: " + dotProduct);
    });

    // assert result.allForkedTasksJoined();
    assert result.perTaskCount().keySet().size() == 1;
    // print out all of the keys
    System.out.println("Task keys: " + result.perTaskCount().keySet());
    assert result.taskCount() > 1;
    System.out.println("Compute ratio: " + result.computeRatio());
    assert result.computeRatio() == 0.5;
    System.out.println(result.graph().toDOT()); // make sure this doesn't crash
    System.out.println("Empirical speedup: " + result.graph().empiricalSpeedup());
    System.out.println("Structural speedup: " + result.graph().structuralSpeedup());

    assert result.poolInvokes() == 1;
}

void testBadForkJoin() {
    int n = 10_000;
    int[] a = new int[n];
    int[] b = new int[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i;
    }

    var result = ForkJoinAnalyzer.analyze(() -> {
        BadDotProduct task = new BadDotProduct(a, b, 0, n);
        int dotProduct = ForkJoinPool.commonPool().invoke(task);
        System.out.println("Dot product: " + dotProduct);
    });

    assert result.allForkedTasksJoined();
    assert result.perTaskCount().keySet().size() == 1;
    // print out all of the keys
    System.out.println("Task keys: " + result.perTaskCount().keySet());
    assert result.taskCount() > 1;
    assert result.computeRatio() == 0.5;
    System.out.println("Compute ratio: " + result.computeRatio());
    System.out.println(result.graph().toDOT()); // make sure this doesn't crash
    System.out.println("Empirical speedup: " + result.graph().empiricalSpeedup());
    System.out.println("Structural speedup: " + result.graph().structuralSpeedup());
    assert result.poolInvokes() == 1;
}

void testDoubleForkForkJoin() {
    int n = 10_000;
    int[] a = new int[n];
    int[] b = new int[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i;
    }

    var result = ForkJoinAnalyzer.analyze(() -> {
        DoubleForkDotProduct task = new DoubleForkDotProduct(a, b, 0, n);
        var dotProduct = ForkJoinPool.commonPool().invoke(task);
        System.out.println("Dot product: " + dotProduct);
    });

    
    assert result.allForkedTasksJoined();
    assert result.perTaskCount().keySet().size() == 1;
    // print out all of the keys
    System.out.println("Task keys: " + result.perTaskCount().keySet());
    assert result.taskCount() > 1;
    System.out.println("Compute ratio: " + result.computeRatio());
    assert result.computeRatio() == 0;
    System.out.println("Empirical speedup: " + result.graph().empiricalSpeedup());
    System.out.println("Structural speedup: " + result.graph().structuralSpeedup());
    // System.out.println(result.graph().toDOT()); // make sure this doesn't crash
    assert result.poolInvokes() == 1;
}

void testInvalidForkFork() {
    int n = 10_000;
    int[] a = new int[n];
    int[] b = new int[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i;
    }

    try {
        var result = ForkJoinAnalyzer.analyze(() -> {
            InvalidForkForkDotProduct task = new InvalidForkForkDotProduct(a, b, 0, n);
            var dotProduct = ForkJoinPool.commonPool().invoke(task);
            System.out.println("Dot product: " + dotProduct);
        });
    } catch (BadParallelismException e) {
        System.out.println("Caught expected BadParallelismException: " + e.getMessage());
        return;
    }
    assert false : "Expected BadParallelismException was not thrown";
}

void testInvalidForkCompute() {
    int n = 10_000;
    int[] a = new int[n];
    int[] b = new int[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i;
    }

    try {
        var result = ForkJoinAnalyzer.analyze(() -> {
            InvalidForkComputeDotProduct task = new InvalidForkComputeDotProduct(a, b, 0, n);
            var dotProduct = ForkJoinPool.commonPool().invoke(task);
            System.out.println("Dot product: " + dotProduct);
        });
    } catch (BadParallelismException e) {
        System.out.println("Caught expected BadParallelismException: " + e.getMessage());
        return;
    }
    assert false : "Expected BadParallelismException was not thrown";
}

void testInvalidJoinFork() {
    int n = 10_000;
    int[] a = new int[n];
    int[] b = new int[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i;
    }

    try {
        var result = ForkJoinAnalyzer.analyze(() -> {
            InvalidJoinForkDotProduct task = new InvalidJoinForkDotProduct(a, b, 0, n);
            var dotProduct = ForkJoinPool.commonPool().invoke(task);
            System.out.println("Dot product: " + dotProduct);
        });
    } catch (BadParallelismException e) {
        System.out.println("Caught expected BadParallelismException: " + e.getMessage());
        return;
    }
    assert false : "Expected BadParallelismException was not thrown";
}

void testJoinJoin() {
    int n = 10_000;
    int[] a = new int[n];
    int[] b = new int[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i;
    }

    try {
        var result = ForkJoinAnalyzer.analyze(() -> {
            DoubleJoinJoinDotProduct task = new DoubleJoinJoinDotProduct(a, b, 0, n);
            var dotProduct = ForkJoinPool.commonPool().invoke(task);
            System.out.println("Dot product: " + dotProduct);
        });
    } catch (BadParallelismException e) {
        System.out.println("Caught expected BadParallelismException: " + e.getMessage());
        throw e;
    }
}

void testInvalidJoinCompute() {
    int n = 10_000;
    int[] a = new int[n];
    int[] b = new int[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i;
    }

    try {
        var result = ForkJoinAnalyzer.analyze(() -> {
            InvalidJoinComputeDotProduct task = new InvalidJoinComputeDotProduct(a, b, 0, n);
            var dotProduct = ForkJoinPool.commonPool().invoke(task);
            System.out.println("Dot product: " + dotProduct);
        });
    } catch (BadParallelismException e) {
        System.out.println("Caught expected BadParallelismException: " + e.getMessage());
        return;
    }
    assert false : "Expected BadParallelismException was not thrown";
}

void testInvalidComputeFork() {
    int n = 10_000;
    int[] a = new int[n];
    int[] b = new int[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i;
    }

    try {
        var result = ForkJoinAnalyzer.analyze(() -> {
            InvalidComputeForkDotProduct task = new InvalidComputeForkDotProduct(a, b, 0, n);
            var dotProduct = ForkJoinPool.commonPool().invoke(task);
            System.out.println("Dot product: " + dotProduct);
        });
    } catch (BadParallelismException e) {
        System.out.println("Caught expected BadParallelismException: " + e.getMessage());
        return;
    }
    assert false : "Expected BadParallelismException was not thrown";
}

void testInvalidComputeJoin() {
    int n = 10_000;
    int[] a = new int[n];
    int[] b = new int[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i;
    }

    try {
        var result = ForkJoinAnalyzer.analyze(() -> {
            InvalidComputeJoinDotProduct task = new InvalidComputeJoinDotProduct(a, b, 0, n);
            var dotProduct = ForkJoinPool.commonPool().invoke(task);
            System.out.println("Dot product: " + dotProduct);
        });
    } catch (BadParallelismException e) {
        System.out.println("Caught expected BadParallelismException: " + e.getMessage());
        return;
    }
    assert false : "Expected BadParallelismException was not thrown";
}

void testInvalidComputeCompute() {
    int n = 10_000;
    int[] a = new int[n];
    int[] b = new int[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i;
    }

    try {
        var result = ForkJoinAnalyzer.analyze(() -> {
            InvalidComputeComputeDotProduct task = new InvalidComputeComputeDotProduct(a, b, 0, n);
            var dotProduct = ForkJoinPool.commonPool().invoke(task);
            System.out.println("Dot product: " + dotProduct);
        });
    } catch (BadParallelismException e) {
        System.out.println("Caught expected BadParallelismException: " + e.getMessage());
        return;
    }
    assert false : "Expected BadParallelismException was not thrown";
}

void testInvalidJoinNoFork() {
    int n = 10_000;
    int[] a = new int[n];
    int[] b = new int[n];
    for (int i = 0; i < n; i++) {
        a[i] = i;
        b[i] = i;
    }

    try {
        var result = ForkJoinAnalyzer.analyze(() -> {
            InvalidJoinNoForkDotProduct task = new InvalidJoinNoForkDotProduct(a, b, 0, n);
            var dotProduct = ForkJoinPool.commonPool().invoke(task);
            System.out.println("Dot product: " + dotProduct);
        });
    } catch (BadParallelismException e) {
        System.out.println("Caught expected BadParallelismException: " + e.getMessage());
        return;
    }
    assert false : "Expected BadParallelismException was not thrown";
}

class GoodDotProduct extends RecursiveTask<Integer> {
    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;

    public GoodDotProduct(int[] a, int[] b, int start, int end) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer __impl_compute() {
        if (end - start <= THRESHOLD) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            GoodDotProduct leftTask = new GoodDotProduct(a, b, start, mid);
            GoodDotProduct rightTask = new GoodDotProduct(a, b, mid, end);
            leftTask.fork();
            int rightResult = rightTask.compute();
            int leftResult = leftTask.join();
            return leftResult + rightResult;
        }
    }
}

class BadDotProduct extends RecursiveTask<Integer> {
    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;

    public BadDotProduct(int[] a, int[] b, int start, int end) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer __impl_compute() {
        if (end - start <= THRESHOLD) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            BadDotProduct leftTask = new BadDotProduct(a, b, start, mid);
            BadDotProduct rightTask = new BadDotProduct(a, b, mid, end);
            int rightResult = rightTask.compute();
            leftTask.fork();
            int leftResult = leftTask.join();
            return leftResult + rightResult;
        }
    }
}

class DoubleForkDotProduct extends RecursiveTask<Integer> {
    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;

    public DoubleForkDotProduct(int[] a, int[] b, int start, int end) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer __impl_compute() {
        if (end - start <= THRESHOLD) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            DoubleForkDotProduct leftTask = new DoubleForkDotProduct(a, b, start, mid);
            DoubleForkDotProduct rightTask = new DoubleForkDotProduct(a, b, mid, end);
            leftTask.fork();
            rightTask.fork();
            int rightResult = rightTask.join();
            int leftResult = leftTask.join();
            return leftResult + rightResult;
        }
    }
}

class InvalidForkForkDotProduct extends RecursiveTask<Integer> {
    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;

    public InvalidForkForkDotProduct(int[] a, int[] b, int start, int end) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer __impl_compute() {
        if (end - start <= THRESHOLD) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            InvalidForkForkDotProduct leftTask = new InvalidForkForkDotProduct(a, b, start, mid);
            InvalidForkForkDotProduct rightTask = new InvalidForkForkDotProduct(a, b, mid, end);
            leftTask.fork();
            leftTask.fork();// INVALID: forking the same task twice
            rightTask.fork();
            int rightResult = rightTask.join();
            int leftResult = leftTask.join();
            return leftResult + rightResult;
        }
    }
}

class InvalidForkComputeDotProduct extends RecursiveTask<Integer> {
    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;

    public InvalidForkComputeDotProduct(int[] a, int[] b, int start, int end) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer __impl_compute() {
        if (end - start <= THRESHOLD) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            InvalidForkComputeDotProduct leftTask = new InvalidForkComputeDotProduct(a, b, start, mid);
            InvalidForkComputeDotProduct rightTask = new InvalidForkComputeDotProduct(a, b, mid, end);
            leftTask.fork();
            leftTask.compute();// INVALID: computing forked task
            rightTask.fork();
            int rightResult = rightTask.join();
            int leftResult = leftTask.join();
            return leftResult + rightResult;
        }
    }
}

class InvalidJoinForkDotProduct extends RecursiveTask<Integer> {
    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;

    public InvalidJoinForkDotProduct(int[] a, int[] b, int start, int end) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer __impl_compute() {
        if (end - start <= THRESHOLD) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            InvalidJoinForkDotProduct leftTask = new InvalidJoinForkDotProduct(a, b, start, mid);
            InvalidJoinForkDotProduct rightTask = new InvalidJoinForkDotProduct(a, b, mid, end);
            leftTask.fork();
            rightTask.compute();
            leftTask.join();
            leftTask.fork(); // INVALID: forking after compute
            return 5; // this doesn't matter
        }
    }
}



class DoubleJoinJoinDotProduct extends RecursiveTask<Integer> {
    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;

    public DoubleJoinJoinDotProduct(int[] a, int[] b, int start, int end) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer __impl_compute() {
        if (end - start <= THRESHOLD) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            DoubleJoinJoinDotProduct leftTask = new DoubleJoinJoinDotProduct(a, b, start, mid);
            DoubleJoinJoinDotProduct rightTask = new DoubleJoinJoinDotProduct(a, b, mid, end);
            leftTask.fork();
            rightTask.fork();
            leftTask.join();
            rightTask.join();
            leftTask.join(); // INVALID: joining the same task twice, but we allow
            return 5; // this doesn't matter
        }
    }
}


class InvalidJoinComputeDotProduct extends RecursiveTask<Integer> {
    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;

    public InvalidJoinComputeDotProduct(int[] a, int[] b, int start, int end) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer __impl_compute() {
        if (end - start <= THRESHOLD) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            InvalidJoinComputeDotProduct leftTask = new InvalidJoinComputeDotProduct(a, b, start, mid);
            InvalidJoinComputeDotProduct rightTask = new InvalidJoinComputeDotProduct(a, b, mid, end);
            leftTask.fork();
            rightTask.compute();
            leftTask.join();
            leftTask.compute(); // INVALID: compute after join
            return 5; // this doesn't matter
        }
    }
}

class InvalidComputeForkDotProduct extends RecursiveTask<Integer> {
    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;

    public InvalidComputeForkDotProduct(int[] a, int[] b, int start, int end) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer __impl_compute() {
        if (end - start <= THRESHOLD) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            InvalidComputeForkDotProduct leftTask = new InvalidComputeForkDotProduct(a, b, start, mid);
            InvalidComputeForkDotProduct rightTask = new InvalidComputeForkDotProduct(a, b, mid, end);
            leftTask.compute();
            rightTask.fork();
            leftTask.fork(); // INVALID: fork after compute
            return 5; // this doesn't matter
        }
    }
}

class InvalidComputeJoinDotProduct extends RecursiveTask<Integer> {
    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;

    public InvalidComputeJoinDotProduct(int[] a, int[] b, int start, int end) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer __impl_compute() {
        if (end - start <= THRESHOLD) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            InvalidComputeJoinDotProduct leftTask = new InvalidComputeJoinDotProduct(a, b, start, mid);
            InvalidComputeJoinDotProduct rightTask = new InvalidComputeJoinDotProduct(a, b, mid, end);
            leftTask.compute();
            rightTask.fork();
            leftTask.join(); // INVALID: join after compute
            return 5; // this doesn't matter
        }
    }
}

class InvalidComputeComputeDotProduct extends RecursiveTask<Integer> {
    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;

    public InvalidComputeComputeDotProduct(int[] a, int[] b, int start, int end) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer __impl_compute() {
        if (end - start <= THRESHOLD) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            InvalidComputeComputeDotProduct leftTask = new InvalidComputeComputeDotProduct(a, b, start, mid);
            InvalidComputeComputeDotProduct rightTask = new InvalidComputeComputeDotProduct(a, b, mid, end);
            leftTask.compute();
            leftTask.compute(); // INVALID: computing the same task twice
            rightTask.compute();
            return 5; // this doesn't matter
        }
    }
}

class InvalidJoinNoForkDotProduct extends RecursiveTask<Integer> {
    private final int[] a;
    private final int[] b;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;

    public InvalidJoinNoForkDotProduct(int[] a, int[] b, int start, int end) {
        this.a = a;
        this.b = b;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer __impl_compute() {
        if (end - start <= THRESHOLD) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            InvalidJoinNoForkDotProduct leftTask = new InvalidJoinNoForkDotProduct(a, b, start, mid);
            InvalidJoinNoForkDotProduct rightTask = new InvalidJoinNoForkDotProduct(a, b, mid, end);
            // leftTask.fork(); // MISSING FORK
            rightTask.compute();
            leftTask.join(); // INVALID: joining without forking
            return 5; // this doesn't matter
        }
    }
}