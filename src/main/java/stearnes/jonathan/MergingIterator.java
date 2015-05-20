package stearnes.jonathan;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

/**
 * <p>
 * Implementation of an {@link Iterator} that returns a SORTED stream of results
 * from a collection of input {@link Iterator}s, where the values in the input
 * iterators are also sorted.
 * </p>
 * 
 * <p>
 * The values returned by the input iterators WILL ALWAYS BE SORTED according to
 * the same {@link Comparator} that is specified in {@link MergingIterator}'s
 * constructor. The input iterators will never contain <tt>null</tt>
 * </p>
 * 
 * <p>
 * The input iterators are expected to have a very large (or possibly infinite)
 * set of results, so this implementation returns results AS SOON AS THEY ARE
 * AVAILABLE, rather than waiting until the input iterators are drained before
 * returning results.
 * </p>
 * 
 * <p>
 * This class can also eliminate duplicate values from the input iterators
 * (making all output values distinct), if the <tt>eliminateDuplicates</tt> flag
 * is set in the constructor. Note that the specified {@link Comparator} is used
 * to determine if two objects are identical, rather than the objects'
 * {@link #equals(Object)} methods.
 * </p>
 * 
 * <p>
 * Note: this class is <em>not</em> thread-safe, and cannot be called
 * concurrently from multiple threads
 * </p>
 * 
 */
public class MergingIterator<T> implements Iterator<T> {

    private final transient AtomicReference<T> recent = new AtomicReference<T>();
	private UnmodifiableIterator<T> origin;
	private TreeSet<T> seen;
	private boolean eliminateDuplicates;

	/**
	 * <p>
	 * Creates a {@link MergingIterator} with the specified collection of input
	 * {@link Iterator}s and {@link Comparator}, eliminating duplicates if
	 * required.
	 * 
	 * <p>
	 * This constructor must NOT call the {@link Iterator#next()} or
	 * {@link #hasNext()} methods of the input iterators, as these methods may
	 * block, and the constructor needs to return as soon as possible.
	 * 
	 * @param iterators
	 *            The collection of input iterators, over which the results will
	 *            be returned. The values in these iterators WILL ALWAYS BE
	 *            SORTED by the specified {@link Comparator}.
	 * 
	 * @param comparator
	 *            The {@link Comparator} that is to be used to compare items.
	 *            The output values returned from {@link #next()} will be
	 *            ordered by this comparator, and the values in the input
	 *            iterators should also be ordered by it.
	 * 
	 * @param eliminateDuplicates
	 *            If this is set to <tt>true</tt> then the output values will
	 *            contain no duplicate values, according to the specified
	 *            {@link Comparator}.
	 */
	public MergingIterator(final Collection<? extends Iterator<T>> iterators,
			final Comparator<T> comparator, final boolean eliminateDuplicates) {

		this.eliminateDuplicates = eliminateDuplicates;
		Preconditions.checkNotNull(iterators, "Input iterators must not be null");
		Preconditions.checkNotNull(comparator, "Comparator must not be null");
		origin = Iterators.mergeSorted(
				iterators, comparator);
		seen = new TreeSet<T>(comparator);
	}

	/**
	 * @see Iterator#hasNext()
	 */
	public boolean hasNext() {
		while (this.recent.get() == null && this.origin.hasNext()) {
            final T next = this.origin.next();
            if (eliminateDuplicates)
            {
                if (!this.seen.contains(next)) {
                    this.seen.add(next);
                    this.recent.set(next);
                }
            }
            else
            {
                this.seen.add(next);
                this.recent.set(next);
            }
        }
        return this.recent.get() != null;
	}

	/**
	 * Returns the next value from the input iterators, where values are
	 * returned <em>in order</em>, according to the {@link Comparator} supplied
	 * in the {@link #MergingIterator(Collection, Comparator, boolean)}
	 * constructor.
	 * 
	 * @throws NoSuchElementException
	 *             if there are no more values to return
	 */
	public T next() {

		if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        return this.recent.getAndSet(null);
	}

}