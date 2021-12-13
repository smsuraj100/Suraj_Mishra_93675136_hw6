package edu.ufl.cise.plpfa21.assignment2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.ufl.cise.plpfa21.assignment1.CompilerComponentFactory;
import edu.ufl.cise.plpfa21.assignment1.IPLPLexer;
import edu.ufl.cise.plpfa21.assignment1.IPLPToken;
import edu.ufl.cise.plpfa21.assignment1.PLPTokenKinds.Kind;
import edu.ufl.cise.plpfa21.assignment3.ast.IASTNode;
import edu.ufl.cise.plpfa21.assignment3.ast.IBinaryExpression;
import edu.ufl.cise.plpfa21.assignment3.ast.IBlock;
import edu.ufl.cise.plpfa21.assignment3.ast.IDeclaration;
import edu.ufl.cise.plpfa21.assignment3.ast.IExpression;
import edu.ufl.cise.plpfa21.assignment3.ast.IFunctionDeclaration;
import edu.ufl.cise.plpfa21.assignment3.ast.IIdentifier;
import edu.ufl.cise.plpfa21.assignment3.ast.IImmutableGlobal;
import edu.ufl.cise.plpfa21.assignment3.ast.IMutableGlobal;
import edu.ufl.cise.plpfa21.assignment3.ast.INameDef;
import edu.ufl.cise.plpfa21.assignment3.ast.IProgram;
import edu.ufl.cise.plpfa21.assignment3.ast.IStatement;
import edu.ufl.cise.plpfa21.assignment3.ast.IType;
import edu.ufl.cise.plpfa21.assignment3.ast.IType.TypeKind;
import edu.ufl.cise.plpfa21.assignment3.ast.IUnaryExpression;
import edu.ufl.cise.plpfa21.assignment3.astimpl.AssignmentStatement__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.BinaryExpression__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.Block__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.BooleanLiteralExpression__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.FunctionCallExpression__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.FunctionDeclaration___;
import edu.ufl.cise.plpfa21.assignment3.astimpl.IdentExpression__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.Identifier__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.IfStatement__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.ImmutableGlobal__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.IntLiteralExpression__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.LetStatement__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.ListSelectorExpression__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.ListType__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.MutableGlobal__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.NameDef__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.NilConstantExpression__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.PrimitiveType__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.Program__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.ReturnStatement__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.StringLiteralExpression__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.SwitchStatement__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.UnaryExpression__;
import edu.ufl.cise.plpfa21.assignment3.astimpl.WhileStatement__;

public class PLPParser implements IPLPParser {

	String input;
	int currentIndex = 0;

	List<IPLPToken> tokenList = new LinkedList<IPLPToken>();

	public PLPParser(String input) {
// TODO Auto-generated constructor stub
		this.input = input;
	}

	@Override
	public IASTNode parse() throws Exception {

		/*
		 * Created Token List
		 */
		IPLPLexer lexer = CompilerComponentFactory.getLexer(input);

		IPLPToken tempToken = lexer.nextToken();
		while (tempToken.getKind() != Kind.EOF) {
			tokenList.add(tempToken);
			tempToken = lexer.nextToken();
		}
		tokenList.add(tempToken);

		IProgram program = isProgram();

		return program;

	}

	private IProgram isProgram() throws SyntaxException {

		IProgram program = null;
		int valCurrentIndex = currentIndex;
		List<IDeclaration> declarationList = new ArrayList<IDeclaration>();
		while (tokenList.get(currentIndex).getKind() != Kind.EOF) {
			IDeclaration declaration = isDeclaration();
			declarationList.add(declaration);
		}
		program = new Program__(tokenList.get(valCurrentIndex).getLine(),
				tokenList.get(valCurrentIndex).getCharPositionInLine(), tokenList.get(valCurrentIndex).getText(),
				declarationList);
		return program;
	}

	private IDeclaration isDeclaration() throws SyntaxException {
		/*
		 * Parsing Logic
		 */
		IDeclaration declaration = null;
		if (isOfKind(tokenList.get(currentIndex), Kind.KW_FUN)) {
			declaration = handleFunc();
		} else if (isOfKind(tokenList.get(currentIndex), Kind.KW_VAL)) {
			declaration = handleVal();
		} else if (isOfKind(tokenList.get(currentIndex), Kind.KW_VAR)) {
			declaration = handleVar();
		}
		return declaration;
	}

// Method to handle FUN declaration
	private IFunctionDeclaration handleFunc() throws SyntaxException {
		List<INameDef> functionArgs = new ArrayList<INameDef>();
		INameDef nameDef = null;
		IType type = null;
		List<IStatement> listOfStatements = new ArrayList<IStatement>();
		IStatement stmt = null;
		IBlock block = null;
		IIdentifier iden = null;
		IFunctionDeclaration funcDeclaration = null;
		int funDeclarationStartIndex = currentIndex;
		currentIndex++;
		if (isOfKind(tokenList.get(currentIndex), Kind.IDENTIFIER)) {
			iden = isIdentifier();
			currentIndex++;
			if (isOfKind(tokenList.get(currentIndex), Kind.LPAREN)) {
				if (isOfKind(tokenList.get(currentIndex + 1), Kind.RPAREN)) {
					currentIndex = currentIndex + 2;
				} else {
					currentIndex++;
					nameDef = isNameDef();
					functionArgs.add(nameDef);
//					currentIndex++;
					while (isOfKind(tokenList.get(currentIndex), Kind.COMMA)) {
						currentIndex++;
						nameDef = isNameDef();
						functionArgs.add(nameDef);
//						currentIndex++;
					}
					if (isOfKind(tokenList.get(currentIndex), Kind.RPAREN)) {
						currentIndex++;
					} else {
						throw new SyntaxException("Missing RPAREN", tokenList.get(currentIndex).getLine(),
								tokenList.get(currentIndex).getCharPositionInLine());
					}

				}
				if (isOfKind(tokenList.get(currentIndex), Kind.COLON)) {
					currentIndex++;
					type = getTokenType();
//					currentIndex++;
				}

				if (isOfKind(tokenList.get(currentIndex), Kind.KW_DO)) {
					currentIndex++;
					int blockStartIndex = currentIndex;
					while (tokenList.get(currentIndex).getKind() != Kind.KW_END) {
						stmt = handleStatement();
						listOfStatements.add(stmt);
					}
					if (isOfKind(tokenList.get(currentIndex), Kind.KW_END)) {
						block = new Block__(tokenList.get(blockStartIndex).getLine(),
								tokenList.get(blockStartIndex).getCharPositionInLine(),
								tokenList.get(blockStartIndex).getText(), listOfStatements);

						funcDeclaration = new FunctionDeclaration___(tokenList.get(funDeclarationStartIndex).getLine(),
								tokenList.get(funDeclarationStartIndex).getCharPositionInLine(),
								tokenList.get(funDeclarationStartIndex).getText(), iden, functionArgs, type, block);
						currentIndex++;
						return funcDeclaration;
					} else {
						throw new SyntaxException("END keyword not present", tokenList.get(currentIndex).getLine(),
								tokenList.get(currentIndex).getCharPositionInLine());
					}
				} else {
					throw new SyntaxException("DO keyword not present", tokenList.get(currentIndex).getLine(),
							tokenList.get(currentIndex).getCharPositionInLine());
				}

			}
		} else {
			throw new SyntaxException("Identifier exception", tokenList.get(currentIndex).getLine(),
					tokenList.get(currentIndex).getCharPositionInLine());
		}
		return funcDeclaration;
	}

// Method to handle VAL declaration
	private IImmutableGlobal handleVal() throws SyntaxException {
		int startIndex = currentIndex;
		IImmutableGlobal immutableGlobal = null;
		IExpression exp = null;
		currentIndex++;

		INameDef nameDef = isNameDef();
//currentIndex++;
		if (isAssign()) {
			currentIndex++;
			exp = isExpression();
			if (isOfKind(tokenList.get(currentIndex), Kind.SEMI)) {
				currentIndex++;
				immutableGlobal = new ImmutableGlobal__(tokenList.get(startIndex).getLine(),
						tokenList.get(startIndex).getCharPositionInLine(), tokenList.get(startIndex).getText(), nameDef,
						exp);
				return immutableGlobal;
			} else {
				throw new SyntaxException("Semi colon not present", tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine());
			}
		} else {
			throw new SyntaxException("Assign operator not present", tokenList.get(currentIndex).getLine(),
					tokenList.get(currentIndex).getCharPositionInLine());
		}
	}

// Method to handle VAR declaration
	private IMutableGlobal handleVar() throws SyntaxException {
		int startIndex = currentIndex;
		IMutableGlobal mutableGlobal = null;
		IExpression exp = null;
		INameDef nameDef = null;
		currentIndex++;
		nameDef = isNameDef();
// currentIndex++;
		if (isOfKind(tokenList.get(currentIndex), Kind.ASSIGN)) {
			currentIndex++;
			exp = isExpression();
			if (isOfKind(tokenList.get(currentIndex), Kind.SEMI)) {
				currentIndex++;
// return null;
				mutableGlobal = new MutableGlobal__(tokenList.get(startIndex).getLine(),
						tokenList.get(startIndex).getCharPositionInLine(), tokenList.get(startIndex).getText(), nameDef,
						exp);
				return mutableGlobal;
			} else {
				throw new SyntaxException("Semi colon not present", tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine());
			}
		} else {
			if (isOfKind(tokenList.get(currentIndex), Kind.SEMI)) {
				currentIndex++;
				mutableGlobal = new MutableGlobal__(tokenList.get(startIndex).getLine(),
						tokenList.get(startIndex).getCharPositionInLine(), tokenList.get(startIndex).getText(), nameDef,
						exp);
				return mutableGlobal;
			} else {
				throw new SyntaxException("Semi colon not present", tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine());
			}
		}

	}

	private IStatement handleStatement() throws SyntaxException {
		if (isOfKind(tokenList.get(currentIndex), Kind.KW_LET)) {
			List<IStatement> listOfStatements = new ArrayList<IStatement>();
			IBlock block = null;
			INameDef nameDef = null;
			IStatement stmt = null;
			IExpression exp = null;

			currentIndex++;
			nameDef = isNameDef();
			// currentIndex++;
			if (isAssign()) {
				currentIndex++;
				exp = isExpression();
			}

			if (isOfKind(tokenList.get(currentIndex), Kind.KW_DO)) {
				currentIndex++;
				while (tokenList.get(currentIndex).getKind() != Kind.KW_END) {
					stmt = handleStatement();
					listOfStatements.add(stmt);
				}
				block = new Block__(tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(),
						listOfStatements);
				if (isOfKind(tokenList.get(currentIndex), Kind.KW_END)) {
					currentIndex++;
					stmt = new LetStatement__(tokenList.get(currentIndex).getLine(),
							tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(),
							block, exp, nameDef);
					return stmt;
				} else {
					throw new SyntaxException("END keyword not present", tokenList.get(currentIndex).getLine(),
							tokenList.get(currentIndex).getCharPositionInLine());
				}

			} else {
				throw new SyntaxException("DO keyword not present", tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine());
			}

		} else if (isOfKind(tokenList.get(currentIndex), Kind.KW_SWITCH)) {
			List<IBlock> caseBlockList = new ArrayList<IBlock>();
			List<IExpression> caseExpList = new ArrayList<IExpression>();

			IBlock block = null;
			IBlock defaultBlock = null;
			IStatement switchStmt = null;
			IStatement stmt = null;
			IExpression switchExp = null;
			IExpression caseExp = null;

			currentIndex++;
			int switchStatementStartIndex = currentIndex;
			switchExp = isExpression();

			while (isOfKind(tokenList.get(currentIndex), Kind.KW_CASE)) {
				List<IStatement> listOfStatements = new ArrayList<IStatement>();
				currentIndex++;
				caseExp = isExpression();
				caseExpList.add(caseExp);
				if (isOfKind(tokenList.get(currentIndex), Kind.COLON)) {
					currentIndex++;
					while (tokenList.get(currentIndex).getKind() != Kind.KW_CASE
							&& tokenList.get(currentIndex).getKind() != Kind.KW_DEFAULT) {
						stmt = handleStatement();
						listOfStatements.add(stmt);
					}
					block = new Block__(tokenList.get(currentIndex).getLine(),
							tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(),
							listOfStatements);
					caseBlockList.add(block);
				} else {
					throw new SyntaxException("Colon not present", tokenList.get(currentIndex).getLine(),
							tokenList.get(currentIndex).getCharPositionInLine());
				}
			}
			if (isOfKind(tokenList.get(currentIndex), Kind.KW_DEFAULT)) {
				List<IStatement> listOfStatements = new ArrayList<IStatement>();
				currentIndex++;
				while (tokenList.get(currentIndex).getKind() != Kind.KW_END) {
					stmt = handleStatement();
					listOfStatements.add(stmt);
				}
				defaultBlock = new Block__(tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(),
						listOfStatements);
				if (isOfKind(tokenList.get(currentIndex), Kind.KW_END)) {
					currentIndex++;
					switchStmt = new SwitchStatement__(tokenList.get(switchStatementStartIndex).getLine(),
							tokenList.get(switchStatementStartIndex).getCharPositionInLine(),
							tokenList.get(switchStatementStartIndex).getText(), switchExp, caseExpList, caseBlockList,
							defaultBlock);
					return switchStmt;
				} else {
					throw new SyntaxException("END keyword not present", tokenList.get(currentIndex).getLine(),
							tokenList.get(currentIndex).getCharPositionInLine());
				}

			}
		} else if (isOfKind(tokenList.get(currentIndex), Kind.KW_IF)) {
			List<IStatement> listOfStatements = new ArrayList<IStatement>();

			IStatement stmt = null;
			IBlock block = null;
			IExpression exp = null;
			IStatement ifStmt = null;

			currentIndex++;
			exp = isExpression();
			if (isOfKind(tokenList.get(currentIndex), Kind.KW_DO)) {
				currentIndex++;
				while (tokenList.get(currentIndex).getKind() != Kind.KW_END) {
					stmt = handleStatement();
					listOfStatements.add(stmt);
				}
				block = new Block__(tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(),
						listOfStatements);
				if (isOfKind(tokenList.get(currentIndex), Kind.KW_END)) {
					currentIndex++;
					ifStmt = new IfStatement__(tokenList.get(currentIndex).getLine(),
							tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(),
							exp, block);
					return ifStmt;
				} else {
					throw new SyntaxException("END keyword not present", tokenList.get(currentIndex).getLine(),
							tokenList.get(currentIndex).getCharPositionInLine());
				}

			} else {
				throw new SyntaxException("DO keyword not present", tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine());
			}

		} else if (isOfKind(tokenList.get(currentIndex), Kind.KW_WHILE)) {
			List<IStatement> listOfStatements = new ArrayList<IStatement>();

			IStatement stmt = null;
			IBlock block = null;
			IExpression exp = null;
			IStatement whileStmt = null;

			currentIndex++;
			exp = isExpression();
			if (isOfKind(tokenList.get(currentIndex), Kind.KW_DO)) {
				currentIndex++;
				while (tokenList.get(currentIndex).getKind() != Kind.KW_END) {
					stmt = handleStatement();
					listOfStatements.add(stmt);
				}
				block = new Block__(tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(),
						listOfStatements);
				if (isOfKind(tokenList.get(currentIndex), Kind.KW_END)) {
					currentIndex++;
					whileStmt = new WhileStatement__(tokenList.get(currentIndex).getLine(),
							tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(),
							exp, block);
					return whileStmt;
				} else {
					throw new SyntaxException("END keyword not present", tokenList.get(currentIndex).getLine(),
							tokenList.get(currentIndex).getCharPositionInLine());
				}

			} else {
				throw new SyntaxException("DO keyword not present", tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine());
			}

		} else if (isOfKind(tokenList.get(currentIndex), Kind.KW_RETURN)) {
			IExpression exp = null;
			IStatement returnStmt = null;

			currentIndex++;
			int returnStatementStartIndex = currentIndex;
			exp = isExpression();
			if (isOfKind(tokenList.get(currentIndex), Kind.SEMI)) {
				currentIndex++;
				returnStmt = new ReturnStatement__(tokenList.get(returnStatementStartIndex).getLine(),
						tokenList.get(returnStatementStartIndex).getCharPositionInLine(),
						tokenList.get(returnStatementStartIndex).getText(), exp);
				return returnStmt;
			} else {
				throw new SyntaxException("Semi colon not present", tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine());
			}
		} else {
			IExpression leftExp, rightExp = null;
			IStatement assignStmt = null;

			int assignmentStatementIndex = currentIndex;
			leftExp = isExpression();
			if (isAssign()) {
				currentIndex++;
				rightExp = isExpression();
				if (isOfKind(tokenList.get(currentIndex), Kind.SEMI)) {
					currentIndex++;
					assignStmt = new AssignmentStatement__(tokenList.get(assignmentStatementIndex).getLine(),
							tokenList.get(assignmentStatementIndex).getCharPositionInLine(),
							tokenList.get(assignmentStatementIndex).getText(), leftExp, rightExp);
					return assignStmt;
				} else {
					throw new SyntaxException("Semi colon not present", tokenList.get(currentIndex).getLine(),
							tokenList.get(currentIndex).getCharPositionInLine());
				}
			}
			if (isOfKind(tokenList.get(currentIndex), Kind.SEMI)) {
				currentIndex++;
				assignStmt = new AssignmentStatement__(tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(),
						leftExp, null);
				return assignStmt;
			} else {
				throw new SyntaxException("Semi colon not present", tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine());
			}

		}

		return null;
	}

	private IType getTokenType() throws SyntaxException {
		IType typeOfToken = null;
		IPLPToken currentToken = tokenList.get(currentIndex);

		if (isOfKind(currentToken, Kind.KW_INT)) {
			typeOfToken = new PrimitiveType__(currentToken.getLine(), currentToken.getCharPositionInLine(),
					currentToken.getText(), TypeKind.INT);
			currentIndex++;
		} else if (isOfKind(currentToken, Kind.KW_STRING)) {
			typeOfToken = new PrimitiveType__(currentToken.getLine(), currentToken.getCharPositionInLine(),
					currentToken.getText(), TypeKind.STRING);
			currentIndex++;
		} else if (isOfKind(currentToken, Kind.KW_BOOLEAN)) {
			typeOfToken = new PrimitiveType__(currentToken.getLine(), currentToken.getCharPositionInLine(),
					currentToken.getText(), TypeKind.BOOLEAN);
			currentIndex++;
		} else if (isOfKind(currentToken, Kind.KW_LIST)) {
			if (isOfKind(tokenList.get(currentIndex + 1), Kind.LSQUARE)) {
				currentIndex = currentIndex + 2;
				if (isOfKind(tokenList.get(currentIndex), Kind.RSQUARE)) {
					currentIndex++;
					typeOfToken = new ListType__(currentToken.getLine(), currentToken.getCharPositionInLine(),
							currentToken.getText(), null);
				} else {
					typeOfToken = new ListType__(currentToken.getLine(), currentToken.getCharPositionInLine(),
							currentToken.getText(), getTokenType());

					if (tokenList.get(currentIndex).getKind() != Kind.RSQUARE) {
						throw new SyntaxException("RSQUARE not present", tokenList.get(currentIndex).getLine(),
								tokenList.get(currentIndex).getCharPositionInLine());
					} else
						currentIndex++;
				}
			} else {
				throw new SyntaxException("LSQUARE not present", tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine());
			}
		}
		return typeOfToken;
	}

	private IExpression isExpression() throws SyntaxException {

		IExpression leftExp, rightExp = null;
		IBinaryExpression binaryExp = null;
		Kind operation = null;

		leftExp = isComparisionExpression();
		while (isOfKind(tokenList.get(currentIndex), Kind.AND) || isOfKind(tokenList.get(currentIndex), Kind.OR)) {

			operation = tokenList.get(currentIndex).getKind();
			currentIndex++;
			rightExp = isComparisionExpression();
		}

		if (operation == null) {
			return leftExp;
		}

		binaryExp = new BinaryExpression__(tokenList.get(currentIndex).getLine(),
				tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(), leftExp,
				rightExp, operation);

		return binaryExp;
	}

	private IExpression isComparisionExpression() throws SyntaxException {
		IExpression leftExp, rightExp = null;
		IBinaryExpression binaryExp = null;
		Kind operation = null;

		leftExp = isAdditivieExpression();
		while (isOfKind(tokenList.get(currentIndex), Kind.LT) || isOfKind(tokenList.get(currentIndex), Kind.GT)
				|| isOfKind(tokenList.get(currentIndex), Kind.EQUALS)
				|| isOfKind(tokenList.get(currentIndex), Kind.NOT_EQUALS)) {
			operation = tokenList.get(currentIndex).getKind();
			currentIndex++;
			rightExp = isAdditivieExpression();
		}

		if (operation == null) {
			return leftExp;
		}

		binaryExp = new BinaryExpression__(tokenList.get(currentIndex).getLine(),
				tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(), leftExp,
				rightExp, operation);

		return binaryExp;
	}

	private IExpression isAdditivieExpression() throws SyntaxException {
		IExpression leftExp, rightExp = null;
		IBinaryExpression binaryExp = null;
		Kind operation = null;

		leftExp = isMultiplicativeExpression();

		while (isOfKind(tokenList.get(currentIndex), Kind.PLUS) || isOfKind(tokenList.get(currentIndex), Kind.MINUS)) {
			operation = tokenList.get(currentIndex).getKind();
			currentIndex++;
			rightExp = isMultiplicativeExpression();
		}

		if (operation == null) {
			return leftExp;
		}

		binaryExp = new BinaryExpression__(tokenList.get(currentIndex).getLine(),
				tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(), leftExp,
				rightExp, operation);

		return binaryExp;
	}

	private IExpression isMultiplicativeExpression() throws SyntaxException {
		IExpression leftExp, rightExp = null;
		IBinaryExpression binaryExp = null;
		Kind operation = null;

		leftExp = isUnaryExpression();
		while (isOfKind(tokenList.get(currentIndex), Kind.TIMES) || isOfKind(tokenList.get(currentIndex), Kind.DIV)) {
			operation = tokenList.get(currentIndex).getKind();
			currentIndex++;
			rightExp = isUnaryExpression();
		}

		if (operation == null) {
			return leftExp;
		}

		binaryExp = new BinaryExpression__(tokenList.get(currentIndex).getLine(),
				tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(), leftExp,
				rightExp, operation);

		return binaryExp;
	}

	private IExpression isUnaryExpression() throws SyntaxException {
		IExpression exp = null;
		IUnaryExpression unaryExp = null;
		Kind operation = null;

		if (isOfKind(tokenList.get(currentIndex), Kind.BANG) || isOfKind(tokenList.get(currentIndex), Kind.MINUS)) {
			operation = tokenList.get(currentIndex).getKind();
			currentIndex++;
			exp = isPrimaryExpression();
		} else {
			exp = isPrimaryExpression();
		}

		if (operation == null) {
			return exp;
		}

		unaryExp = new UnaryExpression__(tokenList.get(currentIndex).getLine(),
				tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(), exp,
				operation);

		return unaryExp;

	}

	private IExpression isPrimaryExpression() throws SyntaxException {
		IExpression exp = null;

		if (isOfKind(tokenList.get(currentIndex), Kind.KW_NIL)) {
			exp = new NilConstantExpression__(tokenList.get(currentIndex).getLine(),
					tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText());
			currentIndex++;
		} else if (isOfKind(tokenList.get(currentIndex), Kind.KW_TRUE)
				|| isOfKind(tokenList.get(currentIndex), Kind.KW_FALSE)) {
			boolean value = isOfKind(tokenList.get(currentIndex), Kind.KW_TRUE) ? true : false;
			exp = new BooleanLiteralExpression__(tokenList.get(currentIndex).getLine(),
					tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(), value);
			currentIndex++;
		} else if (isOfKind(tokenList.get(currentIndex), Kind.INT_LITERAL)) {
			exp = new IntLiteralExpression__(tokenList.get(currentIndex).getLine(),
					tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(),
					tokenList.get(currentIndex).getIntValue());
			currentIndex++;
		} else if (isOfKind(tokenList.get(currentIndex), Kind.STRING_LITERAL)) {
			exp = new StringLiteralExpression__(tokenList.get(currentIndex).getLine(),
					tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(),
					tokenList.get(currentIndex).getText().replaceAll("^\"|\"$", ""));
			currentIndex++;
		} else if (isOfKind(tokenList.get(currentIndex), Kind.LPAREN)) {
			currentIndex++;
			exp = isExpression();

			if (isOfKind(tokenList.get(currentIndex), Kind.RPAREN)) {
				currentIndex++;
			} else {
				throw new SyntaxException("RPAREN not found", tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine());
			}
		} else if (isOfKind(tokenList.get(currentIndex), Kind.IDENTIFIER) && tokenList.size() > currentIndex + 1
				&& isOfKind(tokenList.get(currentIndex + 1), Kind.LPAREN)) {
			List<IExpression> functionArgs = new ArrayList<IExpression>();
			IIdentifier name = isIdentifier();

			int tokenIndex = currentIndex;
			currentIndex = currentIndex + 2;

			if (tokenList.get(currentIndex).getKind() != Kind.RPAREN) {
				functionArgs.add(isExpression());
				while (tokenList.get(currentIndex).getKind() == Kind.COMMA) {
					currentIndex++;
					functionArgs.add(isExpression());
				}
			}

			exp = new FunctionCallExpression__(tokenList.get(tokenIndex).getLine(),
					tokenList.get(tokenIndex).getCharPositionInLine(), tokenList.get(tokenIndex).getText(), name,
					functionArgs);

			if (tokenList.get(currentIndex).getKind() != Kind.RPAREN)
				throw new SyntaxException("RPAREN not found", tokenList.get(tokenIndex).getLine(),
						tokenList.get(tokenIndex).getCharPositionInLine());
			else
				currentIndex++;
		} else if (isOfKind(tokenList.get(currentIndex), Kind.IDENTIFIER) && tokenList.size() > currentIndex + 1
				&& isOfKind(tokenList.get(currentIndex + 1), Kind.LSQUARE)) {

			int tokenIndex = currentIndex;
			IIdentifier name = isIdentifier();
			currentIndex += 2;
			IExpression listExp = isExpression();

			if (tokenList.get(currentIndex).getKind() != Kind.RSQUARE)
				throw new SyntaxException("RSQUARE not found", tokenList.get(currentIndex).getLine(),
						tokenList.get(currentIndex).getCharPositionInLine());
			else
				currentIndex++;

			exp = new ListSelectorExpression__(tokenList.get(tokenIndex).getLine(),
					tokenList.get(tokenIndex).getCharPositionInLine(), tokenList.get(tokenIndex).getText(), name,
					listExp);
		} else if (isOfKind(tokenList.get(currentIndex), Kind.IDENTIFIER)) {
			int tokenIndex = currentIndex;
			IIdentifier name = isIdentifier();
			currentIndex++;
			exp = new IdentExpression__(tokenList.get(tokenIndex).getLine(),
					tokenList.get(tokenIndex).getCharPositionInLine(), tokenList.get(tokenIndex).getText(), name);
		}

		return exp;
	}

	private boolean isAssign() {
		if (isOfKind(tokenList.get(currentIndex), Kind.ASSIGN))
			return true;
		else
			return false;
	}

	private INameDef isNameDef() throws SyntaxException {
		IIdentifier name = null;
		IType type = null;

		int tokenIndex = currentIndex;

		try {
			name = isIdentifier();
			currentIndex++;
			if (isOfKind(tokenList.get(currentIndex), Kind.COLON)) {
				currentIndex++;
				type = getTokenType();
			}

			INameDef nameDef = new NameDef__(tokenList.get(tokenIndex).getLine(),
					tokenList.get(tokenIndex).getCharPositionInLine(), tokenList.get(tokenIndex).getText(), name, type);
			return nameDef;
		} catch (SyntaxException e) {
			throw e;
		}
	}

	private IIdentifier isIdentifier() throws SyntaxException {
		IIdentifier identifier = null;

		if (isOfKind(tokenList.get(currentIndex), Kind.IDENTIFIER)) {
			identifier = new Identifier__(tokenList.get(currentIndex).getLine(),
					tokenList.get(currentIndex).getCharPositionInLine(), tokenList.get(currentIndex).getText(),
					tokenList.get(currentIndex).getText());
		} else {
			throw new SyntaxException("Could not find identifier", tokenList.get(currentIndex).getLine(),
					tokenList.get(currentIndex).getCharPositionInLine());
		}

		return identifier;

	}

	private boolean isOfKind(IPLPToken token, Kind kind) {
		if (token.getKind() == kind)
			return true;
		else
			return false;
	}

}