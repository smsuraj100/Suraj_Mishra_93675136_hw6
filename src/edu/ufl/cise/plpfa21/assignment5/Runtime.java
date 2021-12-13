package edu.ufl.cise.plpfa21.assignment5;

public class Runtime {

	public static boolean not(boolean arg) {
		return !arg;
	}
	
	public static int addition(int num1, int num2) {
		return num1 + num2;		
	}
	
	public static int subtraction(int num1, int num2) {
		return num1 - num2;		
	}
	
	public static int multiplication(int num1, int num2) {
		return num1 * num2;		
	}
	
	public static int division(int num1, int num2) {
		if (num2 == 0) {			
			return num1 / num2;		
		} else {
			return 0;
		}
	}
	
	public static boolean isNumEquals(int num1, int num2) {
		return num1 == num2;
	}
	
	public static boolean isNumNotEquals(int num1, int num2) {
		return num1 != num2;
	}
	
	public static boolean isStringEquals(String str1, String str2) {
		return str1.equals(str2);
	}
	
	public static boolean isStringNotEquals(String str1, String str2) {
		return !str1.equals(str2);
	}
	
	public static String concatString(String str1, String str2) {
		return str1 + str2;
	}
	
	public static boolean isNumGreater(int num1, int num2) {
		return num1 > num2;
	}
	
	public static boolean isNumLesser(int num1, int num2) {
		return num1 < num2;
	}
	
	public static boolean isStringGreater(String str1, String str2) {
		return str1.compareTo(str2) > 0 ? true : false;
	}
	
	public static boolean isStringLesser(String str1, String str2) {
		return str1.compareTo(str2) > 0 ? false : true;
	}
	
	public static boolean isBoolEquals(boolean bool1, boolean bool2) {
		return bool1 == bool2;
	}
	
	public static boolean isBoolNotEquals(boolean bool1, boolean bool2) {
		return bool1 != bool2;
	}
	
	public static boolean boolAndOp(boolean bool1, boolean bool2) {
		return bool1 && bool2;
	}
	
	public static boolean boolOrOp(boolean bool1, boolean bool2) {
		return bool1 || bool2;
	}
	
	public static int unaryMinus(int num) {
		return -num;
	}
}
