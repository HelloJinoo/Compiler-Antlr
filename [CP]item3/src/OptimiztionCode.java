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
				if (!location.equals("global")) { // local이면 local에 없을 때
					// global에서도 찾아보기
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
	
}
