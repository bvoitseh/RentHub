"""Fetches the legal status of the exported settlements from Wikidata (D-48): «instance of» (P31) of every OSM node's
`wikidata` item, plus the Ukrainian labels of those classes. Wikidata content is CC0.

Run by hand after export_settlements.py, never from the application:

    python infra/osm/wikidata_status.py        # writes infra/osm/cache/wikidata_*.json (git-ignored)

Sequential batches with pauses and an identifying User-Agent (Wikidata Query Service policy); finished batches are
cached, so an interrupted run resumes. Python standard library only.
"""
import glob
import json
import pathlib
import sys
import time
import urllib.parse
import urllib.request

ENDPOINT = "https://query.wikidata.org/sparql"
USER_AGENT = "RentHub-reference-export/1.0 (educational project; one-off settlement status lookup)"
CACHE = pathlib.Path(__file__).resolve().parent / "cache"
BATCH = 400
PAUSE_SECONDS = 5


def sparql(query):
    data = urllib.parse.urlencode({"query": query}).encode()
    request = urllib.request.Request(ENDPOINT, data=data, headers={
        "User-Agent": USER_AGENT, "Accept": "application/sparql-results+json",
        "Content-Type": "application/x-www-form-urlencoded"})
    for attempt in range(5):
        try:
            with urllib.request.urlopen(request, timeout=300) as response:
                return json.loads(response.read())["results"]["bindings"]
        except Exception as error:  # 429 / 5xx: back off
            print(f"  request failed ({error}); retrying in {30 * (attempt + 1)} s", flush=True)
            time.sleep(30 * (attempt + 1))
    raise SystemExit("Wikidata did not answer; try again later")


def qid(uri):
    return uri.rsplit("/", 1)[-1]


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    items = sorted({e["tags"]["wikidata"] for f in glob.glob(str(CACHE / "nodes_*.json"))
                    for e in json.loads(pathlib.Path(f).read_text(encoding="utf-8"))["elements"]
                    if e.get("tags", {}).get("wikidata", "").startswith("Q")})
    print(f"{len(items)} Wikidata items", flush=True)
    for number, start in enumerate(range(0, len(items), BATCH)):
        path = CACHE / f"wikidata_p31_{number:03d}.json"
        if path.exists():
            continue
        values = " ".join("wd:" + q for q in items[start:start + BATCH])
        rows = sparql(f"SELECT ?item ?type WHERE {{ VALUES ?item {{ {values} }} ?item wdt:P31 ?type . }}")
        path.write_text(json.dumps([[qid(r["item"]["value"]), qid(r["type"]["value"])] for r in rows]), encoding="utf-8")
        print(f"  batch {number + 1}: {len(rows)} statements", flush=True)
        time.sleep(PAUSE_SECONDS)
    types = sorted({t for f in glob.glob(str(CACHE / "wikidata_p31_*.json"))
                    for _, t in json.loads(pathlib.Path(f).read_text(encoding="utf-8"))})
    labels_path = CACHE / "wikidata_type_labels.json"
    if not labels_path.exists():
        values = " ".join("wd:" + t for t in types)
        rows = sparql(f"SELECT ?type ?label WHERE {{ VALUES ?type {{ {values} }} "
                      f"OPTIONAL {{ ?type rdfs:label ?label FILTER(LANG(?label) = 'uk') }} }}")
        labels_path.write_text(json.dumps({qid(r["type"]["value"]): r.get("label", {}).get("value") for r in rows},
                                          ensure_ascii=False), encoding="utf-8")
    print(f"{len(types)} distinct classes", flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
