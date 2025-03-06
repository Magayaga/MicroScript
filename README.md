# `MicroScript`

**MicroScript**, often abbreviated as **MUS** and **μs** is a high-level programming language. It was originally written in **Java** programming language.

## Examples

### "Hello, World!" program

The following shows how a **"Hello, World!"** program is written in MicroScript programming language:

```js
// "Hello, World!" program
console.write("Hello, World!");
```

### Arithmetic expressions

The following shows how to the addition, substraction, multiplication, and division program using MicroScript programming language:

```js
// Adding numbers
console.write(25 + 19);

// Subtracting numbers
console.write(36 - 14);

// Multiplying numbers
console.write(45 * 12);

// Dividing numbers
console.write(1028 / 16);
```

### Function

The following shows how a **"Hello, World!"** program with function is written in MicroScript programming language:

```js
// Function to compute the "Hello, World!" program
function main() {
    console.write("Hello, World!");
    return 0;
};

main();
```

The following shows how a **square** function is written in MicroScript programming language:

```js
// Function to compute the square
function square(number) {
    return number * number;
}

// Main function
function main() {
    console.write(square(14));
};

main();
```

### Call main function

```js
main();
```

This executes the `main` function, starting the program.

### Import modules and Libraries

You need to use the `import` keyword along with the desired module name.

```ts
// Import modules
import "math"

// Main function
function main() {
    console.write(math::PI);
    console.write(math::E);
    console.write(math::sqrt(16));
}

main();
```

## Copyright

Copyright (c) 2024-2025 [Cyril John Magayaga](https://github.com/magayaga). Licensed under the [MIT license](LICENSE).
