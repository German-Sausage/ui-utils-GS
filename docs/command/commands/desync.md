# `desync` Command

## NAME
`desync` — Desync from the server

## SYNOPSIS
```
desync
```

## DESCRIPTION
The `desync` command sends a `CloseHandledScreenC2SPacket` to the server to close the current screen handler server-side, while keeping the screen open client-side.

It checks that the player and network handler are not null before proceeding. The sync ID from the player's current screen handler is used to construct the packet.

## EXAMPLES
```
desync
→ Result: Sent CloseHandledScreenC2SPacket for syncId: <current-sync-id>
```
