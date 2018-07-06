import pytest

from uris import Witness, Inscription, UnknownRef, AmbiguousRef


@pytest.fixture
def fake_json():
    return [
        {
            'uri': 'faust://document/faustedition/2_H',
            'sigil': '2 H',
            'sigil_t': '2_H',
            'other_sigils': {
                'faust://document/wa_faust/2_H'
            },
            'inscriptions': ['i_uebrige', 'i_oben']
        },
        {
            'uri': 'faust://document/faustedition/2_H_a',
            'sigil': '2 H a',
            'sigil_t': '2_H_a',
            'other_sigils': {
                'faust://document/wa_faust/2_H'
            }

        }
    ]


@pytest.fixture
def fake_db(fake_json):
    return Witness.build_database(fake_json)


def test_build_db(fake_db):
    h = fake_db.get('faust://document/faustedition/2_H')
    assert isinstance(h, Witness)


def test_build_db(fake_db):
    h = fake_db.get('faust://document/wa/2_H')
    assert isinstance(h, AmbiguousRef)
    assert h.first().sigil == '2 H'


def test_known_wit(fake_db):
    Witness.database = fake_db
    h = Witness.get('faust://document/faustedition/2_H')
    assert isinstance(h, Witness)
    assert h.uri == 'faust://document/faustedition/2_H'
    assert h.status == 'ok'


def test_other_uri(fake_db):
    Witness.database = fake_db
    h = Witness.get('faust://document/wa/2_H')
    assert isinstance(h, Witness)
    assert h.uri == 'faust://document/faustedition/2_H'
    assert h.status == 'ok'


def test_missing_stuff(fake_db):
    Witness.database = fake_db
    w = Witness.get('faust://document/fstedtn/2_H')
    assert w.status == 'unknown'


def test_ex_inscription(fake_db):
    Witness.database = fake_db
    i = Witness.get('faust://inscription/wa/2_H/i_uebrige')
    assert isinstance(i, Inscription)
    assert i.status == 'ok'


def test_missing_inscription(fake_db):
    Witness.database = fake_db
    i = Witness.get('faust://inscription/wa/2_H/gipsnich')
    assert isinstance(i, Inscription)
    assert i.status == 'unknown-inscription'


def test_missing_inscription_wit(fake_db):
    Witness.database = fake_db
    i = Witness.get('faust://inscription/wa/XXX/gipsnich')
    assert isinstance(i, Inscription)
    assert i.status == "unknown"
