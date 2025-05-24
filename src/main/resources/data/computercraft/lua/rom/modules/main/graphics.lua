local native = require("graphics_native") ---@type GraphicsNativeAPI

---@diagnostic disable-next-line: missing-fields
local GraphicsAPI = {} ---@type GraphicsAPI
--- Make a new FrameBuffer of the provided size
---@param width integer Width of the new frame buffer
---@param height integer Height of the new frame buffer
---@return FrameBuffer frameBuffer
function GraphicsAPI.FrameBuffer(width, height)
    return native.FrameBuffer(width, height)
end

--- Convert a table to a FrameBuffer.
--- <br/>
--- The table must contain numeric values for `width`, `height`, and a value for each pixel, starting index at `0` in row column format.
---@param tbl table
---@return FrameBuffer frameBuffer
---@see FrameBuffer.getTable To convert a frame buffer to a table
function GraphicsAPI.tableToFrameBuffer(tbl)
    return native.tableToFrameBuffer(tbl)
end

--- Packs Red, Green, & Blue into an ARGB8 integer for use with FrameBuffers. Sets Alpha to `0xff`.
--- <br/><br/>
--- <b>Throws:</b> If any competent was out of range `0x00` - `0xff`
---@param r integer Red component ( 0 - 255 )
---@param g integer Green component ( 0 - 255 )
---@param b integer Blue component ( 0 - 255 )
---@return integer color RGBA8 color
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.unpackRGBA To unpack and ARGB8 integer
function GraphicsAPI.packRGB(r, g, b)
    return native.packRGB(r, g, b)
end

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
function GraphicsAPI.packRGBA(r, g, b, a)
    return native.packRGBA(r, g, b, a)
end

--- Unpacks Red, Green, Blue, and Alpha from an ARGB8 integer.
---@param color integer ARGB8 color to unpack
---@return integer r Red component ( 0 - 255 )
---@return integer g Green component ( 0 - 255 )
---@return integer b Blue component ( 0 - 255 )
---@return integer a Alpha component ( 0 - 255 )
---@see GraphicsAPI.packRGB To pack color into an ARGB8 integer
---@see GraphicsAPI.packRGBA To pack color into an ARGB8 integer
function GraphicsAPI.unpackRGBA(color)
    return native.unpackRGBA(color)
end

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
function GraphicsAPI.getTextRenderer(name, size)
    return native.getTextRenderer(name, size)
end

--- Load an image as a FrameBuffer.
--- <br><br>
--- <b>Throws:</b> If the image was invalid
---@param image integer[] PNG or JPEG as byte array
---@return FrameBuffer image
function GraphicsAPI.loadImage(image)
    return native.loadImage(image)
end

--- Load an image as a FrameBuffer.
--- <br>
--- Works with byte array string of PNG or JPEG images, such as from [`ReadHandle.readAll()`](https://tweaked.cc/module/fs.html#ty:ReadHandle:readAll).
--- <br><br>
--- <b>Throws:</b> If the image was invalid
---@param image string PNG or JPEG as byte array string
---@return FrameBuffer image
function GraphicsAPI.loadImageString(image)
    return native.loadImageString(image)
end

--- Load an image file as a FrameBuffer.
--- <br/><br/>
--- <b>Throws:</b> If the image was invalid
---@param filename string Path to image file
---@return FrameBuffer image
function GraphicsAPI.loadImageFile(filename)
    local f = fs.open(filename, 'rb')
    local image = f.readAll()
    f.close()
    return native.loadImageString(image)
end

--- Load an FBB file as a FrameBuffer.
--- <br/>
--- Works with byte array string of FBB image, such as from [`ReadHandle.readAll()`](https://tweaked.cc/module/fs.html#ty:ReadHandle:readAll).
--- <br/><br/>
--- <b>Throws:</b> If the image was invalid
---@param fbb string FBB as byte array string
---@return FrameBuffer image
function GraphicsAPI.loadFBBString(fbb)
    return native.loadFBBString(fbb)
end

--- Load an FBB file as a FrameBuffer.
--- <br/><br/>
--- <b>Throws:</b> If the image was invalid
---@param filename string Path to FBB file
---@return FrameBuffer image
function GraphicsAPI.loadFBBFile(filename)
    local f = fs.open(filename, 'rb')
    local image = f.readAll()
    f.close()
    return native.loadFBBString(image)
end

return GraphicsAPI