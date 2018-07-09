#!/usr/bin/env python3
# coding=utf-8
import codecs
import os
import sys
from datetime import date
import re
import pandas as pd


class TimeSpan(object):
    def __init__(self, start, end, startExact=True, endExact=True):
        self.start = start
        self.end = end
        self.startExact = startExact
        self.endExact = None if end is None else endExact

    def __repr__(self):
        return self.__class__.__name__ + "(" + ", ".join(
                attr + '=' + repr(getattr(self, attr)) for attr in ['start', 'end', 'startExact', 'endExact']) + ")"

    def __eq__(self, other):
        return isinstance(other, TimeSpan) and \
               self.start == other.start and \
               self.end == other.end and \
               self.startExact == other.startExact and \
               self.endExact == other.endExact

    def __hash__(self):
        return hash(self.start) ^ hash(self.end) ^ hash(self.startExact) ^ hash(self.endExact)

    def is_point(self):
        return self.end is None


def collect_wiki_dates(line):
    timespans = []

    # collect:
    DATESPAN = re.compile(r'(?:(\d\d)\.(?:(\d\d)\.(\d\d\d\d)?)?(\([xX]\))?-)?(\d\d)\.(\d\d)\.(\d\d\d\d)(\([xX]\))?')
    for match in DATESPAN.finditer(line):
        end = date(int(match.group(7)), int(match.group(6)), int(match.group(5)))
        if match.group(1):
            start = date(int(match.group(3) if match.group(3) else match.group(7)),  # year
                         int(match.group(2) if match.group(2) else match.group(6)),  # month
                         int(match.group(1)))  # day
        else:
            start, end = end, None

        timespans.append(TimeSpan(start, end, match.group(4) is None, match.group(8) is None))

    # cleanup:
    result = []
    for timespan in timespans:
        if timespan in result:
            continue  # duplicate
        elif timespan.is_point():
            related = [other for other in timespans
                if timespan != other and (timespan.start == other.start or timespan.start == other.end)]
            if related:
                continue
        result.append(timespan)

    return result



_time_line_re = re.compile(r'^\*\s*((.*?)\s*→\s*)?(.*)')


def parse_wiki_page(lines):
    """

    Args:
        lines (List[str]): Lines from the wiki page
    """
    for line in lines:
        parts = _time_line_re.match(line)
        if parts:
            shortdesc = parts.group(2)
            rest = parts.group(3)
            for timespan in collect_wiki_dates(line):
                yield timespan, shortdesc, rest


def extract_wiki_timespans(filename):
    # type: (str) -> pd.DataFrame
    with codecs.open(filename, encoding='utf-8') as raw_wiki:
        df = pd.DataFrame([(timespan.start, timespan.end, timespan.startExact, timespan.endExact, shortdesc, rest)
                             for timespan, shortdesc, rest in parse_wiki_page(raw_wiki)],
                            columns=['start', 'end', 'startExact', 'endExact', 'shortDesc', 'rest'])
    df = df.sort_values(by='start', kind='mergesort')
    df.insert(4, 'score', df.startExact + df.endExact.fillna(0))
    return df


if __name__ == '__main__':
    df = extract_wiki_timespans(sys.argv[1])
    df.to_excel(os.path.splitext(sys.argv[1])[0] + '.xlsx', index=False)


