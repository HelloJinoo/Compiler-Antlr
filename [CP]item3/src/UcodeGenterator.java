
import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
public class UcodeGenterator {
	static void optimized_miniGo() throws IOException {
		MiniGoLexer lexer = new MiniGoLexer(CharStreams.fromFileName("input_Minigo.go"));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MiniGoParser parser = new MiniGoParser(tokens);
		ParseTree tree = parser.program();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(new OptimizationCode(), tree);
	}
	
	static void MiniGo_to_optimzedUcode() throws IOException {
		MiniGoLexer lexer = new MiniGoLexer(CharStreams.fromFileName("optimizedGo.go"));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MiniGoParser parser = new MiniGoParser(tokens);
		ParseTree tree = parser.program();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(new UcodeGenListener(), tree);
	}
	public static void main(String[] args) throws IOException {
		System.out.println("----------弥利拳等 Go------------");
		optimized_miniGo();
		System.out.println();
		System.out.println("----------弥利拳等 Ucode----------");
		MiniGo_to_optimzedUcode();
	}
}
