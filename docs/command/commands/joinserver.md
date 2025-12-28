# `joinserver` Command

## NAME
`joinserver` — Join a server

## SYNOPSIS
```
joinserver <ip>
```

## DESCRIPTION
The `joinserver` command connects the player to a Minecraft server using the specified IP address.

It creates a `ServerInfo` object and uses `ConnectScreen.connect` to initiate the connection. The connection is made asynchronously on the client thread.

## EXAMPLES
```
joinserver 2b2t.org
→ Result: Joining server: 2b2t.org

joinserver play.example.net
→ Result: Joining server: play
