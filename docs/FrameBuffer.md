# `FrameBuffer`

Arbitrary frame buffers can be created with `graphics.FrameBuffer(width, height)`.

## Methods

| Signature | Return | Description |
|---|---|---|
| `setPixel(x,y,color)` | `nil` | Set the color of a given pixel |
| `getPixel(x,y)` | `integer` | Set the color of a pixel |
| `drawBox(x,y,w,h,color)` | `nil` | Draw a box outline |
| `drawBoxFilled(x,y,w,h,color)` | `nil` | Draw a filled box |
| `drawLine(x1,y1,x2,y2,color)` | `nil` | Draw a line on this buffer |
| `drawBuffer(x,y,buffer2,xOff?,yOff?,w?,h?)` | `nil` | Draw the provided buffer onto this buffer |
| `drawBufferMasked(x,y,buffer2,xOff?,yOff?,w?,h?)` | `nil` | Draw the provided buffer onto this buffer masking by alpha |
| `getWidth()` | `integer` | Get the width of the frame |
| `getHeight()` | `integer` | Get the height of the frame |
| `copy()` | `FrameBuffer` | Makes a deep copy of the frame buffer |
| `inFrame(x,y)` | `bool` | Check if the given position is inside the frame |
| `getTable()` | `table` | Get a table that represents this frame buffer for passing to LUA |

## Detail

### `setPixel(x: number, y: number, color: integer): nil`

Set the color of a pixel.

#### Parameters
- `x: number` - X position of the pixel
- `y: number` - Y position of the pixel
- `color: integer` - Color of the pixel in ARGB8

#### Throws
- If the location is outside the frame buffer

#### See also
- [`graphics.packRGB(r,g,b)`](graphics.md#packrgbr-integer-g-integer-b-integer-integer) and [`graphics.packRGBA(r,g,b,a)`](graphics.md#packrgbar-integer-g-integer-b-integer-a-integer-integer)

---

### `getPixel(x: number, y: number): integer`

Get the color of a pixel.

#### Parameters
- `x: number` - X position of the pixel
- `y: number` - Y position of the pixel

#### Returns
- `integer` - Color of the pixel in ARGB8

#### Throws
- If the location is outside the frame buffer

#### See also
- [`graphics.unpackRGB(color)`](graphics.md#unpackrgbacolor-integer-integer-integer-integer)

---

### `drawBox(x: number, y: number, w: number, h: number, color: integer): nil`

Draw a box outline.

#### Parameters
- `x: number` - X position of the box
- `y: number` - Y position of the box
- `w: number` - Width of the box
- `h: number` - Height of the box
- `color: integer` - Color of the box in ARGB8

#### Throws
- If the box extends outside the frame buffer

#### See also
- [`graphics.packRGB(r,g,b)`](graphics.md#packrgbr-integer-g-integer-b-integer-integer) and [`graphics.packRGBA(r,g,b,a)`](graphics.md#packrgbar-integer-g-integer-b-integer-a-integer-integer)

---

### `drawBoxFilled(x: number, y: number, w: number, h: number, color: integer): nil`

Draw a filled box.

#### Parameters
- `x: number` - X position of the box
- `y: number` - Y position of the box
- `w: number` - Width of the box
- `h: number` - Height of the box
- `color: integer` - Color of the box in ARGB8

#### Throws
- If the box extends outside the frame buffer

#### See also
- [`graphics.packRGB(r,g,b)`](graphics.md#packrgbr-integer-g-integer-b-integer-integer) and [`graphics.packRGBA(r,g,b,a)`](graphics.md#packrgbar-integer-g-integer-b-integer-a-integer-integer)

---

### `drawLine(x1: number, y1: number, x2: number, y2: number, color: integer): nil`

Draw a line between 2 points.

#### Parameters
- `x1: number` - X position of point 1
- `y1: number` - Y position of point 1
- `x2: number` - X position of point 2
- `y2: number` - Y position of point 2
- `color: integer` - Color of the box in ARGB8

#### Throws
- If the line extends outside the frame buffer

#### See also
- [`graphics.packRGB(r,g,b)`](graphics.md#packrgbr-integer-g-integer-b-integer-integer) and [`graphics.packRGBA(r,g,b,a)`](graphics.md#packrgbar-integer-g-integer-b-integer-a-integer-integer)

---

### `drawBuffer(x: number, y: number, buffer2: FrameBuffer, xOff: number?, yOff: number?, w: number?, h: number?): nil`

Draw another buffer onto this buffer.

#### Parameters
- `x: number` - X position on **THIS** buffer to start drawing
- `y: number` - Y position on **THIS** buffer to start drawing
- `buffer2: FrameBuffer` - Buffer to draw. (Can be table representation of a frame buffer)
- `xOff: number?` - *Optional.* X position on `buffer2` to start drawing from. Defaults to `0`
- `yOff: number?` - *Optional.* Y position on `buffer2` to start drawing from. Defaults to `0`
- `w: number?` - Width of `buffer2` to draw from `xOff`. Defaults to `buffer2.getWidth()`
- `w: number?` - Width of `buffer2` to draw from `xOff`. Defaults to `buffer2.getWidth()`

#### Throws
- If the drawn area extends outside of either frame buffer

#### See also
- [`FrameBuffer.getTable()`](#gettable-table) and [`graphics.tableToFrameBuffer(tbl)`](graphics.md#tabletoframebuffertbl-table-framebuffer)

---

### `drawBufferMasked(x: number, y: number, buffer2: FrameBuffer, xOff: number?, yOff: number?, w: number?, h: number?): nil`

Draw another buffer onto this buffer masking by alpha. Any pixel with an alpha of greater than `0` is drawn, pixels with alpha of `0` are skipped. 

#### Parameters
- `x: number` - X position on **THIS** buffer to start drawing
- `y: number` - Y position on **THIS** buffer to start drawing
- `buffer2: FrameBuffer` - Buffer to draw. (Can be table representation of a frame buffer)
- `xOff: number?` - *Optional.* X position on `buffer2` to start drawing from. Defaults to `0`
- `yOff: number?` - *Optional.* Y position on `buffer2` to start drawing from. Defaults to `0`
- `w: number?` - Width of `buffer2` to draw from `xOff`. Defaults to `buffer2.getWidth()`
- `w: number?` - Width of `buffer2` to draw from `xOff`. Defaults to `buffer2.getWidth()`

#### Throws
- If the drawn area extends outside of either frame buffer, or the provided buffer could not be turned into a FrameBuffer

#### See also
- [`FrameBuffer.getTable()`](#gettable-table) and [`graphics.tableToFrameBuffer(tbl)`](graphics.md#tabletoframebuffertbl-table-framebuffer)

---

### `getWidth(): integer`

Get the width of the frame buffer

#### Returns
- `integer` - Width of the frame buffer

---

### `getHeight(): integer`

Get the height of the frame buffer

#### Returns
- `integer` - Height of the frame buffer

---

### `copy(): FrameBuffer`

Get a deep copy of this frame buffer

#### Returns
- `FrameBuffer` - Deep copy of this frame

---

### `getTable(): table`

Get a table representation of the frame buffer

Example table:
```lua
{
    width = 2,
    height = 2,
    [0] = 0xffffffff,
    [1] = 0xffffffff,
    [2] = 0xffffffff,
    [3] = 0xffffffff,
}
```

#### Returns
- `table` - Table copy of the frame buffer

---

### `inFrame(x: number, y: number): bool`

Check if a given position is within this frame buffer.

#### Parameters
- `x: number` - X position to check
- `y: number` - Y position to check

#### Returns
- `bool` - If the position was inside the frame

---