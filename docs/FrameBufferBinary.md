# FrameBufferBinary `fbb` file

## Header

| Byte(s) | Type | Description |
|---|---|---|
| 0 - 3 | Byte String | File type (`"fbb "`) |
| 4 - 7 | `uint32` | Pointer to start of Data section |
| 8 - 9 | `uint16` | Width |
| a - b | `uint16` | Height |
| c | Byte Flags | Header Flags |
| d - f | `null` | Padding |
| 10 - ?? | HeaderTableEntry List | Header Table Entries |

### Header Flags

| Bit | Description |
|---|---|
| 0 | Run-Length-Encoding 8b |
| 1 | Run-Length-Encoding 16b |
| 2 | No Alpha (Incompatible w/ Indexed) |
| 3 | Indexed Color (Incompatible w/ No Alpha) |

### Header Table

Header entry. Entries are repeated until an entry type of `0x0000` if found. Header table can't contain more than one entry of a given type.

| Byte(s) | Type | Description |
|---|---|---|
| 0 - 1 | `uint16` | Entry Type |
| 2 - 3 | `uint16` | Entry length in bytes (including header) |
| 3 - `EntryLength` | Any | Header data |

#### Color Index Table

| Byte(s) | Type | Description |
|---|---|---|
| 0 - 1 | `uint16` | Entry Type (`0x0001`) |
| 2 - 3 | `uint16` | Entry length in bytes (including header) |
|---|---|---|
|  |  | Indexed Color Data (Repeated as needed within entry length) |
| 0 | `uint8` | Color index |
| 1 | `uint8` | Red value |
| 2 | `uint8` | Green value |
| 3 | `uint8` | Blue value |

## Data

| Byte(s) | Type | Description |
|---|---|---|
| 0 - 3 | `uint32` | Data Length (Including this) |
| 4 - `DataLength` | Pixel ... | Pixel data |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 - 3 | `ARGB8` | Pixel color |

## Encoding options

### No Alpha

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 - 2 | RGB8 | Pixel color |

### Indexed

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 | `uint8` | Color index |

---

### Run-Length-Encoding 8b

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 | `uint8` | Number of repetitions of the pixel (not including first instance) |
| 1 - 4 | `ARGB8` | Pixel color |

### Run-Length-Encoding 16b

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 - 1 | `uint16` | Number of repetitions of the pixel (not including first instance) |
| 2 - 5 | `ARGB8` | Pixel color |

---

### Run-Length-Encoding 8b, No Alpha

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 | `uint8` | Number of repetitions of the pixel (not including first instance) |
| 1 - 3 | `RGB8` | Pixel color |

### Run-Length-Encoding 16b, No Alpha

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 - 1 | `uint16` | Number of repetitions of the pixel (not including first instance) |
| 2 - 4 | `RGB8` | Pixel color |

---

### Run-Length-Encoding 8b, Indexed

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 | `uint8` | Number of repetitions of the pixel (not including first instance) |
| 1 | `uint8` | Color index |

### Run-Length-Encoding 16b, Indexed

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 - 1 | `uint16` | Number of repetitions of the pixel (not including first instance) |
| 2 | `uint8` | Color index |

---

# FrameBufferBinarySequence `fbs` file

## Header

| Byte(s) | Type | Description |
|---|---|---|
| 0 - 3 | Byte String | File type (`"fbs "`) |
| 4 - 7 | `uint32` | Pointer to start of Data section |
| 8 - 9 | `uint16` | Width |
| a - b | `uint16` | Height |
| c | Byte Flags | Header Flags |
| d | null | Padding |
| e - f | `uint16` | Number of frames |
| 10 - ?? | HeaderTableEntry ... | Header Table |

### Header Flags

| Bit | Description |
|---|---|
| 4 | Change Only 8b |
| 5 | Change Only 16b |
| 6 | Change Only 15b |
| 7 | Change Only Var |

## Data

| Byte(s) | Type | Description |
|---|---|---|
| 0 - 1 | uint16 | Number of frames defined |
| 2 - 3 | `null` | Padding |
| 4 - ?? | Frame ... | Frames |
|---|---|---|
|  |  | Frame (Repeated for every fame) |
| 0 - 3 | `uint32` | Frame Length (including header)
| 4 - 5 | `uint16` | Frame Number |
| 6 - 7 | `uint16` | Number of repetitions (not including first instance) |
| 8 - `FrameLength` | Pixel ... | Pixel data |

### Change Only 8b

| Byte(s) | Type | Description |
|---|---|---|
| 0 | `uint8` | Number of pixels to skip |
| 2 - ? | Pixel | Pixel |

### Change Only 16b

| Byte(s) | Type | Description |
|---|---|---|
| 0 - 1 | `uint16` | Number of pixels to skip |
| 2 - ? | Pixel | Pixel |

### Change Only 15b

| Byte(s) | Type | Description |
|---|---|---|
| 0 - ? | `uint7` **OR** `uint15` | Number of pixels to skip. If bit `0` is set, treat as `uint15` |
| ? - ? | Pixel | Pixel |

### Change Only Var

| Byte(s) | Type | Description |
|---|---|---|
| 0 | `uint8` | Number of pixels to skip |
| 1 ... | `uint8`... | Number of pixels to skip **IF** previous byte was `0xff` **ELSE** move on to pixel data |
| ? - ? | Pixel | Pixel |