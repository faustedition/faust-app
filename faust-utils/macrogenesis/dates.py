#!/usr/bin/env python3
# coding=utf-8
import argparse
import codecs
import logging
import os
import sys
from collections import namedtuple
from datetime import date
import re
import pandas as pd
from lxml import etree

import faust
from graph import AbsoluteDating
from uris import Witness


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
    df.insert(4, 'score', 2 + df.startExact + df.endExact.fillna(-1))
    return df


def parse_absolute_datings():
    for macrogenetic_file in faust.macrogenesis_files():
        tree = etree.parse(macrogenetic_file)  # type: etree._ElementTree
        for date_el in tree.xpath('//f:date', namespaces=faust.namespaces):  # type: etree._Element
            try:
                abs_dating = AbsoluteDating(
                        when=date_el.get('when'),
                        from_=date_el.get('from'),
                        to=date_el.get('to'),
                        not_before=date_el.get('notBefore'),
                        not_after=date_el.get('notAfter'),
                        bibliographic_source=date_el.xpath('f:source/@uri', namespaces=faust.namespaces))
                comment = "|".join(date_el.xpath('f:comment/text()', namespaces=faust.namespaces))
                for uri in date_el.xpath('f:item/@uri', namespaces=faust.namespaces):
                    wit = Witness.get(uri)
                    yield abs_dating, comment, wit
            except Exception:
                logging.exception('Problem parsing %s', etree.tostring(date_el))


def rate_absolute_datings(known_info=None):
    if known_info is None:
        known_info = pd.read_excel('Handschriftendatierung.xlsx')
    Row = namedtuple('Row', "witness start end average label comment score bib".split())
    rows = []
    for dating, comment, wit in parse_absolute_datings():  # type: str, graph.AbsoluteDating
        match = known_info[(known_info.start == dating.first_possible()) & (known_info.end == dating.last_possible()) |
                           (known_info.start == dating.when)][:1]
        row = Row(witness=str(wit),
                  start=dating.first_possible(),
                  end=dating.last_possible(),
                  average=dating.average,
                  bib=dating.bibliographic_source,
                  label="|".join(match.shortDesc.values),
                  comment=comment,
                  score=match.score.mean())
        rows.append(row)
    return pd.DataFrame(rows).sort_values(by='average', kind='mergesort')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('-w', '--wiki-file', nargs=1)
    parser.add_argument('-a', '--absolute-datings', action='store_true', default=False)
    options = parser.parse_args()
    df = None
    if options.wiki_file:
        df = extract_wiki_timespans(options.wiki_file[0])
        df.to_excel(os.path.splitext(options.wiki_file[0])[0] + '.xlsx', index=False)
    if options.absolute_datings:
        abs_df = rate_absolute_datings(df)
        abs_df.to_excel('absolute_datings.xlsx', index=None)
