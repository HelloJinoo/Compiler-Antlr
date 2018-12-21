import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
public class UcodeGenterator {

	public static void main(String[] args) throws IOException {
		MiniGoLexer lexer = new MiniGoLexer(CharStreams.fromFileName("[01][201402415][장진우][02].go"));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MiniGoParser parser = new MiniGoParser(tokens);
		ParseTree tree = parser.program();
		
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(new UcodeGenListener(), tree);

		BufferedWriter fw = new BufferedWriter(new FileWriter("[01][201402415][장진우][03].uco"));
		fw.write(UcodeGenListener.result);
		fw.close();
		
	}
}
