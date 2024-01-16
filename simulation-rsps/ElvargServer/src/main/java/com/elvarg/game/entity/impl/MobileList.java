package com.elvarg.game.entity.impl;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A collection that provides functionality for storing and managing characters.
 * This list does not support the storage of elements with a value of
 * {@code null}, and maintains an extremely strict ordering of the elements.
 * This list for storing characters will be blazingly faster than typical
 * implementations, mainly due to the fact that it uses a {@link Queue} to cache
 * the slots that characters are removed from in order to reduce the amount of
 * lookups needed to add a new character.
 *
 * @param <E>
 *            the type of character being managed with this collection.
 * @author lare96 <http://github.com/lare96>
 */
public final class MobileList<E extends Mobile> implements Iterable<E> {

	/**
	 * The queue containing all of the cached slots that can be assigned to
	 * {@link E}s to prevent expensive lookups.
	 */
	private final Queue<Integer> slotQueue = new ArrayDeque<>();
	/**
	 * The finite capacity of this collection.
	 */
	private final int capacity;
	/**
	 * The backing array of {@link E}s within this collection.
	 */
	private E[] characters;
	/**
	 * The size of this collection.
	 */
	private int size;

	/**
	 * Creates a new {@link MobileList}.
	 *
	 * @param capacity
	 *            the finite capacity of this collection.
	 */
	@SuppressWarnings("unchecked")
	public MobileList(int capacity) {
		this.capacity = ++capacity;
		this.characters = (E[]) new Mobile[capacity];
		this.size = 0;
		IntStream.rangeClosed(1, (capacity-1)).forEach(slotQueue::add);
	}

	/**
	 * Adds an element to this collection.
	 *
	 * @param e
	 *            the element to add to this collection.
	 * @return {@code true} if the element was successfully added, {@code false}
	 *         otherwise.
	 */
	public boolean add(E e) {
		if (e == null) {
			return false;
		}

		if (isFull()) {
			return false;
		}

		if (!e.isRegistered()) {
			int slot = slotQueue.remove();
			e.setRegistered(true);
			e.setIndex(slot);
			characters[slot] = e;
			e.onAdd();
			size++;
			return true;
		}
		return false;
	}

	/**
	 * Removes an element from this collection.
	 *
	 * @param e
	 *            the element to remove from this collection.
	 * @return {@code true} if the element was successfully removed, {@code false}
	 *         otherwise.
	 */
	public boolean remove(E e) {
		if (e == null) {
			return false;
		}

		if (e.isRegistered() && characters[e.getIndex()] != null) {
			e.setRegistered(false);
			characters[e.getIndex()] = null;
			slotQueue.add(e.getIndex());
			e.onRemove();
			size--;
			return true;
		}

		return false;
	}

	/**
	 * Determines if this collection contains the specified element.
	 *
	 * @param e
	 *            the element to determine if this collection contains.
	 * @return {@code true} if this collection contains the element, {@code false}
	 *         otherwise.
	 */
	public boolean contains(E e) {
		if (e == null) {
			return false;
		}
		return characters[e.getIndex()] != null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation will exclude all elements with a value of {@code null} to
	 * avoid {@link NullPointerException}s.
	 * <p>
	 * <p>
	 * <p>
	 * UPDATED: Now uses a shuffle_list to battle PID. This means that all
	 * characters are always processed in an random order instead of having higher
	 * priority than other characters because of a higher PID.
	 */
	@Override
	public void forEach(Consumer<? super E> action) {
		for (E e : characters) {
			if (e == null) {
				continue;
			}
			action.accept(e);
		}
	}

	@Override
	public Spliterator<E> spliterator() {
		return Spliterators.spliterator(characters, Spliterator.ORDERED);
	}

	/**
	 * Searches the backing array for the first element encountered that matches
	 * {@code filter}. This does not include elements with a value of {@code null}.
	 *
	 * @param filter
	 *            the predicate that the search will be based on.
	 * @return an optional holding the found element, or an empty optional if no
	 *         element was found.
	 */
	public Optional<E> search(Predicate<? super E> filter) {
		for (E e : characters) {
			if (e == null)
				continue;
			if (filter.test(e))
				return Optional.of(e);
		}
		return Optional.empty();
	}

	@Override
	public Iterator<E> iterator() {
		return new CharacterListIterator<>(this);
	}

	/**
	 * Retrieves the element on the given slot.
	 *
	 * @param slot
	 *            the slot to retrieve the element on.
	 * @return the element on the given slot or {@code null} if no element is on the
	 *         spot.
	 */
	public E get(int slot) {
		return characters[slot];
	}

	/**
	 * Determines the amount of elements stored in this collection.
	 *
	 * @return the amount of elements stored in this collection.
	 */
	public int size() {
		return size;
	}

	/**
	 * Gets the finite capacity of this collection.
	 *
	 * @return the finite capacity of this collection.
	 */
	public int capacity() {
		return capacity;
	}

	/**
	 * Gets the remaining amount of space in this collection.
	 *
	 * @return the remaining amount of space in this collection.
	 */
	public int spaceLeft() {
		return capacity - size;
	}

	/**
	 * Is the collection full?
	 *
	 * @return true if collection is full, otherwise false
	 */
	public boolean isFull() {
		return size + 1 >= capacity;
	}

	/**
	 * Returns a sequential stream with this collection as its source.
	 *
	 * @return a sequential stream over the elements in this collection.
	 */
	public Stream<E> stream() {
		return Arrays.stream(characters);
	}

	/**
	 * Removes all of the elements in this collection and resets the
	 * {@link MobileList#characters} and {@link MobileList#size}.
	 */
	@SuppressWarnings("unchecked")
	public void clear() {
		forEach(this::remove);
		characters = (E[]) new Mobile[capacity];
		size = 0;
	}

	/**
	 * An {@link Iterator} implementation that will iterate over the elements in a
	 * character list.
	 *
	 * @param <E>
	 *            the type of character being iterated over.
	 * @author lare96 <http://github.com/lare96>
	 */
	private static final class CharacterListIterator<E extends Mobile> implements Iterator<E> {

		/**
		 * The {@link MobileList} that is storing the elements.
		 */
		private final MobileList<E> list;

		/**
		 * The current index that the iterator is iterating over.
		 */
		private int index;

		/**
		 * The last index that the iterator iterated over.
		 */
		private int lastIndex = -1;

		/**
		 * Creates a new {@link CharacterListIterator}.
		 *
		 * @param list
		 *            the list that is storing the elements.
		 */
		public CharacterListIterator(MobileList<E> list) {
			this.list = list;
		}

		@Override
		public boolean hasNext() {
			return !(index + 1 > list.capacity());
		}

		@Override
		public E next() {
			if (index >= list.capacity()) {
				throw new ArrayIndexOutOfBoundsException("There are no " + "elements left to iterate over!");
			}
			lastIndex = index;
			index++;
			return list.characters[lastIndex];
		}

		@Override
		public void remove() {
			if (lastIndex == -1) {
				throw new IllegalStateException("This method can only be " + "called once after \"next\".");
			}
			list.remove(list.characters[lastIndex]);
			lastIndex = -1;
		}
	}
}