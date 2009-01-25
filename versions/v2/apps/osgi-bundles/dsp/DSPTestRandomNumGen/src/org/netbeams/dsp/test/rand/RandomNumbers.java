package org.netbeams.dsp.test.rand;

import java.util.ArrayList;
import org.netbeams.dsp.message.MessageContent;

public class RandomNumbers extends MessageContent {
	
	private ArrayList<RandomNumber> rNumbers;
	
	
	public RandomNumbers() {
		rNumbers = new ArrayList<RandomNumber> ();
	};
	
	public ArrayList<RandomNumber> getRandomNumber() {
		return rNumbers;
	}

}
