package filter.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import filter.FilterParser;
import filter.ast.builder.AstBuilderPattern;
import filter.ast.builder.AstBuilderVisitor;
import filter.ast.builder.AstBuilders;
import filter.ast.nodes.CompOp;
import filter.ast.nodes.Expr;
import filter.ast.nodes.Value;
import filter.ast.printer.AstPrinter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AstTest {
    private AstBuilderVisitor visitorBuilder;
    private AstBuilderPattern patternBuilder;

    @BeforeEach
    void setUp() {
        this.visitorBuilder = new AstBuilderVisitor();
        this.patternBuilder = new AstBuilderPattern();
    }

    /**
     * Hilfsmethode, um ein Query-Parsing mit beiden Buildern zu vergleichen
     * und sicherzustellen, dass beide das exakt gleiche AST-Ergebnis liefern.
     */
    private void assertAstEquals(Expr expectedAst, String query) {
        Expr astFromVisitor = AstBuilders.fromQuery(query, ctx -> visitorBuilder.translate(ctx));
        Expr astFromPattern = AstBuilders.fromQuery(query, ctx -> patternBuilder.translate(ctx));

        assertNotNull(astFromVisitor, "Visitor-Builder lieferte null für: " + query);
        assertNotNull(astFromPattern, "Pattern-Builder lieferte null für: " + query);

        assertEquals(expectedAst, astFromVisitor, "Visitor AST stimmt nicht überein");
        assertEquals(expectedAst, astFromPattern, "Pattern AST stimmt nicht überein");
    }

    @Test
    void testSimpleStringComparison() {
        String query = "artist == \"Beatles\"";
        Expr expected = new Expr.Comparison("artist", CompOp.EQ, new Value.Str("Beatles"));

        assertAstEquals(expected, query);
    }

    @Test
    void testSimpleNumericComparison() {
        String query = "year >= 1965";
        Expr expected = new Expr.Comparison("year", CompOp.GE, new Value.Num(1965));

        assertAstEquals(expected, query);
    }

    @Test
    void testInListExpression() {
        String query = "genre in (\"rock\", \"jazz\")";
        Expr expected = new Expr.InList("genre", List.of(
            new Value.Str("rock"),
            new Value.Str("jazz")
        ));

        assertAstEquals(expected, query);
    }

    @Test
    void testNotExpression() {
        String query = "not artist == \"Beatles\"";
        Expr expected = new Expr.Not(
            new Expr.Comparison("artist", CompOp.EQ, new Value.Str("Beatles"))
        );

        assertAstEquals(expected, query);
    }

    @Test
    void testSimpleAnd() {
        String query = "artist == \"Beatles\" and year == 1965";
        Expr expected = new Expr.And(
            new Expr.Comparison("artist", CompOp.EQ, new Value.Str("Beatles")),
            new Expr.Comparison("year", CompOp.EQ, new Value.Num(1965))
        );

        assertAstEquals(expected, query);
    }

    @Test
    void testOperatorPrecedenceAndOr() {
        // Gemäß Grammatik bindet 'and' stärker als 'or'.
        // "A or B and C" muss zu "A or (B and C)" werden.
        String query = "genre == \"rock\" or year <= 1990 and artist == \"Beatles\"";

        Expr expected = new Expr.Or(
            new Expr.Comparison("genre", CompOp.EQ, new Value.Str("rock")),
            new Expr.And(
                new Expr.Comparison("year", CompOp.LE, new Value.Num(1990)),
                new Expr.Comparison("artist", CompOp.EQ, new Value.Str("Beatles"))
            )
        );

        assertAstEquals(expected, query);
    }

    @Test
    void testExplicitParentheses() {
        // Durch die Klammer wird das 'or' vorgezogen: "(A or B) and C"
        String query = "(genre == \"rock\" or year <= 1990) and artist == \"Beatles\"";

        Expr expected = new Expr.And(
            new Expr.Or(
                new Expr.Comparison("genre", CompOp.EQ, new Value.Str("rock")),
                new Expr.Comparison("year", CompOp.LE, new Value.Num(1990))
            ),
            new Expr.Comparison("artist", CompOp.EQ, new Value.Str("Beatles"))
        );

        assertAstEquals(expected, query);
    }

    @Test
    void testChainedAndExpressions() {
        // "A and B and C" wird zu "(A and B) and C" laut Aufgabenstellung
        String query = "year <= 1990 and artist == \"Beatles\" and year > 1960";

        Expr expected = new Expr.And(
            new Expr.And(
                new Expr.Comparison("year", CompOp.LE, new Value.Num(1990)),
                new Expr.Comparison("artist", CompOp.EQ, new Value.Str("Beatles"))
            ),
            new Expr.Comparison("year", CompOp.GT, new Value.Num(1960))
        );

        assertAstEquals(expected, query);
    }
}
