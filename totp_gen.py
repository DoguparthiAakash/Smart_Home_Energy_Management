import hmac, base64, struct, hashlib, time
import sys

def get_totp_token(secret):
    try:
        # Pad secret if needed
        padding = len(secret) % 8
        if padding != 0:
            secret += '=' * (8 - padding)
        key = base64.b32decode(secret, casefold=True)
    except Exception as e:
        print(f"Error decoding secret: {e}")
        return "000000"

    msg = struct.pack(">Q", int(time.time()) // 30)
    h = hmac.new(key, msg, hashlib.sha1).digest()
    o = h[19] & 15
    h = (struct.unpack(">I", h[o:o+4])[0] & 0x7fffffff) % 1000000
    return str(h).zfill(6)

if __name__ == "__main__":
    secret = sys.argv[1] if len(sys.argv) > 1 else "RGFEUOXBMFGWCBJJ5WWHLHWEZK7AYJDV"
    print(get_totp_token(secret))
