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

def connect_to_mysql():
    ''' connect to mysql
    '''
    path = 'mysql+pymysql://{}:{}@{}/{}'.format(
        CONFIG['user'], CONFIG['password'], CONFIG['db_host'], CONFIG['db'])

    db = create_engine(path)
    return db


def get_documents_from_db(db, language='en'):
    ''' query for all documents with a language and stream relevant fields. Return as iterator.
    >>> next(get_documents_from_db(connect_to_mysql()))
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
    >>> next(preprocess_documents(get_documents_from_solr(connect_to_mysql())))
    TaggedDocument(['introducing', 'mr', 'dlib', 'machine', 'readable', 'digital', 'library'], [1])
    '''
    i = 0
    for doc in documents:
        title = doc['title']
        docId = doc['id']
        words = gensim.utils.simple_preprocess(title)
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

def query_server(req, query):
    language = req.args.get('language', 'en')
    if language not in MODELS:
        return Response('No model for this language found.', status=501, mimetype='text/plain')

    results = query_similar(query, SIMILAR, MODELS[language])
    return Response(json.dumps(results), mimetype='application/json')

def train_model(language):
    print(f"Starting training doc2vec for {language} @ {datetime.datetime.now()}.")
    db = connect_to_mysql()
    docs = preprocess_documents(get_documents_from_db(db, language))
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
    global CONFIG_LOCATION, OUTPUT_FILE, BATCH_SIZE, DIMENSIONS, SIMILAR, MODELS, CONFIG, ROUTES, ENDPOINTS
    CONFIG_LOCATION = '../resources/config.properties'
    OUTPUT_FILE = 'model'
    BATCH_SIZE = 1000
    DIMENSIONS=30
    SIMILAR=3

    ROUTES = Map([
        Rule('/search/<query>', endpoint='query'),
        Rule('/train', endpoint='train')
    ])
    ENDPOINTS = { 'query': query_server, 'train': training_server }

    MODELS = {}
    CONFIG = read_config()
    run_simple('localhost', CONFIG['doc2vecServicePort'], wsgi_app)
