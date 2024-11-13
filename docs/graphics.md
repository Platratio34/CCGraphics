# `graphics` API

```lua
local graphics = require('graphics')
```

Graphics API

## Methods

| Signature | Return | Description |
|-----------|--------|-------------|
| `FrameBuffer()` | `FrameBuffer` | Make a new FrameBuffer of the provided size |
| `tableToFrameBuffer(tbl)` | `FrameBuffer` | Convert a table to a FrameBuffer |
| `packRGB(r,g,b)` | `integer` | Pack Red, Green, and Blue into an ARGB8 integer |
| `packRGBA(r,g,b,a)` | `integer` |  Pack Red, Green, Blue, and Alpha into an ARGB8 integer|
| `unpackRGBA(color)` | `integer,integer,integer` | Unpack an ARGB8 color into its components |
| `getTextRenderer(name,size)` | `TextRenderer` | Get a new text renderer for the specified font |
| `loadImage(image)` | `FrameBuffer` | Load an image to a frame buffer |
| `loadImageString(image)` | `FrameBuffer` | Load an image to a frame buffer |

## Detail

### `FrameBuffer(): FrameBuffer`

Make a new FrameBuffer of the provided size

#### Returns
- `FrameBuffer` - New frame buffer

---

### `tableToFrameBuffer(tbl: table): FrameBuffer`

Convert a table to a FrameBuffer.

The table must contain numeric values for `width`, `height`, and a value for each pixel, starting index at `0` in row column format.

Example table:
```lua
{
    width = 2,
    height = 2,
    [0] = 0xff000000 -- (0,0)
    [1] = 0xff000000 -- (1,0)
    [2] = 0xff000000 -- (0,1)
    [3] = 0xff000000 -- (1,1)
}
```

### Parameters
- `tbl: table` - Table representing the frame buffer

#### Returns
- `FrameBuffer` - Frame buffer from table

---

### `packRGB(r: integer, g: integer, b: integer): integer`

Packs Red, Green, & Blue into an ARGB8 integer for use with FrameBuffers. Sets Alpha to `0xff`.

#### Parameters
- `r: integer` - Red component ( 0 - 255 )
- `g: integer` - Green component ( 0 - 255 )
- `b: integer` - Blue component ( 0 - 255 )

#### Returns
- `integer` - RGBA8 color

#### Throws
- If any competent was out of range `0x00` - `0xff`

#### See
- `graphics.unpackRGBA(color)`

---

### `packRGBA(r: integer, g: integer, b: integer, a: integer): integer`

Packs Red, Green, Blue, and Alpha into an ARGB8 integer for use with FrameBuffers.

#### Parameters
- `r: integer` - Red component ( 0 - 255 )
- `g: integer` - Green component ( 0 - 255 )
- `b: integer` - Blue component ( 0 - 255 )
- `a: integer` - Alpha component ( 0 - 255 )

#### Returns
- `integer` - RGBA8 color

#### Throws
- If any competent was out of range `0x00` - `0xff`

#### See
- `graphics.unpackRGBA(color)`

---

### `packRGBA(color: integer): integer, integer, integer`

Unpacks Red, Green, Blue, and Alpha from an ARGB8 integer.

#### Parameters
- `color: integer` - ARGB8 color to unpack

#### Returns
- `integer` - Red component ( 0 - 255 )
- `integer` - Green component ( 0 - 255 )
- `integer` - Blue component ( 0 - 255 )
- `integer` - Alpha component ( 0 - 255 )

#### See
- `graphics.packRGB(r,g,b)` and `graphics.packRGBA(r,g,b)`

---

### `getTextRenderer(name: string, size: integer): TextRenderer`

Get a new text renderer for the specified font.

Included fonts:
- `mono` in a size of `7`, `9`, and `11` pixels
- `departure_mono` in a size of `8` pixels

#### Parameters
- `name: string` - Font name
- `size: integer` - Font height in pixels

#### Returns
- `TextRenderer` - Text renderer for the font

#### Throws
- If the requested font doesn't exist

---

### `loadImage(image: int[]): FrameBuffer`

Load an image as a FrameBuffer.

Works with byte array of PNG or JPEG images.

#### Parameters
- `image: int[]` - PNG or JPEG as byte array
- `size: integer` - Font height in pixels

#### Returns
- `FrameBuffer` - Image loaded to a frame buffer

#### Throws
- If the image was invalid

---

### `loadImageString(image: string): FrameBuffer`

Load an image as a FrameBuffer.

Works with byte array string of PNG or JPEG images, such as from (https://tweaked.cc/module/fs.html#ty:ReadHandle:readAll)[`ReadHandle.readAll()`].

#### Parameters
- `image: string` - PNG or JPEG as byte array string
- `size: integer` - Font height in pixels

#### Returns
- `FrameBuffer` - Image loaded to a frame buffer

#### Throws
- If the image was invalid

---