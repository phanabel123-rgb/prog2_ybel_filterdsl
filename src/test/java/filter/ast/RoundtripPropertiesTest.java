package filter.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import filter.ast.builder.AstBuilderPattern;
import filter.ast.builder.AstBuilderVisitor;
import filter.ast.builder.AstBuilders;
import filter.ast.nodes.CompOp;
import filter.ast.nodes.Expr;
import filter.ast.nodes.Value;
import filter.ast.printer.AstPrinter;
import net.jqwik.api.*;

public class RoundtripPropertiesTest {

  // TODO
  @Property
  void roundtripPatternBuilder(@ForAll("simpleQueries") String originalQuery) {

      Expr ast1 = AstBuilders.fromQuery(originalQuery, new AstBuilderPattern()::translate);

      String printedQuery = AstPrinter.toString(ast1);

      Expr ast2 = AstBuilders.fromQuery(printedQuery, new AstBuilderPattern()::translate);

      assertEquals(ast1, ast2, "Pattern-Roundtrip fehlgeschlagen für: "+ originalQuery);
  }

    @Property
    void roundtripVisitorBuilder(@ForAll("simpleQueries") String originalQuery) {

        Expr ast1 = AstBuilders.fromQuery(originalQuery, new AstBuilderVisitor()::translate);
        String printedQuery = AstPrinter.toString(ast1);
        Expr ast2 = AstBuilders.fromQuery(printedQuery, new AstBuilderVisitor()::translate);

        assertEquals(ast1, ast2, "Visitor-Roundtrip fehlgeschlagen für: "+originalQuery);
    }

    @Property
    void crossBuilderValidation(@ForAll("simpleQueries") String originalQuery) {

        Expr astVisitor = AstBuilders.fromQuery(originalQuery, new AstBuilderVisitor()::translate);
        String printedVisitor = AstPrinter.toString(astVisitor);

        Expr astPatternFromVisitorPrint = AstBuilders.fromQuery(printedVisitor, new AstBuilderPattern()::translate);

        assertEquals(astVisitor, astPatternFromVisitorPrint, "Cross-Builder-Validierung fehlgeschlagen!");
    }

    @Property
    void andIdempotenz(@ForAll("simpleQueries") String query) {
        // Gesetz der Idempotenz: A AND A ist logisch genau dasselbe wie A
        Expr a = AstBuilders.fromQuery(query, new AstBuilderPattern()::translate);

        Expr andExpression = new Expr.And(a, a);

        Expr simplified = AstBuilders.simplify(andExpression);


        assertEquals(a, simplified, "Idempotenz von AND fehlgeschlagen!");
    }

    @Property
    void orIdempotenz(@ForAll("simpleQueries") String query) {
        // Gesetz der Idempotenz für OR: A OR A = A
        Expr a = AstBuilders.fromQuery(query, new AstBuilderPattern()::translate);

        Expr orExpression = new Expr.Or(a, a);

        Expr simplified = AstBuilders.simplify(orExpression);


        assertEquals(a, simplified, "Idempotenz von OR fehlgeschlagen!");
    }

    @Property
    void doppelteVerneinung(@ForAll("simpleQueries") String query) {

        Expr a = AstBuilders.fromQuery(query, new AstBuilderPattern()::translate);


        Expr doubleNot = new Expr.Not(new Expr.Not(a));


        Expr simplified = AstBuilders.simplify(doubleNot);

        assertEquals(a, simplified, "Doppelte Verneinung wurde von simplify nicht aufgelöst!");
    }


  // ---------- @Provide-Methods for Arbitraries ----------

  @Provide
  Arbitrary<String> fields() {
    return Arbitraries.of("title", "artist", "genre", "year");
  }

  @Provide
  Arbitrary<String> stringLiterals() {
    return Arbitraries.strings()
        .withChars("abcxyz")
        .ofMinLength(1)
        .ofMaxLength(5)
        .map(s -> "\"" + s + "\"");
  }

  @Provide
  Arbitrary<String> numberLiterals() {
    return Arbitraries.integers().between(1900, 2025).map(Object::toString);
  }

  @Provide
  Arbitrary<String> comparisons() {
    Arbitrary<String> ops = Arbitraries.of("==", "!=", "<", "<=", ">", ">=");

    Arbitrary<String> stringComp =
        Combinators.combine(fields(), ops, stringLiterals())
            .as((f, op, lit) -> f + " " + op + " " + lit);

    Arbitrary<String> numberComp =
        Combinators.combine(Arbitraries.of("year"), ops, numberLiterals())
            .as((f, op, lit) -> f + " " + op + " " + lit);

    return Arbitraries.oneOf(stringComp, numberComp);
  }

  @Provide
  Arbitrary<String> simpleQueries() {
    return comparisons()
        .list()
        .ofMinSize(1)
        .ofMaxSize(3)
        .map(
            list -> {
              if (list.size() == 1) return list.getFirst();
              StringBuilder sb = new StringBuilder();
              for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                  String conn = Arbitraries.of(" and ", " or ").sample();
                  sb.append(conn);
                }
                sb.append(list.get(i));
              }
              return sb.toString();
            });
  }
}
