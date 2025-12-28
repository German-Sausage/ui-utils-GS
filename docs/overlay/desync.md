# Desync

## What It Does
Sends a **CloseHandledScreenC2SPacket** packet to the server while keeping the screen open on the client side.

This causes a desync between your client and the server about the screen state.

## How to Use
- Click the **Desync** button while a GUI screen is open.
- The server will think your screen closed, but you will still see and interact with it.

## Notes
- Not particularly useful for servers that don't require client sided mods.

## Tips
- After desync-ing, the server will likely reject any further GUI interactions. 
- This feature is more useful for servers that require badly coded client sided mods that send packets to a server sided mod that is also badly coded.