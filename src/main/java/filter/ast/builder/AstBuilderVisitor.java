package filter.ast.builder;

import filter.FilterBaseVisitor;
import filter.FilterParser;
import filter.ast.nodes.CompOp;
import filter.ast.nodes.Expr;
import filter.ast.nodes.Value;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class AstBuilderVisitor extends FilterBaseVisitor<Void> {

  // TODO
    private Deque<Expr> stack = new ArrayDeque<>();
    private Deque<Value> values = new ArrayDeque<>();

  // Public entry point
  public Expr translate(FilterParser.QueryContext ctx) {
    // TODO
      this.stack.clear();
      this.values.clear();

      visit(ctx);

      Expr e = this.stack.pop();
      return e;
  }

  // query  : expr EOF
  @Override
  public Void visitQuery(FilterParser.QueryContext ctx) {
    // TODO
      visit(ctx.expr());
    return null;
  }

  // expr: orExpr
  @Override
  public Void visitExpr(FilterParser.ExprContext ctx) {
    // TODO
      visit(ctx.orExpr());
    return null;
  }

  // orExpr : andExpr (OR andExpr)*
  @Override
  public Void visitOrExpr(FilterParser.OrExprContext ctx) {
    // TODO
      visit(ctx.andExpr(0));

      for (int i = 1; i < ctx.andExpr().size() ; i++) {
          visit(ctx.andExpr(i));

          Expr right = this.stack.pop();
          Expr left = this.stack.pop();

          Expr orNode = new Expr.Or(left, right);

          this.stack.push(orNode);
      }
    return null;
  }

  // andExpr: notExpr (AND notExpr)*
  @Override
  public Void visitAndExpr(FilterParser.AndExprContext ctx) {
    // TODO
      visit(ctx.notExpr(0));

      for (int i = 1; i < ctx.notExpr().size(); i++) {
          visit(ctx.notExpr(i));

          Expr right = this.stack.pop();
          Expr left = this.stack.pop();

          Expr andNode = new Expr.And(left, right);

          this.stack.push(andNode);
      }


    return null;
  }

  // notExpr: NOT notExpr | primary
  @Override
  public Void visitNotExpr(FilterParser.NotExprContext ctx) {
    // TODO
      if (ctx.NOT() != null) {

          visit(ctx.notExpr());

          Expr right = this.stack.pop();
          Expr notNode = new Expr.Not(right);

          this.stack.push(notNode);
      }
      else{
          visit(ctx.primary());
      }
    return null;
  }

  // primary: comparison | '(' expr ')'
  @Override
  public Void visitPrimary(FilterParser.PrimaryContext ctx) {
    // TODO
      if (ctx.comparison() != null) {
          visit(ctx.comparison());
      }
      else{
          visit(ctx.expr());
      }
    return null;
  }

  // comparison
  //   : IDENTIFIER op=COMPOP value=literal
  //   | IDENTIFIER IN '(' literalList ')'
  @Override
  public Void visitComparison(FilterParser.ComparisonContext ctx) {
    // TODO
      if (ctx.COMPOP() != null) {
          visit(ctx.value);
          String field = ctx.IDENTIFIER().getText();
          CompOp op = CompOp.fromSymbol(ctx.op.getText());
          Value value = this.values.pop();

          Expr Comparison = new Expr.Comparison(field, op, value);

          this.stack.push(Comparison);
      }
      else if (ctx.IN() != null) {
          visit(ctx.literalList());
          String field = ctx.IDENTIFIER().getText();


          List<Value> values = new ArrayList<>();
          int Anzahl = ctx.literalList().literal().size();
          for (int i = 0; i < Anzahl; i++) {
              values.add(0, this.values.pop());
          }

          Expr inList = new Expr.InList(field, values);

          this.stack.push(inList);
      }
      return null;
  }

  // literalList: literal (',' literal)*
  @Override
  public Void visitLiteralList(FilterParser.LiteralListContext ctx) {
    // TODO
      for(FilterParser.LiteralContext literal : ctx.literal()){
          visit(literal);
      }
    return null;
  }

  // literal: STRING | NUMBER
  @Override
  public Void visitLiteral(FilterParser.LiteralContext ctx) {
    // TODO
      if (ctx.STRING() != null) {
          String text = ctx.STRING().getText();
          Value value = new Value.Str(text.substring(1, text.length() - 1));
          this.values.push(value);
      }
      else if (ctx.NUMBER() != null) {
          Value value = new Value.Num(Integer.parseInt(ctx.NUMBER().getText()));
          this.values.push(value);
      }
    return null;
  }
}
