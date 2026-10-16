import os
import glob
import struct
import zlib
from PIL import Image

def un_cgbi(filename):
    with open(filename, 'rb') as f:
        data = f.read()

    if data[:8] != b'\x89PNG\r\n\x1a\n':
        return None

    pos = 8
    is_cgbi = False
    chunks = []
    width = height = None
    idat_data = b''

    while pos < len(data):
        length, chunk_type = struct.unpack('>I4s', data[pos:pos+8])
        chunk_data = data[pos+8:pos+8+length]
        pos += 12 + length

        if chunk_type == b'CgBI':
            is_cgbi = True
            continue
        elif chunk_type == b'IHDR':
            width, height = struct.unpack('>II', chunk_data[:8])
            chunks.append((chunk_type, chunk_data))
        elif chunk_type == b'IDAT':
            idat_data += chunk_data
        else:
            chunks.append((chunk_type, chunk_data))

    if not is_cgbi:
        return None

    try:
        decompressed = zlib.decompress(idat_data, -15)
    except Exception as e:
        print(f"Error decompressing {filename}: {e}")
        return None

    line_len = 1 + width * 4
    out_bytes = bytearray()
    for y in range(height):
        line = decompressed[y*line_len : (y+1)*line_len]
        filter_byte = line[0:1]
        pixels = bytearray(line[1:])
        for x in range(0, len(pixels), 4):
            b, g, r, a = pixels[x], pixels[x+1], pixels[x+2], pixels[x+3]
            pixels[x] = r
            pixels[x+2] = b
        out_bytes.extend(filter_byte + pixels)

    new_idat = zlib.compress(bytes(out_bytes))
    out_png = bytearray(b'\x89PNG\r\n\x1a\n')
    for ctype, cdata in chunks:
        if ctype == b'IEND':
            crc = zlib.crc32(b'IDAT' + new_idat) & 0xffffffff
            out_png.extend(struct.pack('>I4s', len(new_idat), b'IDAT') + new_idat + struct.pack('>I', crc))
        crc = zlib.crc32(ctype + cdata) & 0xffffffff
        out_png.extend(struct.pack('>I4s', len(cdata), ctype) + cdata + struct.pack('>I', crc))

    return bytes(out_png)

def process_all(directory):
    files = glob.glob(os.path.join(directory, '*.png'))
    converted_count = 0
    valid_count = 0

    for fpath in files:
        fname = os.path.basename(fpath)
        converted = un_cgbi(fpath)
        if converted is not None:
            with open(fpath, 'wb') as f:
                f.write(converted)
            converted_count += 1
            print(f"Converted CgBI -> standard PNG: {fname}")

        # Verify with PIL
        try:
            with Image.open(fpath) as img:
                img.load()
                valid_count += 1
        except Exception as e:
            print(f"FAILED to verify {fname}: {e}")

    print(f"\nDone! Converted: {converted_count}, Total valid standard PNGs: {valid_count}/{len(files)}")

if __name__ == '__main__':
    target_dir = r'D:\share\Projects\OldOS_Android\app\src\main\res\drawable-nodpi'
    process_all(target_dir)
