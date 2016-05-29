"""Converts the "BeastListenerText" worksheet of the
RawDecodingTexts.xls file from Virtual Radar Server, which specifies
test cases for Beast decoding in "AVR format", into YAML."""

import csv
import re
import sys

EXTRACT_FIELDS = [
    'Extracted',
    'HadParity',
    'BadChecksum',
    'IsMlat'
]

ALL_EXTRACT_FIELDS = []
for i in range(1, 3):
    for f in EXTRACT_FIELDS:
        ALL_EXTRACT_FIELDS.append('%s%s' % (f, i))

FIELDS = [
    'Blob1',
    'Blob2',
    'Blob3'
] + ALL_EXTRACT_FIELDS + [
    'Comments'
]


def regex_rule(search_re, replacement):
    s_re = re.compile(search_re)

    def xlate(f):
        return s_re.sub(replacement, f)
    return xlate


FIELD_XLATIONS = [
    regex_rule('Blob', 'Packet'),
    regex_rule('HadParity', 'HasParity'),
    regex_rule('Comments', 'Comment')
]


def xlate_field(f):
    for rule in FIELD_XLATIONS:
        result = rule(f)
        if result != f:
            return result
    return f


def write_yaml(row):
    print '-'
    for f in FIELDS:
        of = xlate_field(f)
        value = row[f]
        if not value:
            continue
        if f.startswith('Blob'):
            value = '\"%s\"' % (value,)
        print '    %s: %s' % (of, value)


def main(args):
    with open(args[1]

, 'rU') as f:
        test_reader = csv.DictReader(f, dialect='excel')
        first_row = True
        for row in test_reader:
            if first_row:
                first_row = False
            else:
                write_yaml(row)


if __name__ == '__main__':
    main(sys.argv)
