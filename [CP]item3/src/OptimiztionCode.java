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
	String location = "global"; // ���� ��ġ

	// ���� �޼ҵ�
	boolean isBinaryOperation(MiniGoParser.ExprContext ctx) {
		return ctx.getChildCount() == 3 && ctx.getChild(1) != ctx.expr(0);
		// �ڽ� 3��¥�� expr �� '(' expr ')'�� ����
	}

	boolean isPreOperation(MiniGoParser.ExprContext ctx) {
		return ctx.getChildCount() == 2;
		// ���� ������ + expr
	}

	public static boolean isNumber(char firstLetter) {
		if (firstLetter == ' ') // ������� ��
			return false;

		if (firstLetter < '0' || firstLetter > '9')
			return false;

		return true;
	}

	String indent() { // �鿩���� �޼ҵ�
		/* ����̳� nesting �Ǿ� �� ���� 4ĭ �鿩���� ��.���� ���� */
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
				if (!location.equals("global")) { // local�̸� local�� ���� ��
					// global������ ã�ƺ���
					if (cmp_var.lhs.equals(lhs)
							&& cmp_var.location.equals("global"))
						if (symbol_table.get(i) != null)
							return symbol_table.get(i);
				}
			}
		}
		return null;
	}
	class Variable {
		String lhs;	//���� �̸�
		String rhs; //���� ��
		boolean use = false; //���� �������
		String location; //���� ��ġ

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
	
	@Override
	public void exitExpr(MiniGoParser.ExprContext ctx) {
	
		
	}
	
	@Override
	public void exitProgram(MiniGoParser.ProgramContext ctx) {
		
	}
	
	@Override
	public void exitDecl(MiniGoParser.DeclContext ctx) {
		// TODO Auto-generated method stub
		super.exitDecl(ctx);
	}
	@Override
	public void enterVar_decl(MiniGoParser.Var_declContext ctx) {
		
	}
	
	@Override
	public void exitVar_decl(MiniGoParser.Var_declContext ctx) {
		
	}
	@Override
	public void exitType_spec(MiniGoParser.Type_specContext ctx) {
		
	}
	@Override
	public void enterFun_decl(MiniGoParser.Fun_declContext ctx) {
		
	}
	
	@Override
	public void exitFun_decl(MiniGoParser.Fun_declContext ctx) {
		
	}
	@Override
	public void exitParams(MiniGoParser.ParamsContext ctx) {
		
	}
	@Override
	public void enterParam(MiniGoParser.ParamContext ctx) {
	
	}
	
	@Override
	public void exitParam(MiniGoParser.ParamContext ctx) {
	
	
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
