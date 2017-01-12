"""
A script that watches the directory containing the macrogenesis XML files and initiates the rendering and analysis
process whenever they change.
"""
import argparse
import logging
import multiprocessing
from watchdog.events import FileSystemEventHandler
from watchdog.observers import Observer
import watchdog
import os
import time
import faust
import visualize

logging.basicConfig(level=logging.INFO)
parser = argparse.ArgumentParser()
parser.add_argument("--watch", action="store_true",
                    help="keep monitoring and run rendering process whenever source xml files change")
args = parser.parse_args()


def process_data():
    visualize.visualize()


if args.watch:
    # watch xml directory for changes
    logging.info("Starting in watch mode.")


    class WatchHandler(FileSystemEventHandler):
        def __init__(self):
            self.counter = 0
            self.running_job = None

        def on_modified(self, event):
            if self.running_job is not None and self.running_job.is_alive():
                logging.info("Another render job already running, terminate.")
                self.running_job.terminate()

            logging.info("Starting new rendering process")


            self.running_job = multiprocessing.Process(target=process_data)
            self.running_job.start()
            self.counter += 1


    event_handler = WatchHandler()
    observer = Observer()
    macrogenesis_xml_dir = os.path.join(faust.xml_dir, 'macrogenesis')
    observer.schedule(event_handler, path=macrogenesis_xml_dir, recursive=True)
    observer.start()

    while True:
        time.sleep(5)
    observer.join()

else:
    process_data()