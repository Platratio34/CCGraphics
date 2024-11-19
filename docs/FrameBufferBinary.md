# FrameBufferBinary `fbb` file

## Header

| Byte(s) | Type | Description |
|---|---|---|
| 0 - 3 | utf8 String | File type (`"fbb "`) |
| 4 - 7 | `uint32` | Pointer to start of Data section |
| 8 - 9 | `uint16` | Width |
| a - b | `uint16` | Height |
| c | ByteFlags | Header Flags |
| d - f | `null` | Padding |
| 10 ... | HeaderTableEntry ... | Header Table Entries. Last entry will have a type of `0x0000` |

### Header Flags

| Bit | Description |
|---|---|
| 0 | Run-Length-Encoding 8b |
| 1 | Run-Length-Encoding 16b |
| 2 | Opaque |
| 3 | Indexed Color |

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
| 0 - ? | `ARGB8` **OR** `RGB8` | Color. **IF** not `opaque` type is `ARGB8` **ELSE** type is `RGB8` |

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
| 0 - 2 | `RGB8` | Pixel color |

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
| 1 ... | `uint8`... | Number of additional repetitions of the pixel **IF** previous byte was `0xff` **ELSE** move on to pixel data |
| ? - ? | Pixel | Pixel color |

### Run-Length-Encoding 16b

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 - 1 | `uint16` | Number of repetitions of the pixel (not including first instance) |
| 1 ... | `uint16`... | Number of additional repetitions of the pixel **IF** previous value was `0xffff` **ELSE** move on to pixel data |
| ? - ? | Pixel | Pixel color |

---

# FrameBufferBinarySequence `fbs` file

## Header

| Byte(s) | Type | Description |
|---|---|---|
| 0 - 3 | utf8 String | File type (`"fbs "`) |
| 4 - 7 | `uint32` | Pointer to start of Data section |
| 8 - 9 | `uint16` | Width |
| a - b | `uint16` | Height |
| c | ByteFlags | Header Flags |
| d | `null` | Padding |
| e - f | `uint16` | Number of frames |
| 10 ... | HeaderTableEntry ... | Header Table Entries. Last entry will have a type of `0x0000` |

### Header Flags

| Bit | Description |
|---|---|
| 4 | Change Only |

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
| 6 | `uint8` | Number of repetitions (not including first instance) |
| 7 | `uint8` | Frame Type |
| 8 - `FrameLength` | Pixel ... | Pixel data |

### Change Only

| Byte(s) | Type | Description |
|---|---|---|
| 0 | `uint8` | Number of pixels to skip |
| 1 ... | `uint8`... | Number of additional pixels to skip **IF** previous byte was `0xff` **ELSE** move on to pixel data |
| ? - ? | Pixel | Pixel |