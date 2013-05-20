#!/bin/bash
exec ssh -N -f -L 3389:localhost:389 faust
