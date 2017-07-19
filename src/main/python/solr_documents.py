#!/bin/env python3
import scorched
import random

# TODO read config file
solr = scorched.SolrInterface('http://localhost:8983/solr/mrdlib_dev')

results = solr.query()

i = 0
updates = []
for result in results.sort_by('id').cursor(rows=10):
    print(f"{result['id']} = {result['title']}, {result['title_clean']}")
    updates.append({"id": result['id'], "title_clean": {"set": result['title_clean'].lower()}})
    i += 1
    if i > 20:
        print(result.keys())
        break

# solr.add(updates)
# solr.commit()
