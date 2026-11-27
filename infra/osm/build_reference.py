"""Builds the region / settlement reference data from the exports in infra/osm/cache (D-48).

Settlements, districts and communities: © OpenStreetMap contributors, ODbL 1.0. Legal status: Wikidata, CC0.

    python infra/osm/build_reference.py --report [--find NAME ...]   # counts and sample rows, nothing written
    python infra/osm/build_reference.py --sql FILE                   # the initial reference-data migration (V12)
    python infra/osm/build_reference.py --refresh-sql FILE           # a later refresh migration (README)

Rules (author's decisions, DECISIONS.md D-48):
- the Ukrainian name is name:uk, or the plain name written in the Ukrainian alphabet; entries without one are dropped;
- one row per OSM node id; every row must have coordinates (the run fails otherwise);
- district = admin_level=6 and community = admin_level=7 boundary containing the node, counting only boundaries of
  the 2020 Ukrainian division (tagged `katotth`; the newest relation wins when several share a code);
- legal status (місто / селище / село) from Wikidata P31; unclear or conflicting -> no status, never the OSM size;
- the community is part of the label only when name + district are ambiguous within the region; if the label is
  still not unique, the postal code follows the name;
- postal code: the node's single five-digit postal_code, if any.
Python standard library only.
"""
import argparse
import glob
import json
import pathlib
import re
import sys
from collections import Counter

CACHE = pathlib.Path(__file__).resolve().parent / "cache"
UKRAINIAN = re.compile(r"^[А-ЩЬЮЯЄІЇҐа-щьюяєіїґ’'ʼ\- .()0-9]+$")
BAND = 100

CITY = {"Q12131624", "Q5123999", "Q12131640", "Q1549591", "Q12134916", "Q515", "Q2264924", "Q428986", "Q111141281"}
TOWN = {"Q121366249", "Q4100864", "Q2989457", "Q2514025", "Q15078955"}
VILLAGE = {"Q21672098", "Q532", "Q41501164"}
UKRAINE_SPECIFIC = {"Q12131624", "Q5123999", "Q12131640", "Q121366249", "Q4100864", "Q21672098", "Q41501164"}
STATUS_OF = {**{q: "CITY" for q in CITY}, **{q: "TOWN" for q in TOWN}, **{q: "VILLAGE" for q in VILLAGE}}
ABBREVIATION = {"CITY": "м.", "TOWN": "сел.", "VILLAGE": "с.", None: ""}


def ukrainian_name(tags):
    if tags.get("name:uk"):
        return tags["name:uk"].strip()
    name = (tags.get("name") or "").strip()
    if name and UKRAINIAN.match(name) and re.search(r"[А-ЩЬЮЯЄІЇҐа-щьюяєіїґ]", name):
        return name
    return None


def short_district(name):
    return re.sub(r"\s+район$", " р-н", name) if name else None


def short_community(name):
    return re.sub(r"\s+(міська|селищна|сільська)\s+(територіальна\s+)?громада$", " громада", name) if name else None


def search_key(text):
    return re.sub(r"[’'ʼ`]", "", text).lower()


class Boundary:
    """A boundary as a set of segments (outer and inner ways); even-odd ray test without assembling rings."""

    def __init__(self, name, ways):
        self.name, self.bands = name, {}
        lats = [p["lat"] for way in ways for p in way]
        lons = [p["lon"] for way in ways for p in way]
        self.box = (min(lats), max(lats), min(lons), max(lons)) if lats else (0, -1, 0, -1)
        for way in ways:
            for a, b in zip(way, way[1:]):
                for band in range(int(min(a["lat"], b["lat"]) * BAND), int(max(a["lat"], b["lat"]) * BAND) + 1):
                    self.bands.setdefault(band, []).append((a["lat"], a["lon"], b["lat"], b["lon"]))

    def contains(self, lat, lon):
        lat_min, lat_max, lon_min, lon_max = self.box
        if not (lat_min <= lat <= lat_max and lon_min <= lon <= lon_max):
            return False
        inside = False
        for y1, x1, y2, x2 in self.bands.get(int(lat * BAND), ()):
            if (y1 > lat) != (y2 > lat) and lon < x1 + (lat - y1) * (x2 - x1) / (y2 - y1):
                inside = not inside
        return inside


def boundaries(kind, region_id, shorten):
    """2020 Ukrainian boundaries only: tagged katotth; among relations sharing a code the newest (highest id) wins."""
    newest = {}
    for rel in json.loads((CACHE / f"{kind}_{region_id}.json").read_text(encoding="utf-8"))["elements"]:
        code = rel.get("tags", {}).get("katotth")
        if code and (code not in newest or rel["id"] > newest[code]["id"]):
            newest[code] = rel
    return [Boundary(shorten(ukrainian_name(r["tags"]) or r["tags"].get("name")),
                     [m["geometry"] for m in r.get("members", []) if m.get("type") == "way" and m.get("geometry")])
            for r in newest.values()]


def wikidata_status():
    classes = {}
    for f in glob.glob(str(CACHE / "wikidata_p31_*.json")):
        for item, cls in json.loads(pathlib.Path(f).read_text(encoding="utf-8")):
            classes.setdefault(item, set()).add(cls)
    result = {}
    for item, found in classes.items():
        statuses = {STATUS_OF[c] for c in found if c in STATUS_OF}
        if len(statuses) > 1:
            statuses = {STATUS_OF[c] for c in found if c in UKRAINE_SPECIFIC}
        result[item] = statuses.pop() if len(statuses) == 1 else None
    return result


def postal(tags):
    code = (tags.get("postal_code") or tags.get("addr:postcode") or "").strip()
    return code if re.fullmatch(r"\d{5}", code) else None


def load():
    regions_raw = [r for r in json.loads((CACHE / "regions.json").read_text(encoding="utf-8"))["elements"]
                   if r["tags"].get("ISO3166-2", "").startswith("UA-")]
    status_of = wikidata_status()
    regions, settlements, seen, dropped = [], [], set(), 0
    for region in sorted(regions_raw, key=lambda r: r["tags"]["ISO3166-2"]):
        rid = region["id"]
        regions.append({"osm_id": rid, "iso": region["tags"]["ISO3166-2"], "name": ukrainian_name(region["tags"])})
        districts = boundaries("districts", rid, short_district)
        communities = boundaries("communities", rid, short_community)
        for e in json.loads((CACHE / f"nodes_{rid}.json").read_text(encoding="utf-8"))["elements"]:
            if e["type"] != "node" or e["id"] in seen:
                continue
            seen.add(e["id"])
            tags = e.get("tags", {})
            name = ukrainian_name(tags)
            if not name:
                dropped += 1
                continue
            if e.get("lat") is None or e.get("lon") is None:
                raise SystemExit(f"node {e['id']} ({name}) has no coordinates")
            lat, lon = e["lat"], e["lon"]
            settlements.append({
                "osm_id": e["id"], "iso": region["tags"]["ISO3166-2"], "name": name,
                "status": status_of.get(tags.get("wikidata")),
                "district": next((b.name for b in districts if b.contains(lat, lon)), None),
                "community": next((b.name for b in communities if b.contains(lat, lon)), None),
                "lat": round(lat, 6), "lon": round(lon, 6), "postal_code": postal(tags)})
    ambiguous = Counter((s["iso"], s["name"], s["district"]) for s in settlements)
    for s in settlements:
        parts = [f"{ABBREVIATION[s['status']]} {s['name']}".strip()]
        if ambiguous[(s["iso"], s["name"], s["district"])] > 1 and s["community"]:
            parts.append(s["community"])
        if s["district"]:
            parts.append(s["district"])
        s["label"] = ", ".join(parts)
    # Last tiebreaker for namesakes in the same community: the postal code after the name (author's choice).
    same = Counter((s["iso"], s["label"]) for s in settlements)
    for s in settlements:
        if same[(s["iso"], s["label"])] > 1 and s["postal_code"]:
            head, _, tail = s["label"].partition(", ")
            s["label"] = f"{head} ({s['postal_code']})" + (f", {tail}" if tail else "")
    return regions, settlements, dropped


def report(regions, settlements, dropped, find):
    print(f"regions: {len(regions)}; settlements: {len(settlements)}; dropped without a Ukrainian name: {dropped}")
    statuses = Counter(s["status"] for s in settlements)
    print(f"status: місто {statuses['CITY']}, селище {statuses['TOWN']}, село {statuses['VILLAGE']}, "
          f"none {statuses[None]}")
    print(f"with district {sum(1 for s in settlements if s['district'])}, with community "
          f"{sum(1 for s in settlements if s['community'])}, with postal code "
          f"{sum(1 for s in settlements if s['postal_code'])}")
    names = {r["iso"]: r["name"] for r in regions}
    for r in regions:
        rows = [s for s in settlements if s["iso"] == r["iso"]]
        c = Counter(s["status"] for s in rows)
        print(f"  {r['iso']:<6} {r['name']:<28} {len(rows):>5}  міст {c['CITY']:>3}, селищ {c['TOWN']:>3}, сіл {c['VILLAGE']:>5}, "
              f"без статусу {c[None]:>4}; без району {sum(1 for s in rows if not s['district'])}, "
              f"без громади {sum(1 for s in rows if not s['community'])}")
    labels = Counter((s["iso"], s["label"]) for s in settlements)
    remaining = {k: v for k, v in labels.items() if v > 1}
    print(f"labels still identical within a region: {len(remaining)} labels, {sum(remaining.values())} settlements")
    for (iso, label), n in sorted(remaining.items(), key=lambda x: -x[1])[:8]:
        print(f"    {n} x {label} ({names[iso]})")
    for wanted in find:
        for s in settlements:
            if s["name"].lower() == wanted.lower():
                print(f"  {s['label']:<58} {names[s['iso']]:<26} {s['lat']:>10} {s['lon']:>10} {s['postal_code'] or '-':>6}")


def sql_text(value):
    return "NULL" if value is None else "'" + str(value).replace("'", "''") + "'"


def write_sql(path, regions, settlements):
    out = ["-- Reference data: regions and settlements of Ukraine (FR-1, FR-4, FR-16, FR-23; DECISIONS.md D-48).",
           "-- Generated by infra/osm/build_reference.py — do not edit by hand; refresh as described in the README.",
           "-- Settlements, districts, communities: © OpenStreetMap contributors, ODbL 1.0. Legal status: Wikidata, CC0.",
           ""]
    out.append("INSERT INTO regions (osm_id, iso_code, name, active) VALUES")
    out.append(",\n".join(f"    ({r['osm_id']}, {sql_text(r['iso'])}, {sql_text(r['name'])}, TRUE)" for r in regions) + ";")
    out.append("")
    columns = "osm_id, region_iso, name, search_name, status, district, community, label, lat, lon, postal_code"
    for start in range(0, len(settlements), 500):
        rows = ",\n".join(
            f"    ({s['osm_id']}, {sql_text(s['iso'])}, {sql_text(s['name'])}, {sql_text(search_key(s['name']))}, "
            f"{sql_text(s['status'])}, {sql_text(s['district'])}, {sql_text(s['community'])}, {sql_text(s['label'])}, "
            f"{s['lat']}, {s['lon']}, {sql_text(s['postal_code'])})"
            for s in settlements[start:start + 500])
        out.append("INSERT INTO settlements (osm_id, region_id, name, search_name, status, district, community, label, "
                   "lat, lon, postal_code, active)")
        out.append(f"SELECT v.osm_id, r.id, v.name, v.search_name, v.status, v.district, v.community, v.label, v.lat, "
                   f"v.lon, v.postal_code, TRUE")
        out.append(f"FROM (VALUES\n{rows}\n) AS v({columns})")
        out.append("JOIN regions r ON r.iso_code = v.region_iso;")
        out.append("")
    pathlib.Path(path).write_text("\n".join(out), encoding="utf-8", newline="\n")


def write_refresh_sql(path, regions, settlements):
    """A later refresh as one migration (D-48): rows are matched by OSM id; new ones are inserted, existing ones
    updated (renames included), rows missing from the export become inactive. Nothing is deleted, so users and
    listings keep their references. Standard MERGE (H2 2.x, PostgreSQL 15+)."""
    columns = "osm_id, region_iso, name, search_name, status, district, community, label, lat, lon, postal_code"
    out = ["-- Refresh of the regions / settlements reference data (DECISIONS.md D-48).",
           "-- Generated by infra/osm/build_reference.py --refresh-sql — do not edit by hand.",
           "-- Settlements, districts, communities: © OpenStreetMap contributors, ODbL 1.0. Legal status: Wikidata, CC0.",
           "",
           "MERGE INTO regions t USING (VALUES",
           ",\n".join(f"    ({r['osm_id']}, {sql_text(r['iso'])}, {sql_text(r['name'])})" for r in regions),
           ") AS v(osm_id, iso_code, name) ON t.iso_code = v.iso_code",
           "WHEN MATCHED THEN UPDATE SET osm_id = v.osm_id, name = v.name, active = TRUE",
           "WHEN NOT MATCHED THEN INSERT (osm_id, iso_code, name, active) VALUES (v.osm_id, v.iso_code, v.name, TRUE);",
           "",
           "CREATE TABLE settlements_refresh (osm_id BIGINT PRIMARY KEY, region_iso VARCHAR(6) NOT NULL, "
           "name VARCHAR(120) NOT NULL, search_name VARCHAR(120) NOT NULL, status VARCHAR(10), district VARCHAR(120), "
           "community VARCHAR(120), label VARCHAR(300) NOT NULL, lat DOUBLE PRECISION NOT NULL, "
           "lon DOUBLE PRECISION NOT NULL, postal_code VARCHAR(5));",
           ""]
    for start in range(0, len(settlements), 500):
        out.append(f"INSERT INTO settlements_refresh ({columns}) VALUES")
        out.append(",\n".join(
            f"    ({s['osm_id']}, {sql_text(s['iso'])}, {sql_text(s['name'])}, {sql_text(search_key(s['name']))}, "
            f"{sql_text(s['status'])}, {sql_text(s['district'])}, {sql_text(s['community'])}, {sql_text(s['label'])}, "
            f"{s['lat']}, {s['lon']}, {sql_text(s['postal_code'])})" for s in settlements[start:start + 500]) + ";")
    out += ["",
            "MERGE INTO settlements t USING (",
            "    SELECT f.*, r.id AS region_id FROM settlements_refresh f JOIN regions r ON r.iso_code = f.region_iso",
            ") AS v ON t.osm_id = v.osm_id",
            "WHEN MATCHED THEN UPDATE SET region_id = v.region_id, name = v.name, search_name = v.search_name, "
            "status = v.status, district = v.district, community = v.community, label = v.label, lat = v.lat, "
            "lon = v.lon, postal_code = v.postal_code, active = TRUE",
            "WHEN NOT MATCHED THEN INSERT (osm_id, region_id, name, search_name, status, district, community, label, "
            "lat, lon, postal_code, active) VALUES (v.osm_id, v.region_id, v.name, v.search_name, v.status, "
            "v.district, v.community, v.label, v.lat, v.lon, v.postal_code, TRUE);",
            "",
            "-- Missing from the export: hidden from search, kept for existing references.",
            "UPDATE settlements SET active = FALSE",
            "WHERE NOT EXISTS (SELECT 1 FROM settlements_refresh f WHERE f.osm_id = settlements.osm_id);",
            "",
            "DROP TABLE settlements_refresh;",
            ""]
    pathlib.Path(path).write_text("\n".join(out), encoding="utf-8", newline="\n")


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser()
    parser.add_argument("--report", action="store_true")
    parser.add_argument("--find", nargs="*", default=[])
    parser.add_argument("--sql")
    parser.add_argument("--refresh-sql")
    args = parser.parse_args()
    regions, settlements, dropped = load()
    if args.report or args.find:
        report(regions, settlements, dropped, args.find)
    if args.sql:
        write_sql(args.sql, regions, settlements)
        print(f"written {args.sql}")
    if args.refresh_sql:
        write_refresh_sql(args.refresh_sql, regions, settlements)
        print(f"written {args.refresh_sql}")


if __name__ == "__main__":
    main()
