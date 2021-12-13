package edu.ufl.cise.plpfa21.assignment1;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ufl.cise.plpfa21.assignment1.PLPTokenKinds.Kind;

public class PLPLexer implements IPLPLexer {

	private String input;
	String currentToken = "";
	String intRegEx = "^\\d+$";
	String idenRegEx = "^([a-zA-Z_$][a-zA-Z\\d_$]*)$";
	char[] tokenCharArray;
	int currentPos = 0;
	int lineNo = 1;
	int charPosInLine = 0;
	int intValue;
	private Boolean firstParse = true;

	public PLPLexer(String input) {
		super();
		this.input = input;
	}

	@Override
	public IPLPToken nextToken() throws LexicalException {

		/*
		 * Comments handle
		 */
		if (firstParse) {
			firstParse = false;
			this.ignoreComments(this);
		}

		int startPos = 0, endPos = 0;
		currentPos = 0;
		char[] inputCharArray = input.toCharArray();

		while (startPos < inputCharArray.length && isWhiteSpaces(inputCharArray[startPos])) {
			if (inputCharArray[startPos] == '\n') {
				lineNo++;
				charPosInLine = 0;

			} else {
				charPosInLine++;
			}
			startPos++;
		}

		endPos = startPos;

		// to handle comments
		if (endPos + 1 < inputCharArray.length && inputCharArray[endPos] == '/' && inputCharArray[endPos + 1] == '*') {
			endPos = endPos + 2;
			while (endPos + 1 < inputCharArray.length && inputCharArray[endPos] != '*'
					&& inputCharArray[endPos + 1] != '/') {
				if (isWhiteSpaces(inputCharArray[endPos])) {
					if (inputCharArray[endPos] == '\n') {
						lineNo++;
						charPosInLine = 0;

					} else {
						charPosInLine++;
					}
				}
				endPos++;
			}
			if (inputCharArray[endPos + 1] == '/') {
				charPosInLine = charPosInLine + 3;
				endPos = endPos + 2;
			}
			input = input.substring(endPos);
			return null;
		}

		// to handle String Literals
		if (endPos < inputCharArray.length && inputCharArray[endPos] == '"') {
			endPos++;
			while (true) {
				if (endPos < inputCharArray.length && inputCharArray[endPos] == '"'
						&& inputCharArray[endPos - 1] != '\\') {
					IPLPToken token = new PLPToken();
					endPos++;
					token.setKind(Kind.STRING_LITERAL);
					token.setLine(lineNo);
					token.setCharPositionInLine(charPosInLine);
					currentToken = charArrayToString(inputCharArray, startPos, endPos);
					token.setText(currentToken);
					token.setStringVaue(currentToken);
					input = input.substring(endPos);
					charPosInLine = charPosInLine + currentToken.length();
					return token;
				}
				endPos++;
			}
		}

		if (endPos < inputCharArray.length && inputCharArray[endPos] == '\'') {
			endPos++;
			while (true) {
				if (endPos < inputCharArray.length && inputCharArray[endPos] == '\''
						&& inputCharArray[endPos - 1] != '\\') {
					IPLPToken token = new PLPToken();
					endPos++;
					token.setKind(Kind.STRING_LITERAL);
					token.setLine(lineNo);
					token.setCharPositionInLine(charPosInLine);
					currentToken = charArrayToString(inputCharArray, startPos, endPos);
					token.setText(currentToken);
					token.setStringVaue(currentToken);
					input = input.substring(endPos);
					charPosInLine = charPosInLine + currentToken.length();
					return token;
				}
				endPos++;
			}
		}

		// to get the currentToken from inputCharArray
		while (startPos < inputCharArray.length && !isWhiteSpaces(inputCharArray[endPos])) {
			endPos++;
		}

		IPLPToken token = new PLPToken();

		if (startPos == endPos) {
			token.setKind(Kind.EOF);
			return token;
		}

		currentToken = charArrayToString(inputCharArray, startPos, endPos);

		Kind currentTokenKind = getTokenKind(currentToken);

		if (currentTokenKind == Kind.ERROR) {
			throw new LexicalException("Given token can not be identified", lineNo, charPosInLine);
		}

		token.setKind(currentTokenKind);
		if (currentTokenKind == Kind.INT_LITERAL) {
			try {
				Integer intNumber = Integer.parseInt(currentToken);
				token.setIntValue(intNumber);
			} catch (NumberFormatException numberFormatException) {
				throw new LexicalException("Error Message", lineNo, charPosInLine);
			}
		}
		token.setLine(lineNo);
		token.setCharPositionInLine(charPosInLine);
		token.setText(currentToken);
		input = currentPos > 0 ? input.substring(startPos + currentPos) : input.substring(endPos);
		charPosInLine = symbolList.contains(currentTokenKind) ? charPosInLine + 1
				: charPosInLine + currentToken.length();
		return token;
	}

	private void ignoreComments(PLPLexer plpLexer) {
		while (input.contains("/*")) {
			int commentStartIndex = input.indexOf("/*");
			int commentEndIndex = input.indexOf("*/", commentStartIndex);

			for (int i = commentStartIndex; i < commentEndIndex + 2; i++) {
				if (input.charAt(i) == '\n')
					continue;
				else
					input = input.substring(0, i) + " " + input.substring(i + 1);
			}
		}
		return;
	}

	public boolean isWhiteSpaces(char c) {
		if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
			return true;
		} else
			return false;
	}

	public String charArrayToString(char[] arr, int startPos, int endPos) {
		return new String(arr, startPos, endPos - startPos);
	}

	public Kind getTokenKind(String str) {
		if (currentToken.matches(intRegEx)) {
			return Kind.INT_LITERAL;
		} else if (currentToken.matches(idenRegEx)) {
			if (isKeyword(currentToken) != null) {
				return isKeyword(currentToken);
			} else
				return Kind.IDENTIFIER;
		} else {
			tokenCharArray = currentToken.toCharArray();
			if (tokenCharArray[currentPos] == '=') {
				if (tokenCharArray.length > currentPos + 1 && tokenCharArray[currentPos + 1] == '=') {
					currentToken = String.valueOf(tokenCharArray[currentPos])
							+ String.valueOf(tokenCharArray[currentPos + 1]);
					currentPos = currentPos + 2;
					return isSymbol(currentToken);
				} else {
					currentPos = currentPos + 1;
					currentToken = String.valueOf(tokenCharArray[currentPos - 1]);
					return isSymbol(String.valueOf(tokenCharArray[currentPos - 1]));
				}
			} else if (tokenCharArray[currentPos] == '!') {
				if (tokenCharArray.length > currentPos + 1 && tokenCharArray[currentPos + 1] == '=') {
					currentToken = String.valueOf(tokenCharArray[currentPos])
							+ String.valueOf(tokenCharArray[currentPos + 1]);
					currentPos = currentPos + 2;
					return isSymbol(currentToken);
				} else {
					currentPos = currentPos + 1;
					currentToken = String.valueOf(tokenCharArray[currentPos - 1]);
					return isSymbol(String.valueOf(tokenCharArray[currentPos - 1]));
				}
			} else if (tokenCharArray[currentPos] == '&') {
				if (tokenCharArray.length > currentPos + 1 && tokenCharArray[currentPos + 1] == '&') {
					currentToken = String.valueOf(tokenCharArray[currentPos])
							+ String.valueOf(tokenCharArray[currentPos + 1]);
					currentPos = currentPos + 2;
					return isSymbol(currentToken);
				} else {
					currentPos = currentPos + 1;
					currentToken = String.valueOf(tokenCharArray[currentPos - 1]);
					return isSymbol(String.valueOf(tokenCharArray[currentPos - 1]));
				}
			} else if (tokenCharArray[currentPos] == '|') {
				if (tokenCharArray.length > currentPos + 1 && tokenCharArray[currentPos + 1] == '|') {
					currentToken = String.valueOf(tokenCharArray[currentPos])
							+ String.valueOf(tokenCharArray[currentPos + 1]);
					currentPos = currentPos + 2;
					return isSymbol(currentToken);
				} else {
					currentPos = currentPos + 1;
					currentToken = String.valueOf(tokenCharArray[currentPos - 1]);
					return isSymbol(String.valueOf(tokenCharArray[currentPos - 1]));
				}
			} else if (String.valueOf(tokenCharArray[currentPos]).matches(intRegEx)) {
				Pattern p = Pattern.compile("[0-9]+");
				Matcher m = p.matcher(String.valueOf(tokenCharArray));
				if (m.find()) {
					currentToken = m.group();
					currentPos = m.group().length();
					return Kind.INT_LITERAL;
				}
			} else if (String.valueOf(tokenCharArray[currentPos]).matches(idenRegEx)) {
				Pattern p = Pattern.compile("[a-zA-Z|_|$][A-Za-z|_|$|0-9]*");
				Matcher m = p.matcher(String.valueOf(tokenCharArray));
				if (m.find()) {
					currentToken = m.group();
					currentPos = m.group().length();
					if (isKeyword(currentToken) != null) {
						return isKeyword(currentToken);
					} else
						return Kind.IDENTIFIER;
				}
			} else {
				currentToken = String.valueOf(tokenCharArray[currentPos]);
				currentPos = currentPos + 1;
				return isSymbol(String.valueOf(tokenCharArray[currentPos - 1]));
			}
		}
		return null;
	}

	public static List<Kind> symbolList = new ArrayList<Kind>();
	static {
		symbolList.add(Kind.ASSIGN);
		symbolList.add(Kind.COMMA);
		symbolList.add(Kind.SEMI);
		symbolList.add(Kind.COLON);
		symbolList.add(Kind.LPAREN);
		symbolList.add(Kind.RPAREN);
		symbolList.add(Kind.LSQUARE);
		symbolList.add(Kind.RSQUARE);
		symbolList.add(Kind.AND);
		symbolList.add(Kind.OR);
		symbolList.add(Kind.LT);
		symbolList.add(Kind.GT);
		symbolList.add(Kind.BANG);
		symbolList.add(Kind.PLUS);
		symbolList.add(Kind.MINUS);
		symbolList.add(Kind.TIMES);
		symbolList.add(Kind.DIV);
	}

	public Kind isSymbol(String sym) {
		switch (sym) {
		case "=":
			return Kind.ASSIGN;
		case ",":
			return Kind.COMMA;
		case ";":
			return Kind.SEMI;
		case ":":
			return Kind.COLON;
		case "(":
			return Kind.LPAREN;
		case ")":
			return Kind.RPAREN;
		case "[":
			return Kind.LSQUARE;
		case "]":
			return Kind.RSQUARE;
		case "&&":
			return Kind.AND;
		case "||":
			return Kind.OR;
		case "<":
			return Kind.LT;
		case ">":
			return Kind.GT;
		case "==":
			return Kind.EQUALS;
		case "!=":
			return Kind.NOT_EQUALS;
		case "!":
			return Kind.BANG;
		case "+":
			return Kind.PLUS;
		case "-":
			return Kind.MINUS;
		case "*":
			return Kind.TIMES;
		case "/":
			return Kind.DIV;
		default:
			return Kind.ERROR;
		}
	}

	public Kind isKeyword(String keyword) {
		switch (keyword) {
		case "VAR":
			return Kind.KW_VAR;
		case "VAL":
			return Kind.KW_VAL;
		case "FUN":
			return Kind.KW_FUN;
		case "DO":
			return Kind.KW_DO;
		case "END":
			return Kind.KW_END;
		case "LET":
			return Kind.KW_LET;
		case "SWITCH":
			return Kind.KW_SWITCH;
		case "CASE":
			return Kind.KW_CASE;
		case "DEFAULT":
			return Kind.KW_DEFAULT;
		case "IF":
			return Kind.KW_IF;
		case "WHILE":
			return Kind.KW_WHILE;
		case "RETURN":
			return Kind.KW_RETURN;
		case "NIL":
			return Kind.KW_NIL;
		case "TRUE":
			return Kind.KW_TRUE;
		case "FALSE":
			return Kind.KW_FALSE;
		case "INT":
			return Kind.KW_INT;
		case "STRING":
			return Kind.KW_STRING;
		case "FLOAT":
			return Kind.KW_FLOAT;
		case "BOOLEAN":
			return Kind.KW_BOOLEAN;
		case "LIST":
			return Kind.KW_LIST;
		default:
			return null;
		}
	}
}
