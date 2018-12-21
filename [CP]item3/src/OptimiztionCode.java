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
	
}
