package filter.ast.builder;

import filter.FilterParser;
import filter.ast.nodes.CompOp;
import filter.ast.nodes.Expr;
import filter.ast.nodes.Value;

import java.util.ArrayList;
import java.util.List;

public class AstBuilderPattern {

  // Public entry point
  // query  : expr EOF
  public Expr translate(FilterParser.QueryContext ctx) {
    // TODO
      Expr e = buildExpr(ctx.expr());
    return e;
  }

  // expr: orExpr
  private Expr buildExpr(FilterParser.ExprContext ctx) {
    // TODO
      Expr e = buildOrExpr(ctx.orExpr());
    return e;
  }

  // orExpr : andExpr (OR andExpr)*
  private Expr buildOrExpr(FilterParser.OrExprContext ctx) {
    // TODO
      Expr e = buildAndExpr(ctx.andExpr(0));
      for (int i = 1; i < ctx.andExpr().size(); i++) {
          Expr e2 = buildAndExpr(ctx.andExpr(i));
          Expr orNode = new Expr.Or(e, e2);
          e = orNode;
      }
    return e;
  }

  // andExpr: notExpr (AND notExpr)*
  private Expr buildAndExpr(FilterParser.AndExprContext ctx) {
    // TODO
      Expr e = buildNotExpr(ctx.notExpr(0));
      for (int i = 1; i < ctx.notExpr().size(); i++) {
          Expr e2 = buildNotExpr(ctx.notExpr(i));
          Expr andNode = new Expr.And(e, e2);
          e = andNode;
      }
    return e;
  }

  // notExpr: NOT notExpr | primary
  private Expr buildNotExpr(FilterParser.NotExprContext ctx) {
    // TODO
      return switch (ctx) {
          case FilterParser.NotExprContext c when c.NOT() != null ->{
              Expr e = buildNotExpr(c.notExpr());
              yield new Expr.Not(e);
          }
          case FilterParser.NotExprContext c -> buildPrimary(c.primary());
      };
  }

  // primary: comparison | '(' expr ')'
  private Expr buildPrimary(FilterParser.PrimaryContext ctx) {
    // TODO
      return switch(ctx){
          case FilterParser.PrimaryContext c when c.comparison() != null -> buildComparison(c.comparison());
          case FilterParser.PrimaryContext c -> buildExpr(c.expr());
      };

  }

  // comparison
  //   : IDENTIFIER op=COMPOP value=literal
  //   | IDENTIFIER IN '(' literalList ')'
  private Expr buildComparison(FilterParser.ComparisonContext ctx) {
    // TODO
      String field = ctx.IDENTIFIER().getText();
      return switch(ctx){
          case FilterParser.ComparisonContext c when c.op != null->{
              Value value = buildLiteral(c.value);
              CompOp op = CompOp.fromSymbol(c.op.getText());
              yield new Expr.Comparison(field, op, value);
          }
          case FilterParser.ComparisonContext c when c.IN() != null-> {
              List<Value> e = buildLiteralList(c.literalList());
              Expr inList = new Expr.InList(field, e);
              yield inList;
          }
          default -> throw new IllegalArgumentException("idk");
      };
  }

  // literalList: literal (',' literal)*
  private List<Value> buildLiteralList(FilterParser.LiteralListContext ctx) {
    // TODO
      List<Value> e = new ArrayList<>();
      for(FilterParser.LiteralContext literal : ctx.literal()){
          e.add(buildLiteral(literal));
      }
    return e;
  }

  // literal: STRING | NUMBER
  private Value buildLiteral(FilterParser.LiteralContext ctx) {
      // TODO
      return switch (ctx) {
          case FilterParser.LiteralContext c when c.STRING() != null -> new Value.Str(c.STRING().getText());
          case FilterParser.LiteralContext c when c.NUMBER() != null ->
              new Value.Num(Integer.parseInt(c.NUMBER().getText()));
          default -> throw new IllegalArgumentException("idk");
      };
  }

}
