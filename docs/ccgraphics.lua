---@meta

---@class FrameBuffer LUA Frame buffer
local FrameBuffer = {}

--- Set the color of a pixel.
--- <br/><br/>
--- <b>Throws:</b> If the location is outside the frame buffer
---@param x number X position of the pixel
---@param y number Y position of the pixel
---@param color integer Color of the pixel in ARGB8
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.packRGBA To pack color into an ARGB8 integer
function FrameBuffer.setPixel(x, y, color) end

--- Get the color of a pixel.
--- <br/><br/>
--- <b>Throws:</b> If the location is outside the frame buffer
---@param x number X position of the pixel
---@param y number Y position of the pixel
---@return integer color Color of the pixel in ARGB8
---@see GraphicsAPI.unpackRGBA To unpack an ARGB8 integer
function FrameBuffer.getPixel(x, y) end

--- Draw a box outline.
--- <br/><br/>
--- <b>Throws:</b> If the box extends outside the frame buffer
---@param x number X position of the box
---@param y number Y position of the box
---@param w number Width of the box
---@param h number Height of the box
---@param color integer Color of the box in ARGB8
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.packRGBA To pack color into an ARGB8 integer
function FrameBuffer.drawBox(x, y, w, h, color) end

--- Draw a filled box.
--- <br/><br/>
--- <b>Throws:</b> If the box extends outside the frame buffer
---@param x number X position of the box
---@param y number Y position of the box
---@param w number Width of the box
---@param h number Height of the box
---@param color integer Color of the box in ARGB8
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.packRGBA To pack color into an ARGB8 integer
function FrameBuffer.drawBoxFilled(x, y, w, h, color) end

--- Draw a line between 2 points.
--- <br/><br/>
--- <b>Throws:</b> If the line extends outside the frame buffer
---@param x1 number X position of point 1
---@param y1 number Y position of point 1
---@param x2 number X position of point 2
---@param y2 number Y position of point 2
---@param color integer Color of the line in ARGB8
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.packRGBA To pack color into an ARGB8 integer
function FrameBuffer.drawLine(x1, y1, x2, y2, color) end

--- Draw another buffer onto this buffer.
--- <br/><br/>
--- <b>Throws:</b> If the drawn area extends outside of either frame buffer
---@param x number X position on **THIS** buffer to start drawing
---@param y number Y position on **THIS** buffer to start drawing
---@param buffer2 FrameBuffer|table Buffer to draw. (Can be table representation of a frame buffer)
---@param xOff? number *Optional.* X position on `buffer2` to start drawing from. Defaults to `0`
---@param yOff? number *Optional.* Y position on `buffer2` to start drawing from. Defaults to `0`
---@param w? number *Optional.* Width of `buffer2` to draw from `xOff`. Defaults to `buffer2.getWidth() - xOff`
---@param h? number *Optional.* Height of `buffer2` to draw from `xOff`. Defaults to `buffer2.getHeight() - yOff`
---@see FrameBuffer.getTable To convert a FrameBuffer to a table
---@see GraphicsAPI.tableToFrameBuffer To create a FrameBuffer from a table
function FrameBuffer.drawBuffer(x, y, buffer2, xOff, yOff, w, h) end

--- Draw another buffer onto this buffer masking by alpha.
--- <br>
--- Any pixel with an alpha of greater than 0 is drawn, pixels with alpha of 0 are skipped.
--- <br/><br/>
--- <b>Throws:</b> If the drawn area extends outside of either frame buffer
---@param x number X position on **THIS** buffer to start drawing
---@param y number Y position on **THIS** buffer to start drawing
---@param buffer2 FrameBuffer|table Buffer to draw. (Can be table representation of a frame buffer)
---@param xOff? number *Optional.* X position on `buffer2` to start drawing from. Defaults to `0`
---@param yOff? number *Optional.* Y position on `buffer2` to start drawing from. Defaults to `0`
---@param w? number *Optional.* Width of `buffer2` to draw from `xOff`. Defaults to `buffer2.getWidth() - xOff`
---@param h? number *Optional.* Height of `buffer2` to draw from `xOff`. Defaults to `buffer2.getHeight() - yOff`
---@see FrameBuffer.getTable To convert a FrameBuffer to a table
---@see GraphicsAPI.tableToFrameBuffer To create a FrameBuffer from a table
function FrameBuffer.drawBufferMasked(x, y, buffer2, xOff, yOff, w, h) end

--- Gets the width of the frame buffer
---@return integer width Width of the frame buffer
function FrameBuffer.getWidth() end

--- Gets the height of the frame buffer
---@return integer height Height of the frame buffer
function FrameBuffer.getHeight() end

--- Get a deep copy of this frame buffer
---@return FrameBuffer frame Deep copy of this frame
function FrameBuffer.copy() end

--- Get a table representation of the frame buffer
---@return table frame Table copy of the frame buffer
function FrameBuffer.getTable() end

--- Check if a given position is within this frame buffer.
---@param x number X position to check
---@param y number Y position to check
---@return boolean inFrame If the position was inside the frame
function FrameBuffer.inFrame(x, y) end

---@class GraphicsNativeAPI Native Graphics api. Loadable with `require('graphics_native')`
local GraphicsNativeAPI = {}

--- Make a new FrameBuffer of the provided size
---@param width integer Width of the new frame buffer
---@param height integer Height of the new frame buffer
---@return FrameBuffer frameBuffer
function GraphicsNativeAPI.FrameBuffer(width, height) end

--- Convert a table to a FrameBuffer.
--- <br/>
--- The table must contain numeric values for `width`, `height`, and a value for each pixel, starting index at `0` in row column format.
---@param tbl table
---@return FrameBuffer frameBuffer
---@see FrameBuffer.getTable To convert a frame buffer to a table
function GraphicsNativeAPI.tableToFrameBuffer(tbl) end

--- Packs Red, Green, & Blue into an ARGB8 integer for use with FrameBuffers. Sets Alpha to `0xff`.
--- <br/><br/>
--- <b>Throws:</b> If any competent was out of range `0x00` - `0xff`
---@param r integer Red component ( 0 - 255 )
---@param g integer Green component ( 0 - 255 )
---@param b integer Blue component ( 0 - 255 )
---@return integer color RGBA8 color
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.unpackRGBA To unpack and ARGB8 integer
function GraphicsNativeAPI.packRGB(r, g, b) end

--- Packs Red, Green, Blue & Alpha into an ARGB8 integer for use with FrameBuffers.
--- <br/><br/>
--- <b>Throws:</b> If any competent was out of range `0x00` - `0xff`
---@param r integer Red component ( 0 - 255 )
---@param g integer Green component ( 0 - 255 )
---@param b integer Blue component ( 0 - 255 )
---@param a integer Alpha component ( 0 - 255 )
---@return integer color RGBA8 color
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.unpackRGBA To unpack and ARGB8 integer
function GraphicsNativeAPI.packRGBA(r, g, b, a) end

--- Unpacks Red, Green, Blue, and Alpha from an ARGB8 integer.
---@param color integer ARGB8 color to unpack
---@return integer r Red component ( 0 - 255 )
---@return integer g Green component ( 0 - 255 )
---@return integer b Blue component ( 0 - 255 )
---@return integer a Alpha component ( 0 - 255 )
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.packRGBA To pack color into an ARGB8 integer
function GraphicsNativeAPI.unpackRGBA(color) end

--- Get a new text renderer for the specified font.
--- <br>
--- Current fonts:
--- - `mono` in a size of `7`, `9`, and `11` pixels
--- - `departure_mono` in a size of `8` pixels
--- <br><br>
--- <b>Throws:</b> If the requested font doesn't exist
---@param name string Font name
---@param size integer Font height in pixels
---@return FontRenderer renderer Text renderer for the font
function GraphicsNativeAPI.getTextRenderer(name, size) end

--- Load an image as a FrameBuffer.
--- <br><br>
--- <b>Throws:</b> If the image was invalid
---@param image integer[] PNG or JPEG as byte array
---@return FrameBuffer image
function GraphicsNativeAPI.loadImage(image) end

--- Load an image as a FrameBuffer.
--- <br>
--- Works with byte array string of PNG or JPEG images, such as from [`ReadHandle.readAll()`](https://tweaked.cc/module/fs.html#ty:ReadHandle:readAll).
--- <br><br>
--- <b>Throws:</b> If the image was invalid
---@param image string PNG or JPEG as byte array string
---@return FrameBuffer image
function GraphicsNativeAPI.loadImageString(image) end

--- Load an FBB as a FrameBuffer.
--- <br>
--- Works with byte array string of FBB images, such as from [`ReadHandle.readAll()`](https://tweaked.cc/module/fs.html#ty:ReadHandle:readAll).
--- <br><br>
--- <b>Throws:</b> If the image was invalid
---@param image string FBB as byte array string
---@return FrameBuffer image
function GraphicsNativeAPI.loadFBBString(image) end

---@class GraphicsAPI Graphics api. Loadable with `require('graphics')`
local GraphicsAPI = {}

--- Make a new FrameBuffer of the provided size
---@param width integer Width of the new frame buffer
---@param height integer Height of the new frame buffer
---@return FrameBuffer frameBuffer
function GraphicsAPI.FrameBuffer(width, height) end

--- Convert a table to a FrameBuffer.
--- <br/>
--- The table must contain numeric values for `width`, `height`, and a value for each pixel, starting index at `0` in row column format.
---@param tbl table
---@return FrameBuffer frameBuffer
---@see FrameBuffer.getTable To convert a frame buffer to a table
function GraphicsAPI.tableToFrameBuffer(tbl) end

--- Packs Red, Green, & Blue into an ARGB8 integer for use with FrameBuffers. Sets Alpha to `0xff`.
--- <br/><br/>
--- <b>Throws:</b> If any competent was out of range `0x00` - `0xff`
---@param r integer Red component ( 0 - 255 )
---@param g integer Green component ( 0 - 255 )
---@param b integer Blue component ( 0 - 255 )
---@return integer color RGBA8 color
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.unpackRGBA To unpack and ARGB8 integer
function GraphicsAPI.packRGB(r, g, b) end

--- Packs Red, Green, Blue & Alpha into an ARGB8 integer for use with FrameBuffers.
--- <br/><br/>
--- <b>Throws:</b> If any competent was out of range `0x00` - `0xff`
---@param r integer Red component ( 0 - 255 )
---@param g integer Green component ( 0 - 255 )
---@param b integer Blue component ( 0 - 255 )
---@param a integer Alpha component ( 0 - 255 )
---@return integer color RGBA8 color
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.unpackRGBA To unpack and ARGB8 integer
function GraphicsAPI.packRGBA(r, g, b, a) end

--- Unpacks Red, Green, Blue, and Alpha from an ARGB8 integer.
---@param color integer ARGB8 color to unpack
---@return integer r Red component ( 0 - 255 )
---@return integer g Green component ( 0 - 255 )
---@return integer b Blue component ( 0 - 255 )
---@return integer a Alpha component ( 0 - 255 )
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.packRGBA To pack color into an ARGB8 integer
function GraphicsAPI.unpackRGBA(color) end

--- Get a new text renderer for the specified font.
--- <br>
--- Current fonts:
--- - `mono` in a size of `7`, `9`, and `11` pixels
--- - `departure_mono` in a size of `8` pixels
--- <br><br>
--- <b>Throws:</b> If the requested font doesn't exist
---@param name string Font name
---@param size integer Font height in pixels
---@return FontRenderer renderer Text renderer for the font
function GraphicsAPI.getTextRenderer(name, size) end

--- Load an image as a FrameBuffer.
--- <br><br>
--- <b>Throws:</b> If the image was invalid
---@param image integer[] PNG or JPEG as byte array
---@return FrameBuffer image
function GraphicsAPI.loadImage(image) end

--- Load an image as a FrameBuffer.
--- <br>
--- Works with byte array string of PNG or JPEG images, such as from [`ReadHandle.readAll()`](https://tweaked.cc/module/fs.html#ty:ReadHandle:readAll).
--- <br><br>
--- <b>Throws:</b> If the image was invalid
---@param image string PNG or JPEG as byte array string
---@return FrameBuffer image
function GraphicsAPI.loadImageString(image) end

--- Load an image file as a FrameBuffer.
--- <br/><br/>
--- <b>Throws:</b> If the image was invalid
---@param filename string Path to image file
---@return FrameBuffer image
function GraphicsAPI.loadImageFile(filename) end

--- Load an FBB file as a FrameBuffer.
--- <br/>
--- Works with byte array string of FBB image, such as from [`ReadHandle.readAll()`](https://tweaked.cc/module/fs.html#ty:ReadHandle:readAll).
--- <br/><br/>
--- <b>Throws:</b> If the image was invalid
---@param fbb string FBB as byte array string
---@return FrameBuffer image
function GraphicsAPI.loadFBBString(fbb) end

--- Load an FBB file as a FrameBuffer.
--- <br/><br/>
--- <b>Throws:</b> If the image was invalid
---@param filename string Path to FBB file
---@return FrameBuffer image
function GraphicsAPI.loadFBBFile(filename) end

---@class FontRenderer
local FontRenderer = {}

--- Rasterize the provided text to a frame buffer
---@param text string Text to rasterize
---@param color? integer *Optional.* Color of the rasterized text. Defaults to `0xffffffff`
---@return FrameBuffer buffer Frame buffer of rasterized text
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.packRGBA To pack color into an ARGB8 integer
---@see FontRenderer.getTextSize To pre-calculate the size of the rasterized text
function FontRenderer.rasterize(text, color) end

--- Get the size of text if it were rasterized
---@param text string Text to calculate size of
---@return integer width Width of the rasterized text
---@return integer height Height of the rasterized text
function FontRenderer.getTextSize(text) end

---@class GraphicsMonitorPeripheral
---Extends [`redirect`](https://tweaked.cc/module/term.html#ty:Redirect)
local GraphicsMonitorPeripheral = {}

--- Get a FrameBuffer that is the size of the monitor.
--- <br><br>
--- Mode independent.
---@return FrameBuffer frameBuffer
function GraphicsMonitorPeripheral.getNewFrameBuffer() end

--- Set the current frame buffer for the monitor.
--- <br>
--- Function is limited to 5 times per-second per-peripheral.
--- <br><br>
--- Function will return `false` in terminal mode.
--- <br><br>
--- <b>Throws:</b> If the provided buffer could not be converted to a FrameBuffer
---@param frame FrameBuffer|table Frame buffer to set. Must be the same size as the monitor. Accepts a table representing a frame buffer
---@return boolean set If the buffer was set.
function GraphicsMonitorPeripheral.setFrameBuffer(frame) end

--- Get the width of the monitor in pixels.
--- <br><br>
--- Mode independent.
---@return integer width
function GraphicsMonitorPeripheral.getWidth() end

--- Get the height of the monitor in pixels.
--- <br><br>
--- Mode independent.
---@return integer height
function GraphicsMonitorPeripheral.getHeight() end

--- Put the monitor into terminal mode.
--- <br>
--- When in terminal mode, the frame buffer can't be set directly, but terminal functions are available.
function GraphicsMonitorPeripheral.makeTerm() end

--- Put the monitor into graphics mode.
--- <br>
--- When in graphics mode calls to the terminal functions may result in undefined result.
function GraphicsMonitorPeripheral.makeGraphics() end

--- Set the font size of the monitor in terminal mode.
--- <br>
--- Currently only sizes of `7`, `9`, and `11` are allowed.
--- <br><br>
--- Mode independent.
--- <br><br>
--- <b>Throws:</b> If the provided size is not allowed
---@param size integer The pixel height of the text when monitor is in terminal mode.
function GraphicsMonitorPeripheral.setTermTextSize(size) end

---@class ScreenAPI
local ScreenAPI = {}

--- Set the current frame buffer for the screen
--- <br><br>
--- <b>Throws:</b> If the frame buffer was not the size of the screen
---@param frame FrameBuffer Frame buffer to set
---@see ScreenAPI.getFrameBuffer to get a frame buffer the size of the screen
---@see ScreenAPI.getWidth
---@see ScreenAPI.getHeight
function ScreenAPI.setFrame(frame) end

--- Put the screen in graphics mode (ie. setting the frame buffer directly)
--- <br/>
---@see ScreenAPI.setTerminalMode to put the screen in terminal mode
function ScreenAPI.setGraphicsMode() end

--- Put the screen in graphics mode (ie. setting the frame buffer directly)
--- <br/>
---@see ScreenAPI.setGraphicsMode to put the screen in terminal mode
function ScreenAPI.setTerminalMode() end

--- Gets the width of the screen in pixels
---@return integer width
function ScreenAPI.getWidth() end

--- Gets the height of the screen in pixels
---@return integer height
function ScreenAPI.getHeight() end

--- Get a new frame buffer the size of this screen
---@return FrameBuffer frameBuffer
function ScreenAPI.getFrameBuffer() end