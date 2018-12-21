import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTreeProperty;



public class OptimizationCode extends MiniGoBaseListener{
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<>();
	int indentCount = 0;
	List<Variable> symbol_table = new ArrayList<>();
	String location = "global"; // 현재 위치

	// 보조 메소드
	boolean isBinaryOperation(MiniGoParser.ExprContext ctx) {
		return ctx.getChildCount() == 3 && ctx.getChild(1) != ctx.expr(0);
		// 자식 3개짜리 expr 중 '(' expr ')'를 배제
	}

	boolean isPreOperation(MiniGoParser.ExprContext ctx) {
		return ctx.getChildCount() == 2;
		// 전위 연산자 + expr
	}

	public static boolean isNumber(char firstLetter) {
		if (firstLetter == ' ') // 비어있을 때
			return false;

		if (firstLetter < '0' || firstLetter > '9')
			return false;

		return true;
	}

	String indent() { // 들여쓰기 메소드
		/* 블록이나 nesting 되어 들어갈 때는 4칸 들여쓰되 ‘.’을 찍음 */
		String Indent = "";
		for (int i = 0; i < indentCount; i++) {
			Indent += "    ";
		}
		return Indent;
	}

	private Variable lookup_Table(String lhs, String location) {
		for (int i = symbol_table.size() - 1; i >= 0; i--) {
			Variable cmp_var = symbol_table.get(i);
			if (cmp_var.lhs.equals(lhs) && cmp_var.location.equals(location)) {
				return symbol_table.get(i);
			} else {
				if (!location.equals("global")) {// global에서도 찾아보기
					if (cmp_var.lhs.equals(lhs)
							&& cmp_var.location.equals("global"))
						if (symbol_table.get(i) != null)
							return symbol_table.get(i);
				}
			}
		}
		return null;
	}
	
	private Variable lookupRhs_Table(String rhs, String location) {
		for (int i = symbol_table.size() - 1; i >= 0; i--) {
			Variable cmp_var = symbol_table.get(i);
			if (cmp_var.rhs != null) {
				if (cmp_var.rhs.equals(rhs)
						&& cmp_var.location.equals(location))
					return symbol_table.get(i);
				else {
					if (!location.equals("global")) {// global에서도 찾아보기
						if (cmp_var.rhs.equals(rhs)
								&& cmp_var.location.equals("global"))
							if (symbol_table.get(i) != null)
								return symbol_table.get(i);
					}
				}
			}
		}
		return null;
	}
	class Variable {
		String lhs;	//변수 이름
		String rhs; //변수 값
		boolean use = false; //변수 사용유무
		String location; //변수 위치

		public Variable(String lhs, String location) {
			this.lhs = lhs;
			this.location = location;
		}

		public Variable(String lhs, String rhs, String location) {
			this.lhs = lhs;
			this.rhs = rhs;
			this.location = location;
		}
	}
	private void declare_Table(String lhs, String location) {
		if (lookup_Table(lhs, location) == null) { // 테이블에 똑같은 변수가 없으면 insert
			Variable newVar = new Variable(lhs, location);
			symbol_table.add(newVar);
		}
	}
	private void define_Table(String lhs, String rhs, String location) {
		if (lookup_Table(lhs, location) == null) { // 테이블에 똑같은 변수가 없으면 insert
			Variable newVar = new Variable(lhs, rhs, location);
			symbol_table.add(newVar);
		} 
	}

	private void update_Table(String lhs, String rhs, String location) {
		boolean check = false;
		for (int i = symbol_table.size() - 1; i >= 0; i--) {
			Variable cmp_var = symbol_table.get(i);
			if (cmp_var.lhs.equals(lhs) && cmp_var.location.equals(location)) {
				symbol_table.get(i).rhs = rhs;
				check = true;
			} else {
				if (!location.equals("global")) { // local이면 local에 없을 때
					// global에서도 찾아보기
					if (cmp_var.lhs.equals(lhs)
							&& cmp_var.location.equals("global")) {
						symbol_table.get(i).rhs = rhs;
						check = true;
					}
				}
			}
		}
		if (check == false) {
			System.out.println("변수 " + lhs + " 없습니다");
		}
	}

	@Override
	public void exitProgram(MiniGoParser.ProgramContext ctx) {
		int i = 0;
		for (int j = symbol_table.size() - 1; j >= 0; j--) {
			if (!symbol_table.get(j).use)
				System.out.println("Warning : "
						+ symbol_table.get(j).location + "의 변수 "
						+ symbol_table.get(j).lhs + "는 쓰이지 않습니다!");
		}
		System.out.println();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("optimizedGo.go"));
			while (ctx.decl(i) != null) {
				String text= newTexts.get(ctx.decl(i));
				System.out.println(newTexts.get(ctx.decl(i)));
				writer.write(text+"\n");
				i++;
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void exitDecl(MiniGoParser.DeclContext ctx) {
		if (ctx.getChild(0) == ctx.var_decl()) { // var_decl
			newTexts.put(ctx, newTexts.get(ctx.var_decl()));
		} else { // fun_decl
			newTexts.put(ctx, newTexts.get(ctx.fun_decl()));
		}
	}
	@Override
	public void enterVar_decl(MiniGoParser.Var_declContext ctx) {
		/* symbol_table에 변수 넣어주기 */
			String varName = ctx.getChild(1).getText();
			if(ctx.getChildCount() == 3) {	//VAR IDENT type_spec
				declare_Table(varName, location);
			}
			else if( ctx.getChildCount() == 5) {	//VAR IDENT ',' IDENT type_spec
				declare_Table(varName, location);
				varName = ctx.getChild(3).getText();
				declare_Table(varName, location);
			}
			else if( ctx.getChildCount() == 6) {	//VAR IDENT '[' LITERAL ']' type_spec
				declare_Table(varName, location);
			}
	}
	
	@Override
	public void exitVar_decl(MiniGoParser.Var_declContext ctx) {
		if (ctx.getChildCount() == 3) { //VAR IDENT type_spec
			newTexts.put(ctx,indent()+ctx.getChild(0).getText()+" " +ctx.getChild(1).getText() +" "+ newTexts.get(ctx.type_spec()));
		} else if (ctx.getChildCount() == 5) { //VAR IDENT ',' IDENT type_spec
			String s1 = ctx.getChild(1).getText();
			String s2 = ctx.getChild(3).getText();
			newTexts.put(ctx,indent()+ctx.getChild(0).getText()+" "+ s1 +" " +newTexts.get(ctx.type_spec()) + " \n"
			+indent()+ctx.getChild(0).getText()+" "+s2 +" "+ newTexts.get(ctx.type_spec()) );
		} else { //VAR IDENT '[' LITERAL ']' type_spec
		newTexts.put(ctx,indent()+ ctx.getChild(0).getText()+" "+ctx.getChild(1).getText() + "["+ ctx.getChild(3) + "] " + newTexts.get(ctx.type_spec()));
		}
	}
	@Override
	public void exitType_spec(MiniGoParser.Type_specContext ctx) {
		if(ctx.getChild(0) == null) {
			newTexts.put(ctx,"");
		}
		else {
			newTexts.put(ctx, ctx.getChild(0).getText());
		}
	}
	
	@Override
	public void exitExpr(MiniGoParser.ExprContext ctx) {
		String s1 = null, s2 = null, op = null;
		boolean b1 = false, b2 = false;

		if (isBinaryOperation(ctx)) {
			if (ctx.getChild(0) == ctx.expr(0)) { // expr op expr
				s1 = newTexts.get(ctx.expr(0));
				s2 = newTexts.get(ctx.expr(1));
				op = ctx.getChild(1).getText();
				for (int i = 0; i < s1.length(); i++) { // s1이 숫자인지
					b1 = isNumber(s1.charAt(i));
					if (!b1)
						break;
				}
				for (int i = 0; i < s2.length(); i++) { // s2가 숫자인지
					b2 = isNumber(s2.charAt(i));
					if (!b2)
						break;
				}

				if (op.equals("+")) {
					if (s1.equals("0")) {
						newTexts.put(ctx, s2);
					} else if (s2.equals("0")) {
						newTexts.put(ctx, s1);
					} else {
						String check = s1 + " " + op + " " + s2;
						if (lookupRhs_Table(check, location) != null) {
							newTexts.put(ctx,
									lookupRhs_Table(check, location).lhs);
						} else {
							if (b1 && b2) { // folding
								newTexts.put(ctx,Integer.toString(Integer.parseInt(s1)
												+ Integer.parseInt(s2)));
							} else
								newTexts.put(ctx, check);
						}
					}
				} else if (op.equals("*")) {
					if (s1.equals("0") || s2.equals("0")) {
						newTexts.put(ctx, "0");
					} else if (s1.equals("1")) {
						newTexts.put(ctx, s2);
					} else if (s2.equals("1")) {
						newTexts.put(ctx, s1);
					} else {
						String check = s1 + " " + op + " " + s2;
						if (lookupRhs_Table(check, location) != null) {
							newTexts.put(ctx,
									lookupRhs_Table(check, location).lhs);
						} else {
							if (b1 && b2) { // folding
								newTexts.put(
										ctx,
										Integer.toString(Integer.parseInt(s1)
												* Integer.parseInt(s2)));
							} else
								newTexts.put(ctx, check);
						}
					}
				} else if (op.equals("-")) {
					if (s1.equals("0")) {
						newTexts.put(ctx, "-" + s2);
					} else if (s2.equals("0")) {
						newTexts.put(ctx, s1);
					} else {
						String check = s1 + " " + op + " " + s2;
						if (lookupRhs_Table(check, location) != null) {
							newTexts.put(ctx,
									lookupRhs_Table(check, location).lhs);
						} else {
							if (b1 && b2) { // folding
								newTexts.put(
										ctx,
										Integer.toString(Integer.parseInt(s1)
												- Integer.parseInt(s2)));
							} else
								newTexts.put(ctx, check);
						}
					}
				} else if (op.equals("/")) {
					if (s1.equals("0")) {
						newTexts.put(ctx, "0");
					} else if (s2.equals("0")) {
						System.out.println("Error[나눗셈]: 0으로 나눌 수 없습니다.");
						newTexts.put(ctx, s1 + " " + op + " " + s2);
					} else if (s2.equals("1")) {
						newTexts.put(ctx, s1);
					} else {
						String check = s1 + " " + op + " " + s2;
						if (lookupRhs_Table(check, location) != null) {
							newTexts.put(ctx,
									lookupRhs_Table(check, location).lhs);
						} else {
							if (b1 && b2) { // folding
								newTexts.put(
										ctx,
										Integer.toString(Integer.parseInt(s1)
												/ Integer.parseInt(s2)));
							} else
								newTexts.put(ctx, check);
						}
					}
				} else if (op.equals("%")) {
					String check = s1 + " " + op + " " + s2;
					if (s2.equals("0")) {
						System.out.println("Error[mod]: 0으로 나눌 수 없습니다.");
						newTexts.put(ctx, s1 + " " + op + " " + s2);
					} else {
						if (lookupRhs_Table(check, location) != null) {
							newTexts.put(ctx,
									lookupRhs_Table(check, location).lhs);
						} else {
							if (b1 && b2) { // folding
								newTexts.put(
										ctx,
										Integer.toString(Integer.parseInt(s1)
												% Integer.parseInt(s2)));
							} else
								newTexts.put(ctx, check);
						}
					}
				} else {
					String check = s1 + " " + op + " " + s2;
					if (b1 && b2) {
						if (op.equals("==")) {
							if (Integer.parseInt(s1) - Integer.parseInt(s2) == 0) {
								newTexts.put(ctx, "1");
							} else {
								newTexts.put(ctx, "0");
							}
						} else if (op.equals("!=")) {
							if (Integer.parseInt(s1) - Integer.parseInt(s2) == 0) {
								newTexts.put(ctx, "0");
							} else {
								newTexts.put(ctx, "1");
							}
						} else if (op.equals("<=") || op.equals("<")) {
							if (Integer.parseInt(s1) - Integer.parseInt(s2) < 0) {
								newTexts.put(ctx, "1");
							} else if (Integer.parseInt(s1)
									- Integer.parseInt(s2) > 0) {
								newTexts.put(ctx, "0");
							} else {
								if (op.equals("<="))
									newTexts.put(ctx, "1");
								else
									newTexts.put(ctx, "0");
							}
						} else if (op.equals(">=") || op.equals(">")) {
							if (Integer.parseInt(s1) - Integer.parseInt(s2) < 0) {
								newTexts.put(ctx, "0");
							} else if (Integer.parseInt(s1)
									- Integer.parseInt(s2) > 0) {
								newTexts.put(ctx, "1");
							} else {
								if (op.equals(">="))
									newTexts.put(ctx, "1");
								else
									newTexts.put(ctx, "0");
							}
						} else if (op.equals("and")) {
							if (Integer.parseInt(s1) != 0
									&& Integer.parseInt(s2) != 0) {
								newTexts.put(ctx, "1");
							} else {
								newTexts.put(ctx, "0");
							}
						} else if (op.equals("or")) {
							if (Integer.parseInt(s1) != 0
									|| Integer.parseInt(s2) != 0) {
								newTexts.put(ctx, "1");
							} else {
								newTexts.put(ctx, "0");
							}
						}
					} else {
						newTexts.put(ctx, check);
					}
				}
			} else { // IDENT '=' expr
				s1 = ctx.getChild(0).getText();
				s2 = newTexts.get(ctx.expr(0));
				op = ctx.getChild(1).getText();
				if (lookup_Table(s1, location) != null)
					lookup_Table(s1, location).use = true;
				for (int i = 0; i < s2.length(); i++) { // s2가 숫자인지
					b2 = isNumber(s2.charAt(i));
					if (!b2)
						break;
				}
				if (!b2) { // s2가 숫자가 아니면
					if (lookup_Table(s2, location) != null) {
						String newRhs = lookup_Table(s2, location).rhs;
						newTexts.put(ctx, s1 + " " + op + " " + newRhs);
						update_Table(s1, newRhs, location);
					} else {
						newTexts.put(ctx, s1 + " " + op + " " + s2);
						update_Table(s1, s2, location);
					}
				} else {
					newTexts.put(ctx, s1 + " " + op + " " + s2);
					update_Table(s1, s2, location);
				}
			}
		} else if (isPreOperation(ctx)) { // op expr
			/* 전위 연산자와 피연산자 사이에는 빈칸을 두지 않는다. */
			op = ctx.getChild(0).getText();
			s1 = newTexts.get(ctx.expr(0));
			if (op.equals("++")) {
				if (lookup_Table(s1, location) != null) {
					String newRhs = lookup_Table(s1, location).rhs;
					for (int i = 0; i < newRhs.length(); i++) {
						b1 = isNumber(newRhs.charAt(i));
						if (!b1)
							break;
					}
					if (!b1) { // rhs가 숫자가 아니면
						newRhs += "+ 1";
					} else { // rhs가 숫자면
						newRhs = Integer.toString(Integer.parseInt(newRhs) + 1);
					}
					update_Table(s1, newRhs, location);
				}
			} else if (op.equals("--")) {
				if (lookup_Table(s1, location) != null) {
					String newRhs = lookup_Table(s1, location).rhs;
					for (int i = 0; i < newRhs.length(); i++) {
						b1 = isNumber(newRhs.charAt(i));
						if (!b1)
							break;
					}
					if (!b1) { // rhs가 숫자가 아니면
						newRhs += "- 1";
					} else { // rhs가 숫자면
						newRhs = Integer.toString(Integer.parseInt(newRhs) - 1);
					}
					update_Table(s1, newRhs, location);
				}
			}
			else if( op.equals("-")) {
				if (lookup_Table(s1, location) != null) {
					String newRhs = lookup_Table(s1, location).rhs;
					for (int i = 0; i < newRhs.length(); i++) {
						b1 = isNumber(newRhs.charAt(i));
						if (!b1)
							break;
					}
					if (!b1) { // rhs가 숫자가 아니면
						newRhs = "-"+newRhs;
					} else { // rhs가 숫자면
						newRhs = Integer.toString(-Integer.parseInt(newRhs));
					}
					update_Table(s1, newRhs, location);
				}
			}
			else if(op.equals("+")) {
				if (lookup_Table(s1, location) != null) {
					String newRhs = lookup_Table(s1, location).rhs;
					for (int i = 0; i < newRhs.length(); i++) {
						b1 = isNumber(newRhs.charAt(i));
						if (!b1)
							break;
					}
					if (!b1) { // rhs가 숫자가 아니면
						newRhs = "+"+newRhs;
					} else { // rhs가 숫자면
						newRhs = Integer.toString(Integer.parseInt(newRhs));
					}
					update_Table(s1, newRhs, location);
				}
			}
			else if(op.equals("!")) {
				if (lookup_Table(s1, location) != null) {
					String newRhs = lookup_Table(s1, location).rhs;
					for (int i = 0; i < newRhs.length(); i++) {
						b1 = isNumber(newRhs.charAt(i));
						if (!b1)
							break;
					}
					if (!b1) { // rhs가 숫자가 아니면
						newRhs ="!"+newRhs;
					} else { // rhs가 숫자면
						newRhs = Integer.toString(Integer.parseInt(newRhs));
					}
					update_Table(s1, newRhs, location);
				}
			}
			newTexts.put(ctx, op + s1);
		} else if (ctx.getChildCount() == 1) { // IDENT|LITERAL
			if (ctx.IDENT() != null) { // IDENT
				if (lookup_Table(ctx.IDENT().getText(), location) != null) { // propagation
					lookup_Table(ctx.IDENT().getText(), location).use = true;
					s1 = lookup_Table(ctx.IDENT().getText(), location).rhs;
					if (s1 != null) {
						if (!isPreOperation((MiniGoParser.ExprContext) ctx
								.getParent())
								&& !(ctx.parent.parent.parent.parent.parent instanceof MiniGoParser.For_stmtContext)) {
							newTexts.put(ctx, s1);
						} else {
							newTexts.put(ctx, ctx.IDENT().getText());
						}
					} else {
						System.out.println("Error[null]: 변수 "
								+ ctx.IDENT().getText() + "에 값이 없습니다!");
						newTexts.put(ctx, ctx.IDENT().getText());
					}
				} else {
					System.out.println("Error[variable]: 변수 "
							+ ctx.IDENT().getText() + " 없습니다!");
					newTexts.put(ctx, ctx.IDENT().getText());
				}
			} else { // LITERAL
				s1 = ctx.getChild(0).getText();
				boolean type = false;
				for (int i = 0; i < s1.length(); i++) {
					type = isNumber(s1.charAt(i));
					if (!type)
						break;
				}
				if (!type) {
					if (ctx.getParent() instanceof MiniGoParser.Return_stmtContext) // return
						// type
						System.out
								.println("Error[return type]: int형 함수와 return type이 맞지 않습니다! ("
										+ s1 + ")");
					else
						System.out
								.println("Error[type]: int형 변수와 type이 맞지 않습니다! ("
										+ s1 + ")");
				}
				newTexts.put(ctx, ctx.getChild(0).getText());
			}
		} else if (ctx.getChildCount() == 3) { // '(' expr ')'
			/* 일반 괄호는 expression에 붙여 적는다. */
			if (!ctx.expr().contains(" ")) {
				newTexts.put(ctx, newTexts.get(ctx.expr(0)));
			} else {
				newTexts.put(ctx, "(" + newTexts.get(ctx.expr(0)) + ")");
			}
		} else if (ctx.getChildCount() == 4) {// IDENT '[' expr ']' | IDENT '(' args ')'
			s1 = ctx.getChild(0).getText();
			if (lookup_Table(ctx.IDENT().getText(), location) != null)
				lookup_Table(ctx.IDENT().getText(), location).use = true;
			if (ctx.getChild(1).getText().equals("[")) { // IDENT '[' expr ']'
				s2 = newTexts.get(ctx.expr(0));
				newTexts.put(ctx, s1 + "[" + s2 + "]");
			} else { // IDENT '(' args ')'
				s2 = newTexts.get(ctx.args());
				newTexts.put(ctx, s1 + "(" + s2 + ")");
			}
		} else if (ctx.getChildCount() == 6) {
			// IDENT '[' expr ']' '=' expr
			s1 = newTexts.get(ctx.expr(0));
			s2 = newTexts.get(ctx.expr(1));
			if (lookup_Table(ctx.IDENT().getText(), location) != null)
				lookup_Table(ctx.IDENT().getText(), location).use = true;
			newTexts.put(ctx, ctx.getChild(0) + "[" + s1 + "] = " + s2);
		}
		
	}
	
	@Override
	public void enterFun_decl(MiniGoParser.Fun_declContext ctx) {
		location = ctx.getChild(1).getText();
	}
	
	@Override
	public void exitFun_decl(MiniGoParser.Fun_declContext ctx) {
		if( ctx.getChildCount() == 7) {
			newTexts.put(ctx, ctx.getChild(0).getText() + " " + ctx.getChild(1).getText() + "("
					+ newTexts.get(ctx.params()) + ") "+ newTexts.get(ctx.type_spec(0)) +" \n"
					+ newTexts.get(ctx.compound_stmt()));
		}
		else {
			newTexts.put(ctx, ctx.getChild(0).getText() + " " + ctx.getChild(1).getText() + "("
					+ newTexts.get(ctx.params()) + ") (" +newTexts.get(ctx.type_spec(0))+","+newTexts.get(ctx.type_spec(1)) +" \n"
					+ newTexts.get(ctx.compound_stmt()));
		}
		location = "global";
	}
	@Override
	public void exitParams(MiniGoParser.ParamsContext ctx) {
		if (ctx.getChildCount() == 0) {
			newTexts.put(ctx, "");
		}
		else { // param (',' param)*
			int i = 2;
			String s = newTexts.get(ctx.param(0));
			while (ctx.param(i) != null) {
				s += ","+newTexts.get(ctx.param(i));
				i = i+2;
				
			}
			newTexts.put(ctx, s);
		}
	}
	@Override
	public void enterParam(MiniGoParser.ParamContext ctx) {
		String varName = ctx.getChild(0).getText();
		declare_Table(varName, location);
	}
	
	@Override
	public void exitParam(MiniGoParser.ParamContext ctx) {
		if (ctx.getChildCount() == 2) { // IDENT type_spec
			newTexts.put(ctx,
					 ctx.getChild(0)+" "+ newTexts.get(ctx.type_spec()) );
		} else { //IDENT '[' ']' type_spec
			newTexts.put(ctx,
					ctx.getChild(0)+"[] "+  newTexts.get(ctx.type_spec()));
		}
	
	}
	@Override
	public void exitStmt(MiniGoParser.StmtContext ctx) {
		if (ctx.getChild(0) == ctx.expr_stmt()) { // expr_stmt
			newTexts.put(ctx, newTexts.get(ctx.expr_stmt()));
		} else if (ctx.getChild(0) == ctx.compound_stmt()) {// compound_stmt
			
			if (ctx.parent instanceof MiniGoParser.If_stmtContext) {
				MiniGoParser.If_stmtContext test = (MiniGoParser.If_stmtContext) ctx.parent;
				if (newTexts.get(test.expr()).equals("1") || newTexts.get(test.expr()).equals("0")) {
					newTexts.put(ctx, newTexts.get(ctx.compound_stmt()));
				} else {
					newTexts.put(ctx, indent() + newTexts.get(ctx.compound_stmt()));
				}
			} else {
				newTexts.put(ctx, indent() + newTexts.get(ctx.compound_stmt()));
			}
		} 
		else if (ctx.getChild(0) == ctx.assign_stmt()) {
			newTexts.put(ctx, newTexts.get(ctx.assign_stmt()));
		} else if (ctx.getChild(0) == ctx.if_stmt()) { // if_stmt
			if (newTexts.get(ctx.if_stmt().expr()).equals("1") || newTexts.get(ctx.if_stmt().expr()).equals("0")) {
				newTexts.put(ctx, newTexts.get(ctx.if_stmt()));
			} else {
				newTexts.put(ctx, indent() + newTexts.get(ctx.if_stmt()));
			}
		} else if (ctx.getChild(0) == ctx.for_stmt()) { // for_stmt
			newTexts.put(ctx, indent() + newTexts.get(ctx.for_stmt()));
		} else if (ctx.getChild(0) == ctx.return_stmt()) { // return_stmt
			newTexts.put(ctx, indent() + newTexts.get(ctx.return_stmt()));
		}
	}
	
	@Override
	public void exitExpr_stmt(MiniGoParser.Expr_stmtContext ctx) {
		
	}
	@Override
	public void exitFor_stmt(MiniGoParser.For_stmtContext ctx) {
		
	}
	@Override
	public void enterCompound_stmt(MiniGoParser.Compound_stmtContext ctx) {
		if (ctx.getParent().getParent().getChildCount() >= 5) {
			if (ctx.parent.parent instanceof MiniGoParser.If_stmtContext) {
				MiniGoParser.If_stmtContext test = (MiniGoParser.If_stmtContext) ctx.parent.parent;
				if (newTexts.get(test.expr()).equals("1") || newTexts.get(test.expr()).equals("0")) {

				} else {
					indentCount++; // 들여쓰기 하나 증가
				}
			} else {
				indentCount++;
			}
		} else {
			indentCount++;
		}
	}

	@Override
	public void exitCompound_stmt(MiniGoParser.Compound_stmtContext ctx) {
		String s = "";
		int i = 0;
		if (ctx.getParent().getParent() instanceof MiniGoParser.If_stmtContext) {
			if (newTexts.get(ctx.getParent().getParent().getChild(2)).equals("1")
					|| newTexts.get(ctx.getParent().getParent().getChild(2)).equals("0")) {
				while (ctx.stmt(i) != null) { // stmt*
					s += newTexts.get(ctx.stmt(i)) + "\n";
					i++;
				}
				newTexts.put(ctx, s);
				MiniGoParser.If_stmtContext test = (MiniGoParser.If_stmtContext) ctx.parent.parent;
				if (newTexts.get(test.expr()).equals("1") || newTexts.get(test.expr()).equals("0")) {

				} else {
					indentCount--; // 들여쓰기 하나 감소
				}
				return;
			}
		}
		s = "{\n";
		while (ctx.local_decl(i) != null) { // local_decl*
			s += newTexts.get(ctx.local_decl(i)) + "\n";
			i++;
		}
		i = 0;
		while (ctx.stmt(i) != null) { // stmt*
			s += newTexts.get(ctx.stmt(i)) + "\n";
			i++;
		}
		indentCount--;
		s += indent() + "}";
		newTexts.put(ctx, s);
	}
	@Override
	public void enterLocal_decl(MiniGoParser.Local_declContext ctx) {
		String varName = ctx.getChild(1).getText();
		declare_Table(varName, location);
	}
	@Override
	public void exitLocal_decl(MiniGoParser.Local_declContext ctx) {
		if (ctx.getChildCount() == 3) { // VAR IDENT type_spec ';'
			newTexts.put(ctx, indent() + ctx.getChild(0).getText() + " "
				+ctx.getChild(1).getText() +" "+ ctx.type_spec());
		}
		else if (ctx.getChildCount() == 6) { // VAR IDENT '[' LITERAL ']' type_spec
			newTexts.put(ctx, indent() + ctx.getChild(0).getText() + " "
					+ctx.getChild(1).getText() +" ["+ ctx.getChild(3).getText() +"] "+ ctx.type_spec());
		}
	}
	
	@Override
	public void exitIf_stmt(MiniGoParser.If_stmtContext ctx) {
		
	}
	@Override
	public void exitReturn_stmt(MiniGoParser.Return_stmtContext ctx) {
		String type = ctx.getParent().getParent().getParent().getChild(0).getText();
		if (ctx.getChildCount() == 2) { // RETURN 
			if (type.equals("int")) {
				System.out.println("Error[return type]: int형 함수와 return type이 맞지 않습니다!");
			}
			newTexts.put(ctx, ctx.getChild(0).getText());
		}else if( ctx.getChildCount() == 4) {  //RETURN expr, expr
			if (type.equals("void")) {
				System.out.println("Error[return type]: void형 함수와 return type이 맞지 않습니다!");
			}
			newTexts.put(ctx, ctx.getChild(0)+" " +newTexts.get(ctx.expr(0))+" , "+ newTexts.get(ctx.expr(1)));
			
		}
		else { // RETURN expr ';'
			if (type.equals("void")) {
				System.out
						.println("Error[return type]: void형 함수와 return type이 맞지 않습니다!");
			}
			newTexts.put(ctx, ctx.getChild(0).getText() + " " + newTexts.get(ctx.expr(0)));
		}
	}
	@Override
	public void exitArgs(MiniGoParser.ArgsContext ctx) {
		if (ctx.getChildCount() == 0) {
			newTexts.put(ctx, "");
		} else { // expr ( ',' expr)*
			int i = 2;
			String s = newTexts.get(ctx.expr(0));
			while (ctx.expr(i) != null) { // ( ',' expr)* 부분을 위한 while문
				s += ","+newTexts.get(ctx.expr(i));
				i=i+2;
			}
			newTexts.put(ctx, s);
		}
	}
	
}
