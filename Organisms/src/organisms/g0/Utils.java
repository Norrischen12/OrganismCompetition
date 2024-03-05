package organisms.g0;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import organisms.Move;

public class Utils {
	/**
	 * Populates an ArrayList with integers 1 through n, inclusive.
	 * @param n The number of elements to put in the list
	 * @return An {@link ArrayList} containing integers 1 through n, inclusive
	 */
	public static ArrayList<Integer> randomArrayList(int n) {
		ArrayList<Integer> list = new ArrayList<>(n);
		for(int i = 1; i <= n; i++){
			list.add(i);
		}
		Collections.shuffle(list);
		return list;
	}
	
	public static int flipBit(int number, int bitToFlip) {
		return number ^ (1 << bitToFlip);
	}
}
