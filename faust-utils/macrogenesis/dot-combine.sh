#!/bin/sh

echo 'digraph genesis {'
for name in "$@"
do
	tred "$name" | tail +2l | sed '$d'
done
echo '}'