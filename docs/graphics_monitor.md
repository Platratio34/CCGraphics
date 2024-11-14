# `graphics_monitor` Peripheral

`graphics_monitor` implements all terminal methods of a terminal redirect (documentation [here](https://tweaked.cc/module/term.html#ty:Redirect))

## Methods

| Signature | Return | Description |
|-----------|--------|-------------|
| `getNewFrameBuffer()` | `FrameBuffer` | Get a FrameBuffer that is the size of the monitor |
| `setFrameBuffer(frame)` | `boolean` | Set the current frame buffer for the monitor |
| `getWidth()` | `integer` | Get the width of the monitor in pixels |
| `getHeight()` | `integer` | Get the height of the monitor in pixels |
| `makeTerm()` | `nil` | Put the monitor into terminal mode |
| `makeGraphics()` | `nil` | Put the monitor into graphics mode |
| `setTermTextSize(size)` | `nil` | Set the font size of the monitor in terminal mode |

## Detail

### `getNewFrameBuffer(): FrameBuffer`

Get a FrameBuffer that is the size of the monitor

Can be called in either mode.

#### Returns
- [`FrameBuffer`](FrameBuffer.md) - The new frame buffer

---

### `setFrameBuffer(frame: FrameBuffer): boolean`

Set the current frame buffer for the monitor. Function is rate limited to 5 times per second.

Function will return false if monitor is in terminal mode.

#### Parameters
- `frame: FrameBuffer` - Frame buffer to set. Must be the same size as the monitor. Accepts a table representing a frame buffer

#### Returns
- `boolean` - If the buffer was set.

#### Throws
- If the provided buffer could not be converted to a FrameBuffer

#### See
- [`FrameBuffer.getTable()`](FrameBuffer.md#gettable-table) and [`graphics.tableToFrameBuffer(tbl)`](graphics.md#tabletoframebuffertbl-table-framebuffer)

---

### `getWidth(): integer`

Get the width of the monitor in pixels.

Can be called in either mode.

#### Returns
- `integer` - Width of the monitor in pixels

---

### `getHeight(): integer`

Get the height of the monitor in pixels.

Can be called in either mode.

#### Returns
- `integer` - Height of the monitor in pixels

---

### `makeTerm(): nil`

Put the monitor into terminal mode.

When in terminal mode, the frame buffer can't be set directly

---

### `makeGraphics(): nil`

Put the monitor into graphics mode.

When in graphics mode calls to the terminal functions result in undefined result.

---

### `setTermTextSize(size: integer): nil`

Set the font size of the monitor in terminal mode.

Can be called in either mode.

Currently only sizes of `7`, `9`, and `11` are allowed.

#### Parameters
- `size: integer` - The pixel height of the text when monitor is in terminal mode.

#### Throws
- If the provided size is not allowed

---