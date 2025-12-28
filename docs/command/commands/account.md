# `account` Command

## NAME
`account` — Manage user account settings for Minecraft.

## SYNOPSIS
```
account <set|dump> [args]
```

## DESCRIPTION
The `account` command allows you to manage user account settings, such as setting a UUID, username, or session, or dumping the current account information.

### Subcommands:
- **`set <uuid|username|session> <value>`**  
  Set the account's **UUID**, **username**, or **session** to the specified value.

    - `uuid`: Set a new UUID for the account.
    - `username`: Set a new username for the account.
    - `session`: Set a new session token for the account.

- **`dump`**  
  Dump the current account information, including **UUID**, **username**, and **access token**.

## USAGE

### Set Account Details:

```
account set uuid <uuid>
```
Example:
```
account set uuid 123e4567-e89b-12d3-a456-426614174000
→ Sets the account's UUID to the provided UUID.
```

```
account set username <username>
```
Example:
```
account set username Steve
→ Sets the account's username to "Steve".
```

```
account set session <session_token>
```
Example:
```
account set session abcdef1234567890
→ Sets the account's session token to the provided session token.
```

### Dump Account Information:

```
account dump
```
Example:
```
account dump
→ Displays the current account details, including UUID, username, and access token.
```

## ## EXAMPLES

 Set Account Details:
```
account set uuid 123e4567-e89b-12d3-a456-426614174000
→ Sets the account's UUID to the provided UUID.

account set username Steve
→ Changes the account's username to "Steve".

account set session abcdef1234567890
→ Sets the session token for the account.

account dump
→ Displays the current account details, including UUID, username, and access token.
```

### One-liner to Log In:
_(A favorite of mine for working in development environments <3)_

You can quickly log into an account by chaining the `account set` commands in a single line, like this:

```
account set session abcdef1234567890 && account set uuid 123e4567-e89b-12d3-a456-426614174000 && account set username Steve
→ Logs into the account using the provided session, UUID, and username in one command chain.
```

This one-liner will:
1. Set the session token (`account set session abcdef1234567890`)
2. Set the UUID (`account set uuid 123e4567-e89b-12d3-a456-426614174000`)
3. Set the username (`account set username Steve`)

All these steps happen in sequence, allowing you to log in without needing to input each command separately.

## ERROR MESSAGES

- **Command must be a string**: The account type (uuid, username, or session) and value must be provided as strings.
- **Usage: account set <uuid|username|session> <value>**: You need to provide both a valid account type (uuid, username, or session) and a value.
- **Invalid account type**: The provided account type is not valid. Make sure to specify "uuid", "username", or "session".

## INTERNAL DETAILS
The `set` subcommand works by creating a new **Session** with the provided value for UUID, username, or session token, and then applies the session by calling `setSession`. This updates the account details, including authentication service and user API service. The `dump` subcommand simply retrieves and displays the current session details.

