package edu.ufl.cise.plpfa21.assignment4;

import java.util.List;

import edu.ufl.cise.plpfa21.assignment1.PLPTokenKinds.Kind;
import edu.ufl.cise.plpfa21.assignment3.ast.ASTVisitor;
import edu.ufl.cise.plpfa21.assignment3.ast.IASTNode;
import edu.ufl.cise.plpfa21.assignment3.ast.IAssignmentStatement;
import edu.ufl.cise.plpfa21.assignment3.ast.IBinaryExpression;
import edu.ufl.cise.plpfa21.assignment3.ast.IBlock;
import edu.ufl.cise.plpfa21.assignment3.ast.IBooleanLiteralExpression;
import edu.ufl.cise.plpfa21.assignment3.ast.IDeclaration;
import edu.ufl.cise.plpfa21.assignment3.ast.IExpression;
import edu.ufl.cise.plpfa21.assignment3.ast.IExpressionStatement;
import edu.ufl.cise.plpfa21.assignment3.ast.IFunctionCallExpression;
import edu.ufl.cise.plpfa21.assignment3.ast.IFunctionDeclaration;
import edu.ufl.cise.plpfa21.assignment3.ast.IIdentExpression;
import edu.ufl.cise.plpfa21.assignment3.ast.IIdentifier;
import edu.ufl.cise.plpfa21.assignment3.ast.IIfStatement;
import edu.ufl.cise.plpfa21.assignment3.ast.IImmutableGlobal;
import edu.ufl.cise.plpfa21.assignment3.ast.IIntLiteralExpression;
import edu.ufl.cise.plpfa21.assignment3.ast.ILetStatement;
import edu.ufl.cise.plpfa21.assignment3.ast.IListSelectorExpression;
import edu.ufl.cise.plpfa21.assignment3.ast.IListType;
import edu.ufl.cise.plpfa21.assignment3.ast.IMutableGlobal;
import edu.ufl.cise.plpfa21.assignment3.ast.INameDef;
import edu.ufl.cise.plpfa21.assignment3.ast.INilConstantExpression;
import edu.ufl.cise.plpfa21.assignment3.ast.IPrimitiveType;
import edu.ufl.cise.plpfa21.assignment3.ast.IProgram;
import edu.ufl.cise.plpfa21.assignment3.ast.IReturnStatement;
import edu.ufl.cise.plpfa21.assignment3.ast.IStatement;
import edu.ufl.cise.plpfa21.assignment3.ast.IStringLiteralExpression;
import edu.ufl.cise.plpfa21.assignment3.ast.ISwitchStatement;
import edu.ufl.cise.plpfa21.assignment3.ast.IType;
import edu.ufl.cise.plpfa21.assignment3.ast.IType.TypeKind;
import edu.ufl.cise.plpfa21.assignment3.ast.IUnaryExpression;
import edu.ufl.cise.plpfa21.assignment3.ast.IWhileStatement;
import edu.ufl.cise.plpfa21.assignment3.astimpl.ListType__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.PrimitiveType__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.Type__;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String m) {
			super(m);
		}
	}

	SymbolTable symtab = new SymbolTable();

	private void check(boolean b, IASTNode n, String message) throws TypeCheckException {
		if (!b) {
			if (n.getLine() > 0) {
				message = n.getLine() + ":" + n.getPosInLine() + " " + n.getText() + "\n" + message;
			}
			throw new TypeCheckException(message);
		}
	}

	@Override
	public Object visitIBinaryExpression(IBinaryExpression n, Object arg) throws Exception {

		IExpression leftExp = n.getLeft();
		IExpression rightExp = n.getRight();

		IType typeOfLeftExp = (IType) leftExp.visit(this, arg);
		IType typeOfRightExp = (IType) rightExp.visit(this, arg);

		Kind operation = n.getOp();

		if ((operation == Kind.LT || operation == Kind.GT || operation == Kind.EQUALS || operation == Kind.NOT_EQUALS)
				&& (typeOfLeftExp.equals(typeOfRightExp)))
			n.setType(PrimitiveType__.booleanType);
		else if (operation == Kind.PLUS && ((typeOfLeftExp.isInt() && typeOfRightExp.isInt())
				|| (typeOfLeftExp.isString() && typeOfRightExp.isString())
				|| (typeOfLeftExp.isList() && typeOfRightExp.isList())))
			n.setType(typeOfRightExp);
		else if ((operation == Kind.MINUS || operation == Kind.TIMES || operation == Kind.DIV)
				&& ((typeOfLeftExp.isInt() && typeOfRightExp.isInt())))
			n.setType(typeOfRightExp);
		else if ((operation == Kind.AND || operation == Kind.OR)
				&& ((typeOfLeftExp.isBoolean() && typeOfRightExp.isBoolean())))
			n.setType(PrimitiveType__.booleanType);
		else
			check(false, n, "Not a valid Binary Expression");

		return n.getType();
	}

	/**
	 * arg is enclosing function declaration
	 */
	@Override
	public Object visitIBlock(IBlock n, Object arg) throws Exception {
		List<IStatement> statements = n.getStatements();
		for (IStatement statement : statements) {
			statement.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitIBooleanLiteralExpression(IBooleanLiteralExpression n, Object arg) throws Exception {
		n.setType(PrimitiveType__.booleanType);
		
		return n.getType();
	}

	@Override
	public Object visitIFunctionDeclaration(IFunctionDeclaration n, Object arg) throws Exception {
		String name = n.getName().getName();
		IType resultType = n.getResultType();
		if (resultType != null) {
			resultType.visit(this, null);
			check(fullyTyped(resultType), n, "result type cannot be partially defined");
		} else {
			n.setType(Type__.voidType);
		}
		check(symtab.insert(name, n), n, name + " already declared in scope");
		symtab.enterScope();
		for (INameDef argument : n.getArgs()) {
			IType argType = (IType) argument.visit(this, argument);
			check(fullyTyped(argType), argument, "Type of argument must be defined");
		}
		n.getBlock().visit(this, n);
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitIFunctionCallExpression(IFunctionCallExpression n, Object arg) throws Exception {
		IIdentifier name = n.getName();
		IDeclaration dec = (IDeclaration) name.visit(this, null);
		check(dec instanceof IFunctionDeclaration, n, name.getName() + " is not declared or is not a function");
		IFunctionDeclaration fdec = (IFunctionDeclaration) dec;
		List<INameDef> formalArgDecs = fdec.getArgs();
		List<IExpression> actualArgs = n.getArgs();
		check(formalArgDecs.size() == actualArgs.size(), n, "formal and actual parameter lists have different lengths");
		for (int i = 0; i < actualArgs.size(); i++) {
			IType actualType = (IType) actualArgs.get(i).visit(this, arg);
			IType formalType = formalArgDecs.get(i).getType();
			check(compatibleAssignmentTypes(formalType, actualType), actualArgs.get(i),
					"types of actual and formal parameter " + formalArgDecs.get(i).getIdent().getName()
							+ "are inconsistent");
		}
		IType resultType = fdec.getResultType();
		n.setType(resultType);
		return resultType;
	}

	@Override
	public Object visitIIdentExpression(IIdentExpression n, Object arg) throws Exception {
		IIdentifier name = n.getName();
		IDeclaration dec = (IDeclaration) name.visit(this, null);
		IType type = getType(dec);
		check(type != Type__.undefinedType, n, "Identifier " + name + " does not have defined type");
		n.setType(type);
		return type;
	}

	@Override
	public Object visitIIfStatement(IIfStatement n, Object arg) throws Exception {
		IExpression guardExp = n.getGuardExpression();
		IType guardType = (IType) guardExp.visit(this, arg);
		
		check(guardType.isBoolean(), n, "Given exp type is not boolean");
		
		IBlock block = n.getBlock();
		block.visit(this, arg);
		
		return arg;
	}

	@Override
	public Object visitIImmutableGlobal(IImmutableGlobal n, Object arg) throws Exception {
		IExpression expression = n.getExpression(); // IIMutableGlobal must have initalizing expression
		IType expressionType = (IType) expression.visit(this, arg);
		INameDef nameDef = n.getVarDef();
		IType declaredType = (IType) nameDef.visit(this, n);
		IType inferredType = unifyAndCheck(declaredType, expressionType, n);
		nameDef.setType(inferredType);
		return null;
	}

	@Override
	public Object visitIIntLiteralExpression(IIntLiteralExpression n, Object arg) throws Exception {
		IType type = PrimitiveType__.intType;
		n.setType(type);
		return type;
	}

	/**
	 * arg is enclosing Function declaration
	 */
	@Override
	public Object visitILetStatement(ILetStatement n, Object arg) throws Exception {
		IExpression exp = n.getExpression();
		IType expType = exp != null ? (IType) exp.visit(this, arg) : Type__.undefinedType;
		
		INameDef nameDef = n.getLocalDef();
		
		check(symtab.insert(nameDef.getIdent().getName(), nameDef), n,
				nameDef.getIdent().getName() + " given name is already declared!");
		
		symtab.enterScope();
		
		//IType tempType = unifyAndCheck(nameDef.getType(), expType, n);
		//nameDef.setType(tempType);
		
		nameDef.setType(expType);
		IBlock block = n.getBlock();
		block.visit(this, arg);
		
		symtab.leaveScope();
		
		return null;
	}

	@Override
	public Object visitIListSelectorExpression(IListSelectorExpression n, Object arg) throws Exception {
		IDeclaration dec = (IDeclaration) n.getName().visit(this, arg);
		IType type = getType(dec);
		check(type.isList(), n, "invalid or missing type for list ");
		IListType ltype = (IListType) type;
		IType indexType = (IType) n.getIndex().visit(this, null);
		check(indexType.isInt(), n, "index selector must be int");
		IType elementType = ltype.getElementType();
		n.setType(elementType);
		return elementType;
	}

	IType getType(IDeclaration dec) {
		if (dec == null) {
			return null;
		}
		if (dec instanceof IMutableGlobal mg) {
			return mg.getVarDef().getType();
		}
		if (dec instanceof IImmutableGlobal mg) {
			return mg.getVarDef().getType();
		}
		if (dec instanceof INameDef nd) {
			return nd.getType();
		}
		if (dec instanceof IFunctionDeclaration fd) {
			return fd.getResultType();
		}
		return null;
	}

	@Override
	public Object visitIListType(IListType n, Object arg) throws Exception {
		IType elementType = n.getElementType();
		IType inferredElemType = elementType != null ? (IType) elementType.visit(this, null) : Type__.undefinedType;
		return new ListType__(n.getLine(), n.getPosInLine(), "", inferredElemType);
	}

	/**
	 * The enclosing declaration should be passed in as argument. If this is an
	 * IMutableGlobal or IImmutableGlobal, the declaration object is is inserted
	 * into the symbol table as the declaration. If the enclosing declaration is a
	 * Function declaration (and this is a formal parameter) or a Let statement (and
	 * this is a local variable declaration), then this NameDef object is the
	 * Declaration in the symbol table.
	 */
	@Override
	public Object visitINameDef(INameDef n, Object arg) throws Exception {
		String name = n.getIdent().getName();
		IType type = (IType) n.getType();
		IType varType = type != null ? (IType) type.visit(this, null) : Type__.undefinedType;
		if (arg instanceof IMutableGlobal || arg instanceof IImmutableGlobal) {
			check(symtab.insert(name, (IDeclaration) arg), n, "Variable " + name + "already declared in this scope");
		} else {
			check(symtab.insert(name, n), n, "Variable " + name + "already declared in this scope");
		}
		return varType;
	}

	@Override
	public Object visitINilConstantExpression(INilConstantExpression n, Object arg) throws Exception {
		n.setType(Type__.nilType);
		return n.getType();
	}

	@Override
	public Object visitIProgram(IProgram n, Object arg) throws Exception {
		List<IDeclaration> decs = n.getDeclarations();
		for (IDeclaration dec : decs) {
			dec.visit(this, symtab);
		}
		return n;
	}

	/**
	 * arg is enclosing function definition
	 */
	@Override
	public Object visitIReturnStatement(IReturnStatement n, Object arg) throws Exception {

		IExpression exp = n.getExpression();
		IType expType = (IType) exp.visit(this, arg);
		
		IFunctionDeclaration funcDeclaration = (IFunctionDeclaration) arg;
		IType funcType = funcDeclaration.getResultType();

		if (!compatibleAssignmentTypes(expType, funcType)) {
			check(false, n, "Incompatible result types!");
		}

		return arg;
	}

	@Override
	public Object visitIStringLiteralExpression(IStringLiteralExpression n, Object arg) throws Exception {
		IType type = PrimitiveType__.stringType;
		n.setType(type);
		return type;
	}

	boolean compatibleAssignmentTypes(IType declared, IType actual) {
		if (declared instanceof IListType && actual instanceof INilConstantExpression) {
			return true;
		}
		if (declared instanceof IListType && actual instanceof IListType) {
			return compatibleAssignmentTypes(((IListType) declared).getElementType(),
					((IListType) actual).getElementType());
		}
		return declared.equals(actual);
	}

	boolean isConstantExpression(IExpression expression) {
		if (expression instanceof IIdentExpression e) {
			String name = ((IIdentExpression) expression).getName().getName();
			IDeclaration dec = symtab.lookupDec(name);
			return !isMutable(dec);
		} else
			return (expression instanceof IBooleanLiteralExpression) || (expression instanceof IIntLiteralExpression)
					|| (expression instanceof IStringLiteralExpression)
					|| (expression instanceof INilConstantExpression);
	}

	/**
	 * arg is enclosing function definition
	 */
	@Override
	public Object visitISwitchStatement(ISwitchStatement n, Object arg) throws Exception {
		IExpression switchExpr = n.getSwitchExpression();
		IType switchExpType = (IType) switchExpr.visit(this, arg);
		
		check(switchExpType.isBoolean() || switchExpType.isInt() || switchExpType.isString(), n,
				"Given expression type does not match with either of int, string or boolean!");

		List<IExpression> branchExprs = n.getBranchExpressions();
		
		for (IExpression branchExp : branchExprs) {
			IType branchExpType = (IType) branchExp.visit(this, arg);
			check(switchExpType.equals(branchExpType), n, "Given expression type mismatched!");
			check(isConstantExpression(branchExp),n,branchExp + "is not a constant");
		}

		List<IBlock> blocks = n.getBlocks();

		for (IBlock block : blocks) {
			block.visit(this, arg);
		}

		n.getDefaultBlock().visit(this, arg);
		
		return arg;
	}

	@Override
	public Object visitIUnaryExpression(IUnaryExpression n, Object arg) throws Exception {
		IExpression e = n.getExpression();
		IType eType = (IType) e.visit(this, arg);
		Kind op = n.getOp();
		if (op == Kind.MINUS && eType.isInt()) {
			n.setType(PrimitiveType__.intType);
		} else if (op == Kind.MINUS && eType.isList()) {
			// tail of list, result is same type of list as argument
			n.setType(eType);
		} else if (op == Kind.BANG && eType.isBoolean()) {
			n.setType(PrimitiveType__.booleanType);
		} else if (op == Kind.BANG && eType.isList()) {
			IListType listType = (IListType) eType;
			IType elementType = listType.getElementType();
			n.setType(elementType);
		} else {
			// not a legal case
			check(false, n, "Illegal operator or expression type in unary expression");
		}
		return n.getType();

	}

	/**
	 * arg is enclosing function declaration
	 */
	@Override
	public Object visitIWhileStatement(IWhileStatement n, Object arg) throws Exception {
		IExpression guard = n.getGuardExpression();
		IType guardType = (IType) guard.visit(this, arg);
		check(guardType.isBoolean(), n, "Guard expression type not boolean");
		IBlock block = n.getBlock();
		block.visit(this, arg);
		return arg;
	}

	@Override
	public Object visitIMutableGlobal(IMutableGlobal n, Object arg) throws Exception {
		IExpression expression = n.getExpression();
		IType expressionType = expression != null ? (IType) expression.visit(this, arg) : Type__.undefinedType;
		INameDef def = n.getVarDef();
		IType declaredType = (IType) def.visit(this, n);
		IType inferredType = unifyAndCheck(declaredType, expressionType, n);
		def.setType(inferredType);
		return null;
	}

	IType unifyAndCheck(IType declaredType, IType expressionType, IASTNode n) throws TypeCheckException {
		boolean fullyDefinedExpr = fullyTyped(expressionType);
		boolean fullyDefinedDec = fullyTyped(declaredType);
		IType inferredType;
		check(fullyDefinedDec || fullyDefinedExpr, n, "type must be inferrable from declaration or initializer");
		if (fullyDefinedDec && fullyDefinedExpr) {
			check(compatibleAssignmentTypes(declaredType, expressionType), n, "incompatible types in declaration");
			inferredType = declaredType;
		} else if (expressionType.equals(Type__.undefinedType)) {
			inferredType = declaredType;
		} else if (expressionType.equals(Type__.nilType)) {
			check(declaredType.isList(), n, "NIL is not compatible with declared type");
			inferredType = declaredType;
		} else if (declaredType.equals(Type__.undefinedType)) {
			inferredType = expressionType;
		} else {
			check(declaredType.isList(), n, "invalid type");
			check(expressionType.isList(), expressionType, "incompatible expression type");
			IListType d = (IListType) declaredType;
			IType e = expressionType;
			while (!d.getElementType().equals(Type__.undefinedType)) {
				d = (IListType) d.getElementType();
				check(e.isList(), expressionType, "incompatible expression type");
				e = ((IListType) e).getElementType();
			}
			inferredType = e;
		}
		return inferredType;
	}

	boolean fullyTyped(IType type) {
		if (type.equals(Type__.undefinedType) || type.equals(Type__.nilType)) {
			return false;
		}
		if (type.isList()) {
			IType t = type;
			while (t instanceof IListType) {
				t = ((IListType) t).getElementType();
				if (t.equals(Type__.undefinedType)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public Object visitIPrimitiveType(IPrimitiveType n, Object arg) throws Exception {
		return n;
	}

	@Override
	public Object visitIAssignmentStatement(IAssignmentStatement n, Object arg) throws Exception {
		IExpression leftExpression = n.getLeft();
		IType leftType = (IType) leftExpression.visit(this, arg);
		IExpression rightExpression = n.getRight();
		if (leftExpression instanceof IFunctionCallExpression) {
			check(rightExpression == null, n, "cannot assign to function");
		} else {
			check(isMutable(leftExpression), n, "attempting to assign to immutable variable");
		}
		if (rightExpression != null) {
			IType rightType = (IType) rightExpression.visit(this, arg);
			check(compatibleAssignmentTypes(leftType, rightType), n, "incompatible types in assignment statement");
		}
		return arg;
	}

	private boolean isMutable(IExpression expression) {
		if (expression instanceof IIdentExpression e) {
			String name = e.getName().getName();
			IDeclaration dec = symtab.lookupDec(name);
			return isMutable(dec);
		}
		if (expression instanceof IListSelectorExpression e) {
			String name = e.getName().getName();
			IDeclaration dec = symtab.lookupDec(name);
			return isMutable(dec);
		}
		return false;
	}

	boolean isMutable(IDeclaration dec) {
		return (dec instanceof IMutableGlobal || dec instanceof INameDef);
	}

	@Override
	public Object visitIIdentifier(IIdentifier n, Object arg) throws Exception {
		String name = n.getName();
		IDeclaration dec = symtab.lookupDec(name);
		check(dec != null, n, "identifier not declared");
		/*
		 * Setting Declaration for assignment 6
		 */
		n.setDec(dec);
		return dec;
	}

	@Override
	public Object visitIExpressionStatement(IExpressionStatement n, Object arg) throws Exception {
		// TODO Auto-generated method stub
		/*
		 * The Following method will not be used.
		 */
		return null;
	}

}
