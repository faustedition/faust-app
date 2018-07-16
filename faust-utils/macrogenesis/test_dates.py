# coding=utf-8
from datetime import date

import pytest

from dates import collect_wiki_dates, TimeSpan
import dates


@pytest.mark.parametrize("string, parsed", [
    ("24.06.1794-09.05.1805", [TimeSpan(date(1794, 6, 24), date(1805, 5, 9))]),
    ("10.-15.09.1798", [TimeSpan(date(1798, 9, 10), date(1798, 9, 15))]),
    ("13.04.-08.11.1800", [TimeSpan(date(1800, 4, 13), date(1800, 11, 8))]),
    ("08.11.1800", [TimeSpan(date(1800, 11, 8), None)])
])
def test_parse_dates(string, parsed):
    assert parsed == list(collect_wiki_dates(string))


def test_time_line_re():
    match = dates._time_line_re.match('* Frankfurter Zeit → la la la')
    assert match.group(2) == 'Frankfurter Zeit'
    assert match.group(3) == 'la la la'

def test_parse():
    line = '* Frankfurter Zeit → 31.05.1773-07.11.1775 (= Ankunft in Weimar) (Gräf, Hans Gerhard: Goethe über seine Dichtungen. Zweiter Teil. Die dramatischen Dichtungen. Zweiter Band, Darmstadt'
    results = list(dates.parse_wiki_page([line]))
    assert len(results) == 1
    timespan, title, desc = results[0]
    assert title == 'Frankfurter Zeit'

def test_duplicate():
    line = '*Frühjahr 1831 → 30.04.1831-17.05.1831 (Gräf, Hans Gerhard: Goethe über seine Dichtungen. Zweiter Teil. Die dramatischen Dichtungen. Zweiter Band, Darmstadt 1968, S. 574 und 578 → vgl. Tagebucheinträge 30.04.1831 und 17.05.1831, Nr. 1898 und 1911) --> Terminus post quem ist nicht sicher, man könnte ihn wohl auch früher setzen, aber aus Briefen und Tagebüchern gehen keine genauen Angaben hervor, zumal im März auch wenig angefallen ist. Der Tagebucheintrag vom 30. April 1831 gibt den ersten deutlichen Anhaltspunkt. Am 17. Mai 1831 verzeichnet das Tagebuch Arbeit am Faust, spätere Einträge und Briefe gibt es auch, fraglich ist dann allerdings, ob diese Daten dann noch unter "Frühjahr" fallen.'
    results = list(dates.parse_wiki_page([line]))
    assert len(results) == 1

