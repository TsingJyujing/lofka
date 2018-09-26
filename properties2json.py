#!/usr/bin/python3

import json
from collections import OrderedDict

import sys
from jproperties import Properties


def update_dict(d: dict, ks: list, v):
    key_info = ks[0]
    if len(ks) == 1:
        d[key_info] = v
    else:
        if key_info not in d:
            d[key_info] = {}
        update_dict(d[key_info], ks[1:], v)


def properties2json_v2(p: Properties, spilt_char: str = "."):
    raw_dict = {}
    for k, v in p.items():
        update_dict(raw_dict, k.split(spilt_char), v)
    return raw_dict


def properties2json(p: Properties):
    return OrderedDict(sorted(p.items(), key=lambda x: x[0]))


def main():
    file_name = sys.argv[1]
    with open(file_name, "r") as fp:
        p = Properties({})
        p.load(fp)
        print(
            json.dumps(
                properties2json(p),
                indent=2
            )
        )


if __name__ == '__main__':
    main()
