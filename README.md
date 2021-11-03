# cognitive-complexity

Calculates cognitive complexities of functions in C (C11) source code.

## Requirements

TODO

* Java
* Maven

## Installation

TODO

## Usage

TODO

## Features

TODO

## Notes & Limitations

may cause error or miscalculation.

### Recursion

is not supported.

```c
void f(int n) {
    if (n > 0) {           // +1
        return n + f(n-1); // not calculated
    } else {               // +1
        return 0;
    }
} // -> complexity 2
```

### Macro definitions in function definition

are not calculated.

```c
void f() {
#define A(x, y, z) do { x = y || z; } while (0) // not calculated
} // -> complexity 0
```

### Preprocessor conditionals outside function definition

are calculated as below.

```c
#if A         // +1 (1)
    f() {}    //          -> complexity 2 (=(c))
  #if B       // +2 (2)
    g() {
      #if C   // +3 (3)
      #endif
    }         //          -> complexity 8 (=(a)+(c)+(3))
  #else       // +1 (4)
    g() {}    //          -> complexity 5 (=(a)+(c))
  #endif      //      -> 3 (=(2)+(4)) (a)
#else         // +1 (5)
    f() {}    //          -> complexity 2 (=(c))
    g() {}    //          -> complexity 2 (=(c))
  #if D       // +2 (6)
  #endif      //      -> 2 (=(6)) (b) (not affect)
#endif        //      -> 2 (=(1)+(5)) (c)
```

### Uncompilable source code

is not supported.

### K&R style function definition

is not supported.

```c
// NG
void f(a)
    int a;
{}
```

### Language extensions

are not supported.

* https://gcc.gnu.org/onlinedocs/gcc/C-Extensions.html
* etc.

### Splitting token with backslash-newline

is not supported.

```c
// NG
void f() {
    i\
f (); // -> if ();
}
```

### Splitting function definition with preprocessor directive

is not supported.

```c
// OK
#if A
    #if B
    #endif
f (
    #if C
    #endif
) {
    #if D
    #endif
}
    #if E
    #endif
#endif
```

```c
// NG
f()
#if A
{}
#else
{}
#endif

// NG
g() {
#if B
}
#else
}
#endif
```

### Unbalanced enclosures

are not supported.

```c
// NG
void f() {
#if 0
    if (a) {
#endif
}
```

```c
// OK
void f() {
#if 0
    if (a) {
#else
    if (b) {
#endif
    }
}
```

### Missing semicolon

is not supported.

```c
// NG
#define A(n) for (int i=0; i<n; i++) { printf("\n"); }
void f() {
    if (a)
        A(5) // consider function call; expression statement ends with semicolon.
    if (b);
}
```

## Reference

* Cognitive Complexity (5 April 2021, Version 1.5)
  * https://www.sonarsource.com/resources/white-papers/cognitive-complexity/
  * https://www.sonarsource.com/docs/CognitiveComplexity.pdf

* clang-tidy - readability-function-cognitive-complexity
  * https://clang.llvm.org/extra/clang-tidy/checks/readability-function-cognitive-complexity.html

## License

BSD 3-Clause License. See LICENSE file for details.
