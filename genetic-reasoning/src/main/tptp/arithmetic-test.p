# AXIOMS


# FACTS

tff( fact_hasline_1, axiom, (     has_line(inscription_c, 5)    )).

# QUERY

tff( query_syntagmatic_precedence, question, ( ? [X, Y]: (has_line(X, Y) & Y $greater 3)     )).