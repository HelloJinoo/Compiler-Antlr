import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTreeProperty;



public class OptimiztionCode extends MiniGoBaseListener{
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
	}
	
	@Override
	public void exitExpr_stmt(MiniGoParser.Expr_stmtContext ctx) {
		
	}
	@Override
	public void exitFor_stmt(MiniGoParser.For_stmtContext ctx) {
		
	}
	@Override
	public void enterCompound_stmt(MiniGoParser.Compound_stmtContext ctx) {
	
	}
	@Override
	public void exitCompound_stmt(MiniGoParser.Compound_stmtContext ctx) {
		
	}
	@Override
	public void enterLocal_decl(MiniGoParser.Local_declContext ctx) {
		
	}
	@Override
	public void exitLocal_decl(MiniGoParser.Local_declContext ctx) {
	}
	
	@Override
	public void exitIf_stmt(MiniGoParser.If_stmtContext ctx) {
		
	}
	@Override
	public void exitReturn_stmt(MiniGoParser.Return_stmtContext ctx) {
		
	}
	@Override
	public void exitArgs(MiniGoParser.ArgsContext ctx) {
		
	}
	
}
