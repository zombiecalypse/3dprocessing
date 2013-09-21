package helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StaticHelpers {
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
