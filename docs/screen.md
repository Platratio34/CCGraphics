# `screen` Global

LUA type: `ScreenAPI`

Global for interacting with the screen of a Graphics Computer

## Methods

| Signature | Return | Description |
|-----------|--------|-------------|
| `setFrame(frame)` | `nil` | Set the current frame buffer for the screen |
| `setGraphicsMode()` | `nil` | Put the screens in graphics mode |
| `setTerminalMode()` | `nil` |  |
| `getWidth()` | `integer` |  |
| `getHeight()` | `integer` |  |
| `getFrameBuffer()` | `FrameBuffer` |  |

## Detail

### `setFrame(frame: FrameBuffer): nil`

Set the current frame buffer for the screen

#### Parameters
- `frame: FrameBuffer` - Frame buffer to set

#### Throws
- If the frame buffer was not the size of the screen

#### See Also
- [`screen.getFrameBuffer()`](#getframebuffer-framebuffer) to get a frame buffer the size of the screen
- [`screen.getWidth()`](#getwidth-integer) & [`screen.getHeight()`](#getheight-integer) to get the size of the screen

---

### `setGraphicsMode(): nil`

Put the screen in graphics mode (ie. setting the frame buffer directly)

#### See Also
- [`screen.setTerminalMode()`](#setterminalmode-nil) to put the screen in terminal mode

---

### `setTerminalMode(): nil`

Puts the screen in terminal mode

#### See Also
- [`screen.setGraphicsMode()`](#setgraphicsmode-nil) to put the screen in graphics mode

---

### `getWidth(): integer`

Gets the width of the screen in pixels

#### Returns
- `integer` - Screen width

---

### `getHeight(): integer`

Gets the height of the screen in pixels

#### Returns
- `integer` - Screen height

---

### `getFrameBuffer(): FrameBuffer`

Get a new frame buffer the size of this screen

#### Returns
- [`FrameBuffer`](FrameBuffer.md) - New frame buffer

---