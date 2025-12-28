# Command System – User Guide

The `CommandSystem` allows you to execute various commands using a simple text-based interface. This system supports chained commands and provides basic error reporting.

## Getting Started

To use the command system, simply input a command string. Commands follow this basic structure:

```
<command> [arg1] [arg2] ...
```

You can also chain multiple commands using `&&`:

```
command1 arg1 && command2 arg2
```

If any command in the chain fails, execution halts.

---

## Available Commands

For usage information and argument formats, see the documentation for each individual command in the [`commands/`](./commands) directory:

- [`echo`](./commands/echo.md)
- [`man`](./commands/man.md)
- [`help`](./commands/help.md)
- [`math`](./commands/math.md)
- [`chat`](./commands/chat.md)
- [`screen`](./commands/screen.md)
- [`account`](./commands/account.md)
- [`close`](./commands/close.md)
- [`desync`](./commands/desync.md)
- [`joinserver`](./commands/joinserver.md)

---

## Chaining Commands

You can chain multiple commands using `&&`. Example:

```
echo Hello && math 5 + 3 && close
```

If `math 5 + 3` fails, `close` will not execute.

---

## Command Errors

If an unknown command is entered, or if a command fails, you'll receive an error message in the format:

```
Unknown command: <name>
```

Or:

```
Error executing command: <message>, <exception>
```

---

## Getting Help

Use the following to explore the command system:

- `help` — List all commands.
- `man <command>` — Show usage/help for a specific command.

Example:

```
man math
```

---

## Example Usage

```bash
> help
> echo Hello, world!
> math 2 * (3 + 4)
> chat "This is a test"
> joinserver play.example.com
```

---
