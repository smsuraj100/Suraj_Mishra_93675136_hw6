package edu.ufl.cise.plpfa21.assignment1;

public class PLPToken implements IPLPToken {

	private Kind kind;
	private String text;
	private int currentLine = 1;
	private int currentCharPosInLine = 0;
	private String currentStringValue;
	private int currentIntValue;

	@Override
	public Kind getKind() {
		return kind;
	}

	@Override
	public void setKind( Kind kind ) {
		this.kind = kind;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.text=text;
	}

	@Override
	public int getLine() {
		return currentLine;
	}

	@Override
	public void setLine(int line) {
		this.currentLine = line;
	}

	@Override
	public int getCharPositionInLine() {
		return currentCharPosInLine;
	}

	@Override
	public void setCharPositionInLine(int charPositionInLine) {
		this.currentCharPosInLine = charPositionInLine;
	}	

	@Override
	public String getStringValue() {
		return currentStringValue;
	}

	@Override
	public void setStringVaue(String stringValue) {
		this.currentStringValue = stringValue;
	}

	@Override
	public int getIntValue() {
		return currentIntValue;
	}

	@Override
	public void setIntValue(int intValue) {
		this.currentIntValue = intValue;
	}

	@Override
	public String toString() {
		return "PLPToken [kind=" + kind + ", text=" + text + ", currentLine=" + currentLine + ", currentCharPosInLine="
				+ currentCharPosInLine + ", currentStringValue=" + currentStringValue + ", currentIntValue="
				+ currentIntValue + "]";
	}

	
	
}
