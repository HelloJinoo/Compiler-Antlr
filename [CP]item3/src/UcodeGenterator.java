import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
		walker.walk(new OptimiztionCode(), tree);

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
		optimized_miniGo();
		MiniGo_to_optimzedUcode();
	}
}
