package filter.ast.nodes;

import java.util.List;

public sealed interface Expr {
  record And(Expr left, Expr right) implements Expr {}

  record Or(Expr left, Expr right) implements Expr {}

  record Not(Expr inner) implements Expr {}

  record Comparison(
      String field, // e.g. "artist"
      CompOp op,
      Value value)
      implements Expr {}

  record InList(String field, List<Value> values) implements Expr {}
}

