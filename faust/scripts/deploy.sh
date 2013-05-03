#!/bin/bash

/mnt/data/faust-*/bin/faust-server stop
unzip target/faust-*.zip -d /mnt/data
/mnt/data/faust-*/bin/faust-server start
