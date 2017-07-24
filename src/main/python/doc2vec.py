#!/bin/env python3
import configparser
import sys
import os
import json
import threading
import datetime

from sqlalchemy import create_engine, MetaData, Table
from sqlalchemy.sql import select

from werkzeug.wrappers import Request, Response
from werkzeug.serving import run_simple
from werkzeug.routing import Map, Rule

import gensim

# dump data to csv using
# >mysqldump --user=root --password --host=localhost --tab=. --fields-terminated-by="," --fields-enclosed-by='"' --fields-escaped-by=\\ "mrdlib" "document_abstract"
# (for abstracts)
# and
# >mysqldump --user=root --password --host=localhost --tab=. --fields-terminated-by="," --fields-enclosed-by='"' --fields-escaped-by=\\ "mrdlib" "document"
# for documents

# TODO
# write tests & doc, run them
# annoy indexer
# deploy properly

# test via doctest

def read_config():
    ''' Read standard config file or file given by command line argument. Return as dictionary.
    >>> c = read_config()
    >>> 'db_host' in c['MrDlib']
    True
    >>> 'password' in c['MrDlib']
    True
    '''
    config = configparser.ConfigParser()
    if len(sys.argv) > 1 and os.path.isfile(sys.argv[1]):
        global CONFIG_LOCATION
        CONFIG_LOCATION = sys.argv[1]

    config.read(CONFIG_LOCATION)
    return config['MrDlib']

def open_data_source():
    ''' open csv dump of table
    '''
    path = 'mysql+pymysql://{}:{}@{}/{}'.format(
        CONFIG['user'], CONFIG['password'], CONFIG['db_host'], CONFIG['db'])

    db = create_engine(path)
    return db


def get_documents(db, language='en'):
    ''' query for all documents with a language and stream relevant fields. Return as iterator.
    >>> next(get_documents(open_data_source()))
    {'title': 'Introducing Mr. DLib, a Machine-readable Digital Library', 'id': 1}
    '''
    metadata = MetaData(db)
    documents = Table(CONFIG['documents'], metadata, autoload=True)
    start = 0

    def fetch_batch(offset):
        query = select([documents.c.title, documents.c.document_id])\
            .where(documents.c.language_detected == language)\
            .order_by(documents.c.document_id.asc())\
            .limit(BATCH_SIZE)\
            .offset(offset)
        results = db.connect().execute(query)
        for doc in results:
            yield {'title': doc[0], 'id': doc[1]}

    empty = False
    while not empty:
        data = fetch_batch(start)
        start += BATCH_SIZE

        empty = True
        for result in data:
            empty = False
            yield result



def preprocess_documents(documents):
    ''' convert documents to gensim's input format, lazily
    >>> next(preprocess_documents(get_documents(open_data_source())))
    TaggedDocument(['introducing', 'mr', 'dlib', 'machine', 'readable', 'digital', 'library'], [1])
    '''
    # i = 0
    for doc in documents:
        title = doc['title']
        docId = doc['id']
        words = gensim.utils.simple_preprocess(title)
        # i += 1
        # if i > 50:
        #     break
        yield gensim.models.doc2vec.TaggedDocument(words, [str(docId)]) # non-sequential int-tags seem to confuse gensim
            
def build_model(data, language):
    model = gensim.models.doc2vec.Doc2Vec(documents=data,size=DIMENSIONS)
    model.save(f"{OUTPUT_FILE}_{language}")
    return model

def query_similar(query, limit, model):
    words = gensim.utils.simple_preprocess(query)
    vector = model.infer_vector(words) 
    results = model.docvecs.most_similar([vector], topn=limit)
    return [docId for docId, sim in results]

def related_docs(docId, limit, model):
    results = model.docvecs.most_similar([str(docId)], topn=limit)
    return [docId for docId, sim in results]

def search_server(req, query):
    language = req.args.get('language', 'en')
    if language not in MODELS:
        return Response('No model for this language found.', status=501, mimetype='text/plain')
    limit = req.args.get('limit', LIMIT)

    results = query_similar(query, limit, MODELS[language])
    return Response(json.dumps(results), mimetype='application/json')

def related_server(req, docId):
    language = req.args.get('language', 'en')
    if language not in MODELS:
        return Response('No model for this language found.', status=501, mimetype='text/plain')
    limit = req.args.get('limit', LIMIT)

    results = related_docs(docId, limit, MODELS[language])
    return Response(json.dumps(results), mimetype='application/json')

def train_model(language):
    print(f"Starting training doc2vec for {language} @ {datetime.datetime.now()}.")
    db = open_data_source()
    docs = preprocess_documents(get_documents(db, language))
    model = build_model(docs, language)

    global MODELS
    MODELS[language] = model
    print(f"Finished training for {language} @ {datetime.datetime.now()}. Saving...")

def training_server(req):
    if 'language' not in req.args:
        return Response('Language parameter not provided.', status=400, mimetype='text/plain')

    language = req.args.get('language', 'en')
    thread = threading.Thread(target=train_model, args=(language,), daemon=False)
    thread.start()
    return Response('Started training.', status=200, mimetype='text/plain')

def load_server(req):
    if 'language' not in req.args:
        return Response('Language parameter not provided.', status=400, mimetype='text/plain')
    language = req.args.get('language', 'en')

    global MODELS
    try:
        MODELS[language] = gensim.models.Doc2Vec.load(f"{OUTPUT_FILE}_{language}")
        return Response('Model loaded.', status=200, mimetype='text/plain')
    except Exception, e:
        return Response('Could not load model: ' + str(e), status=500, mimetype='text/plain')


def dispatch_request(request):
    adapter = ROUTES.bind_to_environ(request.environ)
    endpoint, values = adapter.match()

    if endpoint in ENDPOINTS:
        return ENDPOINTS[endpoint](request, **values)
    else:
        return Response('Route not found.', status=404, mimetype='text/plain')

def wsgi_app(environ, start_response):
    request = Request(environ)
    response = dispatch_request(request)
    return response(environ, start_response)

if __name__ == '__main__':
    global CONFIG_LOCATION, OUTPUT_FILE, BATCH_SIZE, DIMENSIONS, LIMIT, MODELS, CONFIG, ROUTES, ENDPOINTS
    CONFIG_LOCATION = '../resources/config.properties'
    OUTPUT_FILE = 'model'
    BATCH_SIZE = 1000
    DIMENSIONS=30
    LIMIT=3

    ROUTES = Map([
        Rule('/search/<query>', endpoint='search'),
        Rule('/train', endpoint='train'),
        Rule('/load', endpoint='load'),
        Rule('/similar/<docId>', endpoint='similar')
    ])
    ENDPOINTS = {
        'search': search_server,
        'similar': related_server, 
        'train': training_server,
        'load': load_server
    }

    MODELS = {}
    CONFIG = read_config()
    run_simple('localhost', CONFIG['doc2vecServicePort'], wsgi_app)
