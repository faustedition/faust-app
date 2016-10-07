import data_curation.query
import faust



#inscriptions_in_macrogenetic_files = data_curation.query.unique_values (faust.macrogenesis_files(),
#                                                                        '//f:item/@uri[contains(.,"i_")]')
#inscriptions_in_transcripts = data_curation.query.unique_values (faust.transcript_files(),
#                                                                        '//ge:stageNote/@xml:id[contains(.,"i_")]')
#bibliographic_sources_in_macrogenetic_files = data_curation.query.unique_values (faust.macrogenesis_files(),
#                                                                        '//f:source/@uri')



for f in faust.transcript_files():
    inscriptions = data_curation.query.unique_values ([f], '//ge:stageNote/@xml:id[contains(.,"i_")]')
    for i in inscriptions:
        print '%s/%s' % (f, i)




#print
#print 'Inscriptions in macrogenetic files:                      %i' % len(inscriptions_in_macrogenetic_files)
#for value in inscriptions_in_macrogenetic_files: print value
#print 'Inscriptions in transcript files:                        %i' % len(inscriptions_in_transcripts)
#for value in inscriptions_in_transcripts: print value
# need to prefix inscriptions_in_transcripts with transcript uri for this to work
#print 'References to i.s in macrogentic files without referent: %i' % len(inscriptions_in_macrogenetic_files - inscriptions_in_transcripts)
#print
#print 'Biligraphic sources in macrogenetic files:               %i' % len(bibliographic_sources_in_macrogenetic_files)
# for value in bibliographic_sources_in_macrogenetic_files: print value
