package com.chwihae.infra.test;

import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class AbstractConcurrencyTest extends AbstractIntegrationTest {

    protected final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    protected ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(NUMBER_OF_CORES);
    }

    protected <T> List<Callable<T>> generateConcurrentTasks(int concurrentRequestCount, Callable<T> task) {
        return IntStream.range(0, concurrentRequestCount)
                .mapToObj(i -> task)
                .toList();
    }

    protected <T> List<Callable<T>> generateConcurrentTasks(int concurrentRequestCount, Function<Integer, Callable<T>> taskGenerator) {
        return IntStream.range(0, concurrentRequestCount)
                .mapToObj(taskGenerator::apply)
                .toList();
    }
}
