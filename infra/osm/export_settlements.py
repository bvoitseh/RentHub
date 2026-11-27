"""Exports Ukrainian regions and settlements from OpenStreetMap through the public Overpass API (D-48).

Data © OpenStreetMap contributors, ODbL 1.0 (https://www.openstreetmap.org/copyright).

Run by hand only, rarely (reference data refresh), never from the application:

    python infra/osm/export_settlements.py            # writes infra/osm/cache/*.json (git-ignored)

Usage policy of the public Overpass instance: few sequential queries, an identifying User-Agent, pauses between
requests. Each region is one query; an existing cache file is reused, so an interrupted run can be resumed.
Python standard library only.
"""
import json
import pathlib
import sys
import time
import urllib.parse
import urllib.request

ENDPOINT = "https://overpass-api.de/api/interpreter"
USER_AGENT = "RentHub-reference-export/1.0 (educational project; one-off settlement list export)"
PAUSE_SECONDS = 15
CACHE = pathlib.Path(__file__).resolve().parent / "cache"
PLACES = "city|town|village|hamlet"

REGIONS_QUERY = """
[out:json][timeout:180];
area["ISO3166-1"="UA"]["admin_level"="2"]->.ua;
rel(area.ua)["boundary"="administrative"]["admin_level"="4"];
out tags;
"""

# Per region three light queries: the settlement nodes, and the district (admin_level=6) and territorial community
# (admin_level=7, громада) boundaries with geometry, so they are matched locally (point in polygon) instead of by
# one heavy query per boundary.
NODES_QUERY = """
[out:json][timeout:300];
area({area_id})->.r;
node(area.r)["place"~"^({places})$"];
out;
"""

COMMUNITIES_QUERY = """
[out:json][timeout:600];
area({area_id})->.r;
rel(area.r)["boundary"="administrative"]["admin_level"="7"];
out geom;
"""

DISTRICTS_QUERY = """
[out:json][timeout:300];
area({area_id})->.r;
rel(area.r)["boundary"="administrative"]["admin_level"="6"];
out geom;
"""


def overpass(query):
    data = urllib.parse.urlencode({"data": query}).encode()
    request = urllib.request.Request(ENDPOINT, data=data, headers={"User-Agent": USER_AGENT})
    for attempt in range(5):
        try:
            with urllib.request.urlopen(request, timeout=900) as response:
                return json.loads(response.read())
        except Exception as error:  # 429 / 504 from a busy server: wait and retry a few times
            print(f"  request failed ({error}); retrying in {60 * (attempt + 1)} s", flush=True)
            time.sleep(60 * (attempt + 1))
    raise SystemExit("Overpass did not answer; try again later")


def cached(name, query):
    path = CACHE / name
    if path.exists():
        return json.loads(path.read_text(encoding="utf-8"))
    result = overpass(query)
    path.write_text(json.dumps(result, ensure_ascii=False), encoding="utf-8")
    time.sleep(PAUSE_SECONDS)
    return result


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    CACHE.mkdir(exist_ok=True)
    regions = cached("regions.json", REGIONS_QUERY)["elements"]
    print(f"{len(regions)} regions", flush=True)
    regions = [r for r in regions if r["tags"].get("ISO3166-2", "").startswith("UA-")]
    print(f"{len(regions)} with an ISO 3166-2 code (oblasts, Crimea, Kyiv, Sevastopol)", flush=True)
    for region in regions:
        area_id = 3600000000 + region["id"]
        name = region["tags"].get("name:uk") or region["tags"].get("name")
        nodes = cached(f"nodes_{region['id']}.json", NODES_QUERY.format(area_id=area_id, places=PLACES))
        districts = cached(f"districts_{region['id']}.json", DISTRICTS_QUERY.format(area_id=area_id))
        communities = cached(f"communities_{region['id']}.json", COMMUNITIES_QUERY.format(area_id=area_id))
        print(f"  {name}: {len(nodes['elements'])} places, {len(districts['elements'])} districts, "
              f"{len(communities['elements'])} communities", flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
