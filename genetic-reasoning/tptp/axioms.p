# AXIOMS

# *** Relationships between Lines ***

# transitivity of line order
fof( transitivity_of_line_less, axiom, (     (line_less (X,Y) & line_less(Y,Z)) => line_less(X,Z)     )).

# *** Relationships between Manuscripts ***

# Paradigmatic Relationship

# Paradigmatic Relationship Derives from Shared Lines
fof( para_from_shared_lines, axiom, (		(contains_line (X,L) & contains_line(Y,L)) => para(X,Y)		)).

# Syntagmatic Precedence

# Syntagmatic Precedence Derives from Line Order
fof( syntagmatic_from_, axiom, (		(contains_line (X,L) & contains_line(Y,M) & line_less(L,M)) => syn_pre(X,Y)		)).

# FACTS

# Line Order

fof( line_order_1, axiom, (     line_less(line_1, line_2 )    )).
fof( line_order_1, axiom, (     line_less(line_2, line_3 )    )).
fof( line_order_1, axiom, (     line_less(line_3, line_4 )    )).
fof( line_order_1, axiom, (     line_less(line_4, line_5 )    )).
fof( line_order_1, axiom, (     line_less(line_5, line_6 )    )).
fof( line_order_1, axiom, (     line_less(line_6, line_7 )    )).
fof( line_order_1, axiom, (     line_less(line_7, line_8 )    )).

# Inscription A
fof( fact_1, axiom, (     contains_line(inscription_a, line_2 )    )).
fof( fact_2, axiom, (     contains_line(inscription_a, line_3 )    )).
fof( fact_3, axiom, (     contains_line(inscription_a, line_4 )    )).

# Inscription B
fof( fact_4, axiom, (     contains_line(inscription_b, line_4 )    )).
fof( fact_5, axiom, (     contains_line(inscription_b, line_5 )    )).
fof( fact_6, axiom, (     contains_line(inscription_b, line_6 )    )).

# Inscription C
fof( fact_7, axiom, (     contains_line(inscription_c, line_6 )    )).
fof( fact_8, axiom, (     contains_line(inscription_c, line_7 )    )).
fof( fact_9, axiom, (     contains_line(inscription_c, line_8 )    )).


# QUERY

# Which manuscripts have a paradigmatic relationship?
# fof( query_para, question, ( ? [X,Y]: para(X,Y)     )).

# Show the line order
# fof( query_line_order, question, ( ? [X,Y]: line_less(X,Y)     )).

# Syntagmatic precedence
fof( query_syntagmatic_precedence, question, ( ? [X,Y]: syn_pre(X,Y)     )).
