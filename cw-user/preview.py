"""Local design preview with explicit JavaScript module MIME type on Windows."""
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

class DesignHandler(SimpleHTTPRequestHandler):
    extensions_map = {**SimpleHTTPRequestHandler.extensions_map, ".mjs": "text/javascript"}

if __name__ == "__main__":
    handler = partial(DesignHandler, directory=str(Path(__file__).parent / "design"))
    with ThreadingHTTPServer(("127.0.0.1", 4180), handler) as server:
        print("Design preview: http://127.0.0.1:4180/", flush=True)
        server.serve_forever()
