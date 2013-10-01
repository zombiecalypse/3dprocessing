package helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StaticHelpers {
	public static <A, B> Iterable<Pair<A, B>> zip(Iterable<A> a, Iterable<B> b) {
		Iterator<A> iter_a = a.iterator();
		Iterator<B> iter_b = b.iterator();

		List<Pair<A, B>> ret = new ArrayList<Pair<A, B>>();

		while (iter_a.hasNext() && iter_b.hasNext()) {
			ret.add(new Pair<A, B>(iter_a.next(), iter_b.next()));
		}
		return ret;
	}
	
	public static <A, B> Iterable<Pair<A, B>> zip(Iterator<A> a, Iterator<B> b) {
		return zip(iter(a), iter(b));
	}

	public static <A, B> Iterable<Pair<A, B>> zip(Iterable<A> a, Iterator<B> b) {
		return zip(iter(a), iter(b));
	}
	
	public static <A, B> Iterable<Pair<A, B>> zip(Iterator<A> a, Iterable<B> b) {
		return zip(iter(a), iter(b));
	}
	
	public static Iterator<Integer> count(final int start) {
		return new Iterator<Integer>() {
			int current = start;

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Integer next() {
				return current++;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		
	}

	public static <A> Iterable<A> iter(Iterable<A> x) {
		return x;
	}

	public static <A> Iterable<A> iter(Iterator<A> x) {
		return new TrivialIterable<A>(x);
	}

	public static <A> long len(Iterable<A> x) {
		int len = 0;
		for (A a : x) {
			len++;
		}
		return len;
	}

	public static <A> long len(Iterator<A> x) {
		return len(iter(x));
	}

	public static <A> List<A> list(Iterable<A> x) {
		ArrayList<A> l = new ArrayList<A>();
		for (A a : x)
			l.add(a);
		return l;
	}

	public static <A> List<A> list(Iterator<A> x) {
		return list(iter(x));
	}

	public static <A> Set<A> set(Iterable<A> x) {
		HashSet<A> l = new HashSet<A>();
		for (A a : x)
			l.add(a);
		return l;
	}

	public static <A> Set<A> set(Iterator<A> x) {
		return set(iter(x));
	}

	public static class Pair<A, B> {
		public A a;
		public B b;

		public Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}
	}
}

class TrivialIterable<A> implements Iterable<A> {
	private Iterator<A> iter;

	public TrivialIterable(Iterator<A> x) {
		this.iter = x;
	}

	@Override
	public Iterator<A> iterator() {
		return iter;
	}
}