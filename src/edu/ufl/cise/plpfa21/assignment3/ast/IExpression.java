package edu.ufl.cise.plpfa21.assignment3.ast;

public interface IExpression extends IASTNode {

	IType getType();
	void setType(IType type);

}
