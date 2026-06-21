package filter.ast;

import filter.ast.builder.AstBuilderPattern;
import filter.ast.builder.AstBuilderVisitor;
import filter.ast.builder.AstBuilders;
import filter.ast.nodes.Expr;
import filter.ast.printer.AstPrinter;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApprovalTest {
  // TODO
    @Test
    void testSimpleQuery(){
        String query = "artist == \"Beatles\" and year == 1965";

        Expr ast1 = AstBuilders.fromQuery(query, new AstBuilderPattern()::translate);
        Expr ast2 = AstBuilders.fromQuery(query, new AstBuilderVisitor()::translate);

        String patternOutput = AstPrinter.toString(ast1);
        String visitorOutput = AstPrinter.toString(ast2);

        assertEquals(patternOutput, visitorOutput, "Visitor- und Pattern-AST sind nicht identisch!");
        Approvals.verify(patternOutput);
    }



    @Test
    void testComplexQueryPattern(){
        String query = "genre in (\"rock\", \"jazz\") or year <= 1990 and not not artist == \"Beatles\"";
        Expr ast1 = AstBuilders.fromQuery(query, new AstBuilderPattern()::translate);
        Expr ast2 = AstBuilders.fromQuery(query, new AstBuilderVisitor()::translate);

        String patternOutput = AstPrinter.toString(ast1);
        String visitorOutput = AstPrinter.toString(ast2);

        assertEquals(patternOutput, visitorOutput, "Visitor- und Pattern-AST sind nicht identisch!");

        Approvals.verify(patternOutput);
    }



}
