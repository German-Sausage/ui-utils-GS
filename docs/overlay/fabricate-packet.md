# Fabricate Packet

## What It Does
The Fabricate Packet widget lets you **manually create and send inventory packets** or **button packets** to the server.

You can simulate different actions like picking up an item, swapping, cloning, and more — all while controlling **how many times** the packet is sent.

This is a **powerful and advanced tool** mainly for experienced users.

---

## How to Use

1. **Expand the Fabricate Packet Widget**
    - Click the **Fabricate Packet** button in the overlay.
    - Click the small `+` icon to expand and show full options.

2. **Set Up Your Packet**
    - **Action**: Choose the type of inventory action you want (Pickup, Quick Move, Swap, Clone, Throw, Quick Craft, Pickup All).
    - **Sync ID**: Automatically fills in your current screen’s ID, but you **can** edit it.
    - **Revision**: Automatically fills from your screen’s revision, but you **can** edit it.
    - **Slot**: Select the slot you want to perform the action on (using the Slot Manager).
      - You can use the slot manager to display slot ID's, or the Pick Slot button (Shown as a dotted-boxed **ID**) to click on a slot and automatically pick it.
      - You can also type in other slot ID's manually.
    - **Button**: ID for used in creating **SLOT** packets, where 0 is left-click and 1 is right-click.
    - **Button ID**: The button ID on the screen used in **button** clicks.
    - **Times to Send**: How many times you want to send this packet. (Default: 1)

3. **Send the Packet**
    - Click **Slot** to send an inventory-related packet using the selected action and slot.
    - Click **Button** to send a button-click packet to click a button on a screen.

4. **Collapse the Widget**
    - Click the `-` button (now visible where the `+` was) to hide the extra options again.

---

## What Each Field Means

| Field             | Purpose |
| ----------------- | ------- |
| **Action**         | The type of inventory click (pickup, swap, throw, etc.) |
| **Sync ID**        | The ID of your current screen's handler (usually set automatically) |
| **Revision**       | The current version of your screen state (auto-filled) |
| **Slot**           | The inventory slot to interact with |
| **Button**         | The button ID for the action (0 = left-click, 1 = right-click) |
| **Button ID**      | ID of button to press on the screen |
| **Times to Send**  | How many times to repeat the packet action |

---

## Example Usage

### **Pick up 28 dirt from a full stack of dirt in slot 10**
    1. Expand Fabricate Packet.
    2. Set **Action** to **Pickup**.
    3. Set Button to `1` (right-click).
    4. Set **Slot** to `10`.
    5. Set **Times to Send** to `5`.
    6. Click **Slot** to send.

#### Why this works:
As button is set to 1, it effectively right-clicks the dirt 5 times, which if you do ingame also leaves you with 28 dirt on your cursor.

### **Enchanting an item**
    1. Expand Fabricate Packet.
    2. Set the Button field to `1` (2nd Enchantment Table button).
    3. Click "Button" button to send.

#### Why this works:
This sends the button click to the server, as if you clicked the button yourself.
Buttons in screens usually start from 0, as for the enchanting table, the level 1 enchant is button 0, level 2 is button 1, and level 3 is button 2.

---

## Notes
- **Sync ID and Revision** are pulled from your currently open screen but can be manually edited if needed (ex to preform actions on a saved screen)
- **Slot Manager** lets you pick any slot, and display the slot ID's visually.
- Spamming packets with high Times to Send are sent in the same tick.
- When using the Button feature, only the Button ID field is used, all other inputs are ignored.
- When using the Slot feature, the Button ID field is ignored.

---

## Tips
- Start with 1-time sends to test safely before increasing.
- Combine with **Delay Packets** to stack multiple actions invisibly and then send them all at once.
- Useful for advanced duplication tricks, interaction bypasses, or just experimenting with packet behavior.

---

✅ Fabricate Packet is a **powerful tool** — use it wisely!
