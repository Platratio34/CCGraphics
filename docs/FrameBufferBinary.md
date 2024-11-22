# FrameBufferBinary `fbb` file

## Header

| Byte(s) | Type | Description |
|---|---|---|
| 0 - 3 | utf8 String | File type (`"fbb"`, null terminated) |
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
| 7 | Indexed Color 7/15b mode |

**IF** both the 8b & 16b run length encoding flags are set, RLE is done in 7/15b mode

### Header Table Entry

Header table entry. Entries are repeated until an entry type of `0x0000` if found. Header table can't contain more than one entry of a given type.

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
| 0 - ? | `ARGB8` **OR** `RGB8` | Color. **IF** `opaque` flag is set, type is `RGB8` **ELSE** type is `ARGB8` |

## Special Data Types

### 7/15b unsigned integer: `uint7/15`

**IF** the first (highest) bit is set, it is read as a `uint15` starting after the flag bit.

**ELSE** it is read as a `uint7` starting after the flag bit.

## Data

| Byte(s) | Type | Description |
|---|---|---|
| 0 - 3 | `uint32` | Data Length (Including this) |
| 4 - `DataLength` | Pixel ... | Pixel data |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 - 3 | `ARGB8` | Pixel color |

## Encoding options

### Opaque

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 - 2 | `RGB8` | Pixel color |

### Indexed

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 | `uint8` | Color index |

### Indexed 7/15b

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 | `uint7/15` | Color index |

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
| 2 ... | `uint16`... | Number of additional repetitions of the pixel **IF** previous value was `0xffff` **ELSE** move on to pixel data |
| ? - ? | Pixel | Pixel color |

### Run-Length-Encoding 15b

Active **IF** RLE8 & RLE15 flags are both set

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Pixel (Repeated for every pixel) |
| 0 - 1 | `uint7/15` | Number of repetitions of the pixel (not including first instance) |
| 2 ... | `uint7/15`... | Number of additional repetitions of the pixel **IF** previous value was `0x7fff` **ELSE** move on to pixel data |
| ? - ? | Pixel | Pixel color |

---

# FrameBufferBinarySequence `fbs` file

## Header

| Byte(s) | Type | Description |
|---|---|---|
| 0 - 3 | utf8 String | File type (`"fbs"`, null terminated) |
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
| 0 - 1 | `uint16` | Number of frames defined |
| 2 - 3 | `null` | Padding |
| 4 - ?? | Frame ... | Frames |
|---|---|---|
|  |  | Frame (Repeated for every fame) |
| 0 - 3 | `uint32` | Frame Length (including header)
| 4 - 5 | `uint16` | Frame Number |
| 6 | `uint8` | Number of repetitions (not including first instance) |
| 7 | `uint8` | Frame Type |
| 8 - `FrameLength` | Pixel ... | Pixel data |

### Frame Types

| Type | Name | Description |
|---|---|---|
| `0x00` | Default | Normal frame. If in change only mode, will provide change from previous frame |
| `0x01` | Keyframe | Only applicable in change only mode. Defines a complete frame. **DOES NOT INCLUDE SKIPPED PIXELS** |
| `0x80` | Option | Redefines encoding options for the remaining frames. |
| `0xff` | EndFrame | Ends an indicant stream of frames (See [Frame Stream](#frame-stream) for more information) |

Decoders should ignore the frame number and repetitions of any non-image frame (frames with a type of `0x80` or greater).

### Change Only Mode

| Byte(s) | Type | Description |
|---|---|---|
| 0 | `uint8` | Number of pixels to skip |
| 1 ... | `uint8`... | Number of additional pixels to skip **IF** previous byte was `0xff` **ELSE** move on to pixel data |
| ? - ? | Pixel | Pixel |

### Option Frame

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Frame (Repeated for every fame) |
| 0 - 3 | `uint32` | Frame Length (including header)
| 4 - 5 | `uint16` | Frame Number (`0xffff`) |
| 6 | `uint8` | Unused |
| 7 | `uint8` | Frame Type (`0x10`) |
| 8 | ByteFlags | Header encoding flags (See [FBB Header](#header-flags) and [FBS Header](#header-flags-1) for specifics) |
| 10 ... | HeaderTableEntry ... | Header Table Entries. Last entry will have a type of `0x0000` (See [Header Table Entry](#header-table-entry) for more information) |


### End Frame

| Byte(s) | Type | Description |
|---|---|---|
|  |  | Frame (Repeated for every fame) |
| 0 - 3 | `uint32` | Frame Length (including header)
| 4 - 5 | `uint16` | Frame Number (`0xffff`) |
| 6 | `uint8` | Unused |
| 7 | `uint8` | Frame Type (`0xff`) |

## Frame Stream

If the number of frames is `0xffff`, it is considered to have infinite frames **UNTIL** a frame of type `EndFrame` if found.

Stream chunks **MUST NOT** split frames or header information. Splits may only be between frames.

`frameNumber` & `framesDefined` are un-used for streams.