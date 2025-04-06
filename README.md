# Truth Table Generator

Here are some example expressions you can try:
1. `(A -> B) & (!B | A)`
2. `(A <-> B) -> (A & B)`
3. `A | !(B -> C) | (A ^ B)`
4. `((A / !B) \ B) | (C ^ (B -> C))`
5. `(A <-> (B ^ C)) -> (A & !(A | B | !C))`

## Example

```text
Enter a logical expression (up to 3 variables A, B, C):
Operators: ! & | / \ ^ -> <-> ( )
! (NOT), & (AND), | (OR), ^ (XOR)
/ (NAND), \ (NOR)
-> (Implies), <-> (Equivalent)
Expression: (A -> B) & (!B | A)

Generating Truth Table for: (A -> B) & (!B | A)

╔═══╤═══╤══════════╤════╤══════════╤═══════════════════════╗
║ A │ B │ (A -> B) │ !B │ (!B | A) │ ((A -> B) & (!B | A)) ║
╠═══╪═══╪══════════╪════╪══════════╪═══════════════════════╣
║ 0 │ 0 │        1 │  1 │        1 │                     1 ║
╟───┼───┼──────────┼────┼──────────┼───────────────────────╢
║ 0 │ 1 │        1 │  0 │        0 │                     0 ║
╟───┼───┼──────────┼────┼──────────┼───────────────────────╢
║ 1 │ 0 │        0 │  1 │        1 │                     0 ║
╟───┼───┼──────────┼────┼──────────┼───────────────────────╢
║ 1 │ 1 │        1 │  0 │        1 │                     1 ║
╚═══╧═══╧══════════╧════╧══════════╧═══════════════════════╝
```
