package filter;

import filter.app.FilterApp;
import filter.ast.builder.AstBuilderPattern;
import filter.ast.builder.AstBuilderVisitor;
import filter.ast.builder.AstBuilders;
import filter.ast.eval.Evaluator;
import filter.ast.nodes.CompOp;
import filter.ast.nodes.Expr;
import filter.ast.nodes.Value;
import filter.ast.printer.AstPrinter;
import filter.model.MediaItem;

public class Main {

  static void main() {

    // ---------- Parsing ----------
    var query =
        """
        genre in ("rock", "jazz") or year <= 1990 and not artist == "Beatles"
        """;
    var ast1 = AstBuilders.fromQuery(query, new AstBuilderPattern()::translate);
    //var ast2 = AstBuilders.fromQuery(query, new AstBuilderVisitor()::translate);
    IO.println(AstPrinter.toString(ast1));
    //IO.println(AstPrinter.toString(ast2));

    // ---------- data as in songlist.txt ----------
    var items = MediaItem.loadFromResource("songlist.txt");
    var matching = items.stream().filter(i -> Evaluator.matches(i, ast1)).toList();
    matching.forEach(IO::println);

    // ---------- AST ----------
    // artist == "Beatles" and year == 1965
    var expr =
        new Expr.And(
            new Expr.Comparison("artist", CompOp.EQ, new Value.Str("Beatles")),
            new Expr.Comparison("year", CompOp.GE, new Value.Num(1965)));

    IO.println(AstPrinter.toString(expr));

    // ---------- start demo app ----------
    FilterApp.run();
  }
}
