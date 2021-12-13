package edu.ufl.cise.plpfa21.assignment4;

import java.util.HashSet;
import java.util.Set;

public class calculateSubstrings {

	public calculateSubstrings() {
		// TODO Auto-generated constructor stub
	}
	
	public static int getNoOfSubstrings(String str) {
		int strLen = str.length();
		int count = strLen;
		
		Set<String> uniqueSubStrings = new HashSet<String>();
		
		while(count > 0) {
			String temp = str.substring(0, count);
			System.out.println("Substrings ->" + temp);
			uniqueSubStrings.add(temp);
			count--;
		}
		
		count = 1;
		
		while(count < strLen) {
			String temp = str.substring(count, strLen);
			System.out.println("Substrings ->" + temp);
			uniqueSubStrings.add(temp);
			count++;
		}
		
		int countRight = strLen - 2;
		count = 1;
		
		while(count < countRight) {
			String temp = str.substring(0, count);
			countRight = temp.length() - 1;
			String temp2 = temp.substring(countRight, temp.length() - 1);
			System.out.println("Substrings 1 ->" + temp);
			System.out.println("Substrings 2->" + temp2);
			uniqueSubStrings.add(temp2);
			count++;
			countRight--;
		}
		
		return uniqueSubStrings.size();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int noOfStrings = getNoOfSubstrings("abcde");
		System.out.println("No of strings:" + noOfStrings);
	}

}
