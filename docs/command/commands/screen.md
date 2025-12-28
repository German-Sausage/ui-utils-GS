# `screen` Command

## NAME
`screen` — Manage screen saver slots

## SYNOPSIS
```
screen <save|load|list|info> <slot> [args]
```

## DESCRIPTION
The `screen` command allows you to manage your saved screens with several actions. You can **save**, **load**, **list**, or get **info** about a screen slot. Below are the options you can use with this command:

### `save <slot>`
Save the current screen to the specified slot.

- **Note**: If you use the **Save GUI tool** in the overlay, the screen will be automatically saved to the "default" slot.

### `load <slot>`
Load a previously saved screen from the specified slot.

### `list`
List all available saved screen slots.

### `info <slot>`
Show information about the screen saved in the specified slot.

## USAGE

### Save a Screen:
```
screen save <slot>
```
Example:
```
screen save slot1
→ Saves the current screen to "slot1"
```

- **Using the Save GUI Tool**: If you use the **Save GUI tool** in the overlay, the screen will be saved to the "default" slot by default.

### Load a Screen:
```
screen load <slot>
```
Example:
```
screen load slot1
→ Loads the screen saved in "slot1"
```

### List Saved Screens:
```
screen list
```
Example:
```
screen list
→ Displays a list of all saved screen slots
```

### Get Info about a Saved Screen:
```
screen info <slot>
```
Example:
```
screen info slot1
→ Displays information about the screen saved in "slot1"
```

## EXAMPLES

```
screen save slot1
→ Saves the current screen in the "slot1" slot.

screen load slot1
→ Loads the screen from "slot1".

screen list
→ Lists all saved screen slots, e.g., "slot1 slot2".

screen info slot1
→ Displays info about the screen saved in "slot1".
```

## ERROR MESSAGES

- **No slot provided:** You need to specify a slot name when saving, loading, or retrieving info.
- **Command must be a string:** The slot name should be a valid string.
- **Error saving screen:** This could happen if there is a problem saving the screen (e.g., invalid slot name).
- **Error loading screen:** This could happen if the slot doesn't exist or there’s an issue with loading the screen.
