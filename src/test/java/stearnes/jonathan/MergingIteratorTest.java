package stearnes.jonathan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MergingIteratorTest {

	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	@Test
	public void testMergingIteratorConstructorReturnsPromptly()
	{
		long startTime = System.nanoTime();
		Collection<? extends Iterator<String>> iterators = new ArrayList<Iterator<String>>();
		new MergingIterator<String>(iterators, createComparator(), false);
		long endTime = System.nanoTime();

		long duration = endTime - startTime;
		
		Assert.assertTrue(duration < 2000000);
	}
	
	@Test
	public void testConstructorThrowsNullPointerExceptionNullInputIterators()
	{
		expected.expect(NullPointerException.class);
		expected.expectMessage("Input iterators must not be null");
		new MergingIterator<String>(null, createComparator(), false);
	}

	@Test
	public void testConstructorThrowsNullPointerExceptionWithNullComparator()
	{
		expected.expect(NullPointerException.class);
		expected.expectMessage("Comparator must not be null");
		new MergingIterator<String>(new ArrayList<Iterator<String>>(), null, false);
	}
	
	@Test
	public void testHasNextReturnsFalseWhenInputIteratorsAreExhausted()
	{
		Collection<Iterator<String>> iterators = new ArrayList<Iterator<String>>();
		
		MergingIterator<String> mergingIterator = new MergingIterator<String>(iterators, createComparator(), false);
		
		Assert.assertFalse(mergingIterator.hasNext());
	}
	
	@Test
	public void testHasNextReturnsTrueWhenInputIteratorsAreNotExhausted()
	{
		Collection<Iterator<String>> iterators = new ArrayList<Iterator<String>>();
		iterators.add(Arrays.asList("1").iterator());
		
		MergingIterator<String> mergingIterator = new MergingIterator<String>(iterators, createComparator(), false);
		
		Assert.assertTrue(mergingIterator.hasNext());
	}
	
	@Test
	public void testNextThrowsNoSuchElementExceptionWhenInputIteratorsAreExhausted()
	{
		Collection<Iterator<String>> iterators = new ArrayList<Iterator<String>>();
		MergingIterator<String> mergingIterator = new MergingIterator<String>(iterators , createComparator(), false);
		
		expected.expect(NoSuchElementException.class);
		mergingIterator.next();
	}

	@Test
	public void testNextReturnsSortedResults() {
		
		Collection<Iterator<String>> iterators = new ArrayList<Iterator<String>>();
		iterators.add(Arrays.asList("7", "8", "9").iterator());
		iterators.add(Arrays.asList("4", "5", "6").iterator());
		iterators.add(Arrays.asList("1", "2", "3").iterator());
		
		MergingIterator<String> mergingIterator = new MergingIterator<String>(iterators, createComparator(), false);
		
		int i = 0;
		do {
			Assert.assertEquals(String.valueOf(++i), mergingIterator.next());
		} while (mergingIterator.hasNext());
		Assert.assertEquals(9, i);
	}
	
	@Test
	public void testNextAllowsDuplicatesWhenEliminateDuplicatesIsFalse()
	{
		Collection<Iterator<String>> iterators = new ArrayList<Iterator<String>>();
		iterators.add(Arrays.asList("1", "1", "1").iterator());
		
		MergingIterator<String> mergingIterator = new MergingIterator<String>(iterators, createComparator(), false);
		
		int i = 0;
		do {
			++i;
			Assert.assertEquals("1", mergingIterator.next());
		} while (mergingIterator.hasNext());
		Assert.assertEquals(3, i);
	}
	
	@Test
	public void testNextEliminatesDuplicatesWhenEliminateDuplicatesIsTrue()
	{
		Collection<Iterator<String>> iterators = new ArrayList<Iterator<String>>();
		iterators.add(Arrays.asList("1", "1", "1").iterator());
		
		MergingIterator<String> mergingIterator = new MergingIterator<String>(iterators, createComparator(), true);
		
		int i = 0;
		do {
			++i;
			Assert.assertEquals("1", mergingIterator.next());
		} while (mergingIterator.hasNext());
		Assert.assertEquals(1, i);
	}
	
	private Comparator<String> createComparator() {
		return new Comparator<String>() {
			
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		};
	}

}
