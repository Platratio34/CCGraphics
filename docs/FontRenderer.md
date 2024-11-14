# `FontRenderer`

Font Renderer to rasterize text to Frame Buffers

## Methods

| Signature | Return | Description |
|---|---|---|
| `rasterize(text,color?)` | `FrameBuffer` | Rasterize the provided text to a frame buffer |
| `getTextSize(text)` | `integer,integer` | Get the size of text if it were rasterized |

## Detail

### `rasterize(text: string, color: integer?): FrameBuffer`

Rasterize the provided text to a frame buffer

#### Parameters
- `text: string` - Text to rasterize
- `color: integer?` - *Optional.* Color of the rasterized text. Defaults to `0xffffffff`

#### Returns
- `FrameBuffer` - Frame buffer of rasterized text

---

### `getTextSize(text: string): integer, integer`

Get the size of text if it were rasterized

#### Parameters
- `text: string` - Text to calculate size of

#### Returns
- `integer` - Width of the rasterized text
- `integer` - Height of the rasterized text

---
