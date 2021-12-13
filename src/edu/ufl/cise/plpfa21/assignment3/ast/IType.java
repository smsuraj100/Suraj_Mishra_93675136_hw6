package edu.ufl.cise.plpfa21.assignment3.ast;

public interface IType extends IASTNode {

	public static enum TypeKind {
		INT,
		BOOLEAN,
		STRING,
		LIST
	}
	
	boolean isInt();
	boolean isBoolean();
	boolean isString();
	boolean isList();
	boolean isKind(TypeKind kind);
	
	String getDesc() throws Exception;
	String getClassName() throws Exception;
	
}
