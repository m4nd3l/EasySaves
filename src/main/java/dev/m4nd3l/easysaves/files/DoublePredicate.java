package dev.m4nd3l.easysaves.files;

/**
 * Functional interface evaluation system matching dual distinct parameterized values.
 *
 * @param <P1> Parameter structural type entry 1.
 * @param <P2> Parameter structural type entry 2.
 */
@FunctionalInterface
public interface DoublePredicate<P1, P2> {
    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param value1 The first input argument.
     * @param value2 The second input argument.
     * @return True if the input arguments match the predicate, otherwise false.
     */
    boolean test(P1 value1, P2 value2);
}