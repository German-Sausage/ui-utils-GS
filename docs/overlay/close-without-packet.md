# Close Without Packet

## What It Does
Softly closes the current GUI screen **without** sending a close packet to the server.

This can be useful when you want to the server to think you're still in a screen, but you want to close it on your side.
Effectively sets the client-sided screen to `null` without notifying the server.

## How to Use
- Click the **Close Without Packet** button in the overlay.
- Your current screen will close immediately on your side only.

## Notes
- The server may still think the screen is open.
- Useful for bypassing forced screen or tricking some plugin mechanics.
